package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reviews",
        indexes = {
                // Every "get reviews for product X" query hits product_id
                @Index(name = "idx_review_product", columnList = "product_id"),
                @Index(name = "idx_review_user",    columnList = "user_id")
        },
        // One user can review a product exactly once — enforced at DB level
        uniqueConstraints = {
                @UniqueConstraint(
                        name  = "uq_user_product_review",
                        columnNames = {"user_id", "product_id"}
                )
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // @Min/@Max enforced at the Java layer; the DB column is just an int
    @Min(1) @Max(5)
    @Column(nullable = false)
    private int rating;

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Automatically set createdAt before the first INSERT
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}