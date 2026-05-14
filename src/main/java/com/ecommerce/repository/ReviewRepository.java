package com.ecommerce.repository;

import com.ecommerce.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // All reviews for a product — used on product detail page
    List<Review> findByProductId(Long productId);

    // Check if user already reviewed this product (enforce one-review-per-user rule in service layer too)
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // Compute average rating for a product — used in ProductService to enrich ProductResponse
    // COALESCE returns 0.0 instead of null when no reviews exist
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);
}