package com.swiftcart.security;

import com.swiftcart.entity.OrderItem;
import com.swiftcart.entity.Product;
import com.swiftcart.entity.Order;
import com.swiftcart.enums.Role;
import com.swiftcart.entity.User;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.OrderRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("swiftSecurity")
public class SwiftCartSecurityExpression {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public SwiftCartSecurityExpression(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public boolean isAdminOrSellerOwner(Long sellerId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;
        if (currentUser.getRole() == Role.ADMIN) return true;
        return currentUser.getRole() == Role.SELLER && currentUser.getId().equals(sellerId);
    }

    public boolean isAdminOrCustomerOwner(Long customerId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;
        if (currentUser.getRole() == Role.ADMIN) return true;
        return currentUser.getId().equals(customerId);
    }

    public boolean canManageProduct(Long productId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;
        if (currentUser.getRole() == Role.ADMIN) return true;
        
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) return false;
        return currentUser.getRole() == Role.SELLER && currentUser.getId().equals(productOpt.get().getSeller().getId());
    }

    public boolean canCustomerManageOrder(String orderUuid) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;
        if (currentUser.getRole() == Role.ADMIN) return true;

        Optional<Order> orderOpt = orderRepository.findByOrderUuid(orderUuid);
        if (orderOpt.isEmpty()) return false;
        return currentUser.getId().equals(orderOpt.get().getUser().getId());
    }

    public boolean canSellerManageOrder(String orderUuid) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;
        if (currentUser.getRole() == Role.ADMIN) return true;

        Optional<Order> orderOpt = orderRepository.findByOrderUuid(orderUuid);
        if (orderOpt.isEmpty()) return false;

        Order order = orderOpt.get();
        if (order.getItems() == null) return false;

        return currentUser.getRole() == Role.SELLER && order.getItems().stream()
                .anyMatch(item -> item.getProduct() != null &&
                        item.getProduct().getSeller() != null &&
                        currentUser.getId().equals(item.getProduct().getSeller().getId()));
    }

    public boolean canManageOrder(String orderUuid) {
        return canCustomerManageOrder(orderUuid);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) principal).getUser();
        }
        return null;
    }
}
