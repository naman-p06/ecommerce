package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Used in search: /api/products/search?category=mobile-phones
    Optional<Category> findBySlug(String slug);

    boolean existsByName(String name);
}