package com.swiftcart.repository;

import com.swiftcart.entity.RazorpayPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RazorpayPaymentRepository extends JpaRepository<RazorpayPayment, Long> {
    Optional<RazorpayPayment> findByRazorpayOrderId(String razorpayOrderId);
    Optional<RazorpayPayment> findByRazorpayPaymentId(String razorpayPaymentId);
}
