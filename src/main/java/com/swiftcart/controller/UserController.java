package com.swiftcart.controller;

import com.swiftcart.dto.response.ApiResponse;

import com.swiftcart.entity.Address;
import com.swiftcart.entity.User;
import com.swiftcart.repository.AddressRepository;
import com.swiftcart.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, AddressRepository addressRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getProfile(Principal principal) {
        User user = getUserFromPrincipal(principal);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<User>> updateProfile(Principal principal, @RequestBody Map<String, String> body) {
        User user = getUserFromPrincipal(principal);
        if (body.containsKey("name")) user.setName(body.get("name"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));
        if (body.containsKey("profileImageUrl")) user.setProfileImageUrl(body.get("profileImageUrl"));
        return ResponseEntity.ok(ApiResponse.success(userRepository.save(user)));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Map<String, String>>> changePassword(Principal principal, @RequestBody Map<String, String> body) {
        User user = getUserFromPrincipal(principal);
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (user.getPasswordHash() != null && !passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Incorrect current password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Password changed successfully")));
    }

    @GetMapping("/me/addresses")
    public ResponseEntity<ApiResponse<List<Address>>> listAddresses(Principal principal) {
        User user = getUserFromPrincipal(principal);
        return ResponseEntity.ok(ApiResponse.success(addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId())));
    }

    @PostMapping("/me/addresses")
    @Transactional
    public ResponseEntity<ApiResponse<Address>> addAddress(Principal principal, @RequestBody Address address) {
        User user = getUserFromPrincipal(principal);
        address.setUser(user);

        if (address.isDefault()) {
            addressRepository.resetDefaultAddressForUser(user.getId());
        }

        Address saved = addressRepository.save(address);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PutMapping("/me/addresses/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Address>> updateAddress(Principal principal, @PathVariable Long id, @RequestBody Address updatedAddress) {
        User user = getUserFromPrincipal(principal);
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized address modification");
        }

        address.setLabel(updatedAddress.getLabel());
        address.setRecipientName(updatedAddress.getRecipientName());
        address.setPhone(updatedAddress.getPhone());
        address.setPincode(updatedAddress.getPincode());
        address.setFlatHouse(updatedAddress.getFlatHouse());
        address.setArea(updatedAddress.getArea());
        address.setCity(updatedAddress.getCity());
        address.setState(updatedAddress.getState());

        if (updatedAddress.isDefault() && !address.isDefault()) {
            addressRepository.resetDefaultAddressForUser(user.getId());
            address.setDefault(true);
        }

        return ResponseEntity.ok(ApiResponse.success(addressRepository.save(address)));
    }

    @DeleteMapping("/me/addresses/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteAddress(Principal principal, @PathVariable Long id) {
        User user = getUserFromPrincipal(principal);
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized address removal");
        }

        addressRepository.delete(address);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Address deleted successfully")));
    }

    @PutMapping("/me/addresses/{id}/default")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, String>>> setDefaultAddress(Principal principal, @PathVariable Long id) {
        User user = getUserFromPrincipal(principal);
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized address update");
        }

        addressRepository.resetDefaultAddressForUser(user.getId());
        address.setDefault(true);
        addressRepository.save(address);

        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Default address updated")));
    }

    @GetMapping("/{customerId}/profile")
    @PreAuthorize("@swiftSecurity.isAdminOrCustomerOwner(#customerId)")
    public ResponseEntity<ApiResponse<User>> getCustomerProfile(@PathVariable Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    private User getUserFromPrincipal(Principal principal) {
        return com.swiftcart.security.SecurityUtil.getUserFromPrincipal(principal, userRepository);
    }
}
