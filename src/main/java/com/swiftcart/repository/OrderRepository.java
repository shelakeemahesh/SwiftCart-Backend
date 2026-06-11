package com.swiftcart.repository;

import com.swiftcart.entity.Order;
import com.swiftcart.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderUuid(String orderUuid);
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);
    Optional<Order> findFirstByUserIdAndStatusNotInOrderByIdDesc(Long userId, List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o JOIN o.items item WHERE item.product.seller.id = :sellerId")
    List<Order> findBySellerId(@Param("sellerId") Long sellerId);
}
