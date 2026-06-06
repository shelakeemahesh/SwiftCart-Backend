package com.swiftcart.repository;

import com.swiftcart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndProvider(String email, String provider);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
}
