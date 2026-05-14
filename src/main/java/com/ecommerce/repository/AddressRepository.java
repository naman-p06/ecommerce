package com.ecommerce.repository;

import com.ecommerce.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    // Get all addresses for a user — filtered by user_id (indexed)
    List<Address> findByUserId(Long userId);

    // Find the default address for checkout pre-fill
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
}