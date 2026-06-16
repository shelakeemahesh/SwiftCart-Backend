package com.swiftcart.controller;

import com.swiftcart.dto.response.ApiResponse;

import com.swiftcart.entity.Order;
import com.swiftcart.enums.OrderStatus;
import com.swiftcart.entity.Product;
import com.swiftcart.entity.Review;
import com.swiftcart.entity.User;
import com.swiftcart.repository.OrderRepository;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.ReviewRepository;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public ReviewController(
            ReviewRepository reviewRepository,
            OrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            ProductService productService) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productService = productService;
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<Page<Review>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        if (rating != null) {
            return ResponseEntity.ok(ApiResponse.success(reviewRepository.findByProductIdAndRating(productId, rating, pageRequest)));
        }
        return ResponseEntity.ok(ApiResponse.success(reviewRepository.findByProductId(productId, pageRequest)));
    }

    @PostMapping("/products/{productId}")
    @Transactional
    public ResponseEntity<ApiResponse<Review>> submitReview(
            Principal principal,
            @PathVariable Long productId,
            @RequestBody Review reviewRequest) {

        User user = getUserFromPrincipal(principal);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Page<Order> userOrders = orderRepository.findByUserId(user.getId(), PageRequest.of(0, 100));
        Optional<Order> verifiedOrderOpt = userOrders.getContent().stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .filter(o -> o.getItems().stream().anyMatch(item -> item.getProduct().getId().equals(productId)))
                .findFirst();

        if (verifiedOrderOpt.isEmpty()) {
            throw new RuntimeException("Review submission rejected: You must purchase and receive this product before reviewing it.");
        }

        Order verifiedOrder = verifiedOrderOpt.get();

        if (reviewRepository.existsByUserIdAndProductIdAndOrderId(user.getId(), productId, verifiedOrder.getId())) {
            throw new RuntimeException("Review already submitted for this product order");
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .order(verifiedOrder)
                .rating(reviewRequest.getRating())
                .title(reviewRequest.getTitle())
                .body(reviewRequest.getBody())
                .images(reviewRequest.getImages())
                .isVerifiedPurchase(true)
                .build();

        Review saved = reviewRepository.save(review);

        // This comment is written by human not ai - Async recalculation of average rating directly
        productService.recalculateProductRatingAsync(productId);

        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Review>> editReview(Principal principal, @PathVariable Long reviewId, @RequestBody Review updated) {
        User user = getUserFromPrincipal(principal);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized edit request");
        }

        review.setTitle(updated.getTitle());
        review.setBody(updated.getBody());
        review.setRating(updated.getRating());
        Review saved = reviewRepository.save(review);

        productService.recalculateProductRatingAsync(review.getProduct().getId());

        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteReview(Principal principal, @PathVariable Long reviewId) {
        User user = getUserFromPrincipal(principal);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized delete request");
        }

        reviewRepository.delete(review);

        productService.recalculateProductRatingAsync(review.getProduct().getId());

        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Review deleted successfully")));
    }

    @PostMapping("/{reviewId}/helpful")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markHelpful(Principal principal, @PathVariable Long reviewId) {
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setHelpfulCount(review.getHelpfulCount() + 1);
        reviewRepository.save(review);

        return ResponseEntity.ok(ApiResponse.success(Map.of("helpfulCount", review.getHelpfulCount(), "message", "Marked as helpful")));
    }

    private User getUserFromPrincipal(Principal principal) {
        return com.swiftcart.security.SecurityUtil.getUserFromPrincipal(principal, userRepository);
    }
}
