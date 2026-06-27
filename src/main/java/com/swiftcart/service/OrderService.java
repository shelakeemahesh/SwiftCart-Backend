package com.swiftcart.service;

import com.swiftcart.enums.*;
import com.swiftcart.entity.*;
import com.swiftcart.repository.*;
import com.swiftcart.kafka.producer.OrderEventProducer;
import com.swiftcart.event.LiveActivityEvent;
import com.swiftcart.kafka.producer.LiveActivityProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    private final OrderEventProducer orderEventProducer;
    private final LiveActivityProducer liveActivityProducer;

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
            OrderEventProducer orderEventProducer,
            LiveActivityProducer liveActivityProducer) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.addressRepository = addressRepository;
        this.couponRepository = couponRepository;
        this.pricingService = pricingService;
        this.notificationService = notificationService;
        this.orderEventProducer = orderEventProducer;
        this.liveActivityProducer = liveActivityProducer;
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

        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cannot place order with an empty cart");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Shipping address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized address usage");
        }

        BigDecimal mrpTotal = BigDecimal.ZERO;
        BigDecimal finalTotalBeforeCoupon = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem item : cartItems) {
            Product product;
            ProductVariant variant = null;

            product = productRepository.findAndLockById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product no longer exists"));

            if (!product.isActive()) {
                throw new RuntimeException("Product '" + product.getName() + "' is no longer active");
            }

            int requestedQty = item.getQuantity();

            if (item.getVariant() != null) {
                variant = variantRepository.findAndLockById(item.getVariant().getId())
                        .orElseThrow(() -> new RuntimeException("Variant no longer exists"));
                if (variant.getStockQty() < requestedQty) {
                    throw new RuntimeException("Insufficient stock for variant of product: " + product.getName());
                }
                
                variant.setStockQty(variant.getStockQty() - requestedQty);
                variantRepository.save(variant);
            } else {
                if (product.getStockQty() < requestedQty) {
                    throw new RuntimeException("Insufficient stock for product: " + product.getName());
                }
                
                product.setStockQty(product.getStockQty() - requestedQty);
                productRepository.save(product);
            }

            BigDecimal effectiveUnitPrice = pricingService.getEffectiveProductPrice(product);
            if (variant != null) {
                effectiveUnitPrice = effectiveUnitPrice.add(variant.getAdditionalPrice());
            }

            BigDecimal itemMrpTotal = product.getMrp().multiply(BigDecimal.valueOf(requestedQty));
            BigDecimal itemFinalTotal = effectiveUnitPrice.multiply(BigDecimal.valueOf(requestedQty));

            mrpTotal = mrpTotal.add(itemMrpTotal);
            finalTotalBeforeCoupon = finalTotalBeforeCoupon.add(itemFinalTotal);

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

        BigDecimal deliveryFee = finalTotalBeforeCoupon.compareTo(BigDecimal.valueOf(500)) >= 0 
                ? BigDecimal.ZERO : BigDecimal.valueOf(40); 
        BigDecimal platformFee = BigDecimal.valueOf(5); 
        BigDecimal discountTotal = mrpTotal.subtract(finalTotalBeforeCoupon);

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

        for (OrderItem oi : orderItems) {
            oi.setOrder(savedOrder);
            orderItemRepository.save(oi);
        }
        savedOrder.setItems(orderItems);

        if (appliedCoupon != null) {
            appliedCoupon.setUsedCount(appliedCoupon.getUsedCount() + 1);
            couponRepository.save(appliedCoupon);
        }

        cartRepository.deleteByUserId(userId);

        for (OrderItem oi : orderItems) {
            Product p = oi.getProduct();
            p.setSoldCount(p.getSoldCount() + oi.getQuantity());
            productRepository.save(p);
        }

        if (orderEventProducer.isKafkaEnabled()) {
            orderEventProducer.publishOrderPlaced(savedOrder.getOrderUuid());
        } else {
            if (savedOrder.getUser() != null && savedOrder.getUser().getEmail() != null) {
                notificationService.sendOrderConfirmation(savedOrder.getUser().getEmail(), savedOrder.getOrderUuid());
            }
        }

        log.info("Order placed successfully with UUID: {}", savedOrder.getOrderUuid());
        try {
            liveActivityProducer.publishEvent(new LiveActivityEvent(
                    "PURCHASE",
                    savedOrder.getUser() != null ? savedOrder.getUser().getName() : "Customer",
                    orderItems.isEmpty() ? "Product" : (orderItems.get(0).getProduct() != null ? orderItems.get(0).getProduct().getName() : "Product"),
                    savedOrder.getAddress() != null ? savedOrder.getAddress().getCity() : "Bengaluru"
            ));
        } catch (Exception e) {
            log.error("Failed to publish live activity event", e);
        }
        return savedOrder;
    }

    @Transactional
    public Order cancelOrder(String orderUuid, Long userId) {
        Order order = orderRepository.findByOrderUuid(orderUuid)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized cancel request");
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("Order is in state " + order.getStatus() + " and cannot be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED); 
        }

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

        if (orderEventProducer.isKafkaEnabled()) {
            orderEventProducer.publishOrderStatusChange(saved.getOrderUuid(), "CANCELLED");
        } else {
            if (saved.getUser() != null && saved.getUser().getEmail() != null) {
                notificationService.sendOrderStatusUpdate(saved.getUser().getEmail(), saved.getOrderUuid(), "CANCELLED");
            }
        }

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

        if (orderEventProducer.isKafkaEnabled()) {
            orderEventProducer.publishOrderStatusChange(saved.getOrderUuid(), saved.getStatus().name());
        } else {
            if (saved.getUser() != null && saved.getUser().getEmail() != null) {
                notificationService.sendOrderStatusUpdate(saved.getUser().getEmail(), saved.getOrderUuid(), saved.getStatus().name());
            }
        }

        return saved;
    }

    @Transactional
    public Order updateOrderStatusBySellerOrAdmin(String orderUuid, OrderStatus newStatus) {
        Order order = orderRepository.findByOrderUuid(orderUuid)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        if (orderEventProducer.isKafkaEnabled()) {
            orderEventProducer.publishOrderStatusChange(saved.getOrderUuid(), saved.getStatus().name());
        } else {
            if (saved.getUser() != null && saved.getUser().getEmail() != null) {
                notificationService.sendOrderStatusUpdate(saved.getUser().getEmail(), saved.getOrderUuid(), saved.getStatus().name());
            }
        }

        return saved;
    }

}
