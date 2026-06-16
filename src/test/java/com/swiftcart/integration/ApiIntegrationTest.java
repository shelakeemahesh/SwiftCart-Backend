package com.swiftcart.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftcart.dto.request.RegisterRequest;
import com.swiftcart.dto.request.SellerRegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCustomerRegistrationFlow() throws Exception {
        
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Test Customer");
        registerRequest.setEmail("test_integration_customer@swiftcart.com");
        registerRequest.setPhone("9876543210");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("User registered successfully. Please verify your phone number via OTP."));

        mockMvc.perform(post("/api/v1/auth/send-otp")
                .param("phone", "9876543210"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("OTP sent successfully to phone 9876543210"));

    }

    @Test
    void testSellerRegistrationFlow() throws Exception {
        SellerRegisterRequest request = new SellerRegisterRequest();
        request.setName("Test Seller");
        request.setEmail("test_integration_seller@swiftcart.com");
        request.setPhone("9876543211");
        request.setPassword("password123");
        request.setBusinessName("Test Business");
        request.setGstin("22AAAAA0000A1Z5");
        request.setPanNumber("ABCDE1234F");
        request.setPickupAddress("123 Street");
        request.setPickupPincode("400001");
        request.setBankAccountNumber("1234567890");
        request.setIfscCode("SBIN0000123");

        mockMvc.perform(post("/api/v1/auth/register/seller")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Seller registered successfully. Please verify your phone number via OTP."));
    }

    @Test
    void testPublicProductsEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void testSearchEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                .param("q", "speaker"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }
}
