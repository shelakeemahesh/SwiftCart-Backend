package com.swiftcart.service;

import com.swiftcart.config.KafkaConfig;
import com.swiftcart.entity.*;
import com.swiftcart.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final AddressRepository addressRepository;
    private final CouponRepository couponRepository;
    private final PricingService pricingService;
    private final NotificationService notificationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CartRepository cartRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            AddressRepository addressRepository,
            CouponRepository couponRepository,
            PricingService pricingService,
            NotificationService notificationService,
            java.util.Optional<KafkaTemplate<String, Object>> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.addressRepository = addressRepository;
        this.couponRepository = couponRepository;
        this.pricingService = pricingService;
        this.notificationService = notificationService;
        this.kafkaTemplate = kafkaTemplate.orElse(null);
    }

    public Page<Order> listUserOrders(Long userId, OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByUserIdAndStatus(userId, status, pageable);
        }
        return orderRepository.findByUserId(userId, pageable);
    }

    public java.util.Optional<Order> getLatestActiveOrder(Long userId) {
        List<OrderStatus> inactiveStatuses = List.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED, OrderStatus.RETURNED);
        return orderRepository.findFirstByUserIdAndStatusNotInOrderByIdDesc(userId, inactiveStatuses);
    }

    public Order getOrderDetail(String orderUuid) {
        return orderRepository.findByOrderUuid(orderUuid)
                .orElseThrow(() -> new RuntimeException("Order not found with UUID: " + orderUuid));
    }

    @Transactional
    public Order placeOrder(Long userId, Long addressId, String couponCode, PaymentMethod paymentMethod, String notes) {
        log.info("Starting order placement for user ID: {}", userId);

        // 1. Fetch Cart Items
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cannot place order with an empty cart");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Shipping address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized address usage");
        }

        // 2. Lock stock levels using Pessimistic Lock (SELECT ... FOR UPDATE) and validate availability
        BigDecimal mrpTotal = BigDecimal.ZERO;
        BigDecimal finalTotalBeforeCoupon = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem item : cartItems) {
            Product product;
            ProductVariant variant = null;

            // Lock the product base row
            product = productRepository.findAndLockById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product no longer exists"));

            if (!product.isActive()) {
                throw new RuntimeException("Product '" + product.getName() + "' is no longer active");
            }

            int requestedQty = item.getQuantity();

            // Lock variant row if exists
            if (item.getVariant() != null) {
                variant = variantRepository.findAndLockById(item.getVariant().getId())
                        .orElseThrow(() -> new RuntimeException("Variant no longer exists"));
                if (variant.getStockQty() < requestedQty) {
                    throw new RuntimeException("Insufficient stock for variant of product: " + product.getName());
                }
                // Decrement stock
                variant.setStockQty(variant.getStockQty() - requestedQty);
                variantRepository.save(variant);
            } else {
                if (product.getStockQty() < requestedQty) {
                    throw new RuntimeException("Insufficient stock for product: " + product.getName());
                }
                // Decrement stock
                product.setStockQty(product.getStockQty() - requestedQty);
                productRepository.save(product);
            }

            // Calculate unit price factoring in active flash sales
            BigDecimal effectiveUnitPrice = pricingService.getEffectiveProductPrice(product);
            if (variant != null) {
                effectiveUnitPrice = effectiveUnitPrice.add(variant.getAdditionalPrice());
            }

            BigDecimal itemMrpTotal = product.getMrp().multiply(BigDecimal.valueOf(requestedQty));
            BigDecimal itemFinalTotal = effectiveUnitPrice.multiply(BigDecimal.valueOf(requestedQty));

            mrpTotal = mrpTotal.add(itemMrpTotal);
            finalTotalBeforeCoupon = finalTotalBeforeCoupon.add(itemFinalTotal);

            // Populate SNAPSHOT details for the order item record (freezing values)
            OrderItem.ProductSnapshot snapshot = OrderItem.ProductSnapshot.builder()
                    .name(product.getName())
                    .brand(product.getBrand())
                    .sku(variant != null ? variant.getSku() : null)
                    .imageUrl(product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl())
                    .basePrice(effectiveUnitPrice)
                    .mrp(product.getMrp())
                    .build();

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .variant(variant)
                    .productSnapshot(snapshot)
                    .quantity(requestedQty)
                    .unitPrice(effectiveUnitPrice)
                    .mrp(product.getMrp())
                    .discount(product.getMrp().subtract(effectiveUnitPrice))
                    .total(itemFinalTotal)
                    .build();

            orderItems.add(orderItem);
        }

        // 3. Fee structure
        BigDecimal deliveryFee = finalTotalBeforeCoupon.compareTo(BigDecimal.valueOf(500)) >= 0 
                ? BigDecimal.ZERO : BigDecimal.valueOf(40); // Free delivery on orders >= 500
        BigDecimal platformFee = BigDecimal.valueOf(5); // Default platform fee
        BigDecimal discountTotal = mrpTotal.subtract(finalTotalBeforeCoupon);

        // 4. Validate and apply coupon
        BigDecimal couponDiscount = BigDecimal.ZERO;
        Coupon appliedCoupon = null;
        if (couponCode != null && !couponCode.isBlank()) {
            appliedCoupon = pricingService.validateCoupon(couponCode, finalTotalBeforeCoupon, userId);
            if (appliedCoupon.getType() == CouponType.FREE_DELIVERY) {
                deliveryFee = BigDecimal.ZERO;
            } else {
                couponDiscount = pricingService.calculateCouponDiscount(appliedCoupon, finalTotalBeforeCoupon);
            }
        }

        BigDecimal finalAmount = finalTotalBeforeCoupon.add(deliveryFee).add(platformFee).subtract(couponDiscount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        // 5. Build and save Order
        Order order = Order.builder()
                .user(cartItems.get(0).getUser())
                .address(address)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentMethod(paymentMethod)
                .mrpTotal(mrpTotal)
                .discountTotal(discountTotal)
                .deliveryFee(deliveryFee)
                .platformFee(platformFee)
                .couponDiscount(couponDiscount)
                .finalAmount(finalAmount)
                .couponCode(couponCode)
                .notes(notes)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Save order items linking to the order
        for (OrderItem oi : orderItems) {
            oi.setOrder(savedOrder);
            orderItemRepository.save(oi);
        }
        savedOrder.setItems(orderItems);

        // Update coupon usage count
        if (appliedCoupon != null) {
            appliedCoupon.setUsedCount(appliedCoupon.getUsedCount() + 1);
            couponRepository.save(appliedCoupon);
        }

        // 6. Clear Cart
        cartRepository.deleteByUserId(userId);

        // 7. Increment product sold counts
        for (OrderItem oi : orderItems) {
            Product p = oi.getProduct();
            p.setSoldCount(p.getSoldCount() + oi.getQuantity());
            productRepository.save(p);
        }

        // 8. Publish OrderPlaced Kafka event
        publishOrderPlacedEvent(savedOrder);

        log.info("Order placed successfully with UUID: {}", savedOrder.getOrderUuid());
        return savedOrder;
    }

    @Transactional
    public Order cancelOrder(String orderUuid, Long userId) {
        Order order = orderRepository.findByOrderUuid(orderUuid)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized cancel request");
        }

        // Cancellable only if status is PENDING or CONFIRMED
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("Order is in state " + order.getStatus() + " and cannot be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED); // Trigger refund logic
        }
        notificationService.sendOrderStatusUpdate(order.getUser().getEmail(), order.getOrderUuid(), "CANCELLED");
        
        // Restore stock
        for (OrderItem item : order.getItems()) {
            if (item.getVariant() != null) {
                ProductVariant variant = item.getVariant();
                variant.setStockQty(variant.getStockQty() + item.getQuantity());
                variantRepository.save(variant);
            } else {
                Product product = item.getProduct();
                product.setStockQty(product.getStockQty() + item.getQuantity());
                productRepository.save(product);
            }
        }

        Order saved = orderRepository.save(order);
        publishOrderStatusEvent(saved);
        return saved;
    }

    @Transactional
    public Order requestReturn(String orderUuid, Long userId) {
        Order order = orderRepository.findByOrderUuid(orderUuid)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized return request");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("Only delivered orders can be returned");
        }

        order.setStatus(OrderStatus.RETURN_REQUESTED);
        Order saved = orderRepository.save(order);
        publishOrderStatusEvent(saved);
        return saved;
    }

    @Transactional
    public Order updateOrderStatusBySellerOrAdmin(String orderUuid, OrderStatus newStatus) {
        Order order = orderRepository.findByOrderUuid(orderUuid)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        publishOrderStatusEvent(saved);
        return saved;
    }

    private void publishOrderPlacedEvent(Order order) {
        try {
            kafkaTemplate.send(KafkaConfig.ORDER_PLACED_TOPIC, order.getOrderUuid(), order.getOrderUuid());
        } catch (Exception e) {
            log.error("Failed to publish OrderPlaced event to Kafka: {}", e.getMessage());
        }
    }

    private void publishOrderStatusEvent(Order order) {
        try {
            kafkaTemplate.send(KafkaConfig.ORDER_STATUS_TOPIC, order.getOrderUuid(), order.getStatus().name());
        } catch (Exception e) {
            log.error("Failed to publish OrderStatus event to Kafka: {}", e.getMessage());
        }
    }
}
