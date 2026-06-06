package com.swiftcart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class SwiftCartBackendTests {

    @Test
    void contextLoads() {
        // Simple sanity check to verify application context boot configuration
    }
}
