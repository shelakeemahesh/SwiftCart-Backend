package com.swiftcart;

import com.swiftcart.entity.*;
import com.swiftcart.repository.*;
import com.swiftcart.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@ActiveProfiles("dev")
public class ConcurrentOrderLockTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RazorpayPaymentRepository razorpayPaymentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User seller;
    private List<User> customers = new ArrayList<>();
    private List<Address> addresses = new ArrayList<>();
    private Product product;

    @BeforeEach
    public void setup() {
        cartRepository.deleteAll();
        reviewRepository.deleteAll();
        razorpayPaymentRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        addressRepository.deleteAll();
        userRepository.deleteAll();

        customers.clear();
        addresses.clear();

        // 1. Create seller
        seller = userRepository.save(User.builder()
                .phone("9999999990")
                .email("seller@swiftcart.com")
                .name("Seller Test")
                .role(Role.SELLER)
                .isVerified(true)
                .build());

        // 2. Create 10 distinct customers and addresses
        for (int i = 0; i < 10; i++) {
            User customer = userRepository.save(User.builder()
                    .phone("999999999" + (i + 1))
                    .email("customer" + i + "@swiftcart.com")
                    .name("Customer " + i)
                    .role(Role.CUSTOMER)
                    .isVerified(true)
                    .build());
            customers.add(customer);

            Address address = addressRepository.save(Address.builder()
                    .user(customer)
                    .label(Label.HOME)
                    .recipientName("Customer " + i)
                    .phone("999999999" + (i + 1))
                    .pincode("560001")
                    .flatHouse("Flat 10" + i)
                    .area("Indiranagar")
                    .city("Bangalore")
                    .state("Karnataka")
                    .isDefault(true)
                    .build());
            addresses.add(address);
        }

        // Create category
        Category category = categoryRepository.save(Category.builder()
                .name("Electronics")
                .slug("electronics")
                .isActive(true)
                .build());

        // 4. Create product with low stock (e.g., 5 items)
        product = productRepository.save(Product.builder()
                .seller(seller)
                .category(category)
                .brand("BrandX")
                .name("Limited Product")
                .slug("limited-product")
                .description("Limited Stock Item")
                .basePrice(BigDecimal.valueOf(100))
                .mrp(BigDecimal.valueOf(120))
                .stockQty(5)
                .isActive(true)
                .build());
    }

    @Test
    public void testConcurrentOrderPlacementPessimisticLock() throws InterruptedException {
        // Set up 10 concurrent requests trying to buy 1 item each (total 10 items requested, but only 5 in stock)
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final User threadCustomer = customers.get(i);
            final Address threadAddress = addresses.get(i);
            tasks.add(() -> {
                latch.await(); // wait for the start signal
                try {
                    // Populate cart for customer
                    // Normally placing order pulls from cart, so we add 1 item to cart before checking out
                    CartItem cartItem = cartRepository.save(CartItem.builder()
                            .user(threadCustomer)
                            .product(product)
                            .quantity(1)
                            .build());

                    orderService.placeOrder(
                            threadCustomer.getId(),
                            threadAddress.getId(),
                            null,
                            PaymentMethod.COD,
                            "Concurrent Test Notes"
                    );
                    successCount.incrementAndGet();
                    return true;
                } catch (Exception e) {
                    System.err.println("Thread failed placing order: " + e.getMessage());
                    e.printStackTrace();
                    failureCount.incrementAndGet();
                    return false;
                }
            });
        }

        List<Future<Boolean>> futures = new ArrayList<>();
        for (Callable<Boolean> task : tasks) {
            futures.add(executorService.submit(task));
        }

        // Release the latch to start all threads concurrently
        latch.countDown();

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // Fetch product again to assert remaining stock is exactly 0
        Product reloadedProduct = productRepository.findById(product.getId()).orElseThrow();

        System.out.println("Concurrent Order Placement Results:");
        System.out.println("Successful placements: " + successCount.get());
        System.out.println("Failed placements: " + failureCount.get());
        System.out.println("Remaining stock: " + reloadedProduct.getStockQty());

        // Assert that exactly 5 orders succeeded (since stock limit was 5) and remaining stock is 0
        Assertions.assertEquals(5, successCount.get(), "Only 5 placements should succeed");
        Assertions.assertEquals(5, failureCount.get(), "5 placements should fail due to stock depletion");
        Assertions.assertEquals(0, reloadedProduct.getStockQty(), "Remaining stock should be exactly 0");
    }
}
