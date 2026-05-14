package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "products",
        indexes = {
                // Index on product id is implicit (PK), but we add it on FK columns
                // that appear in WHERE clauses across CartItem, OrderItem, Review queries
                @Index(name = "idx_product_category", columnList = "category_id"),
                @Index(name = "idx_product_name",     columnList = "name")
        }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "description")          // fixed: original had capital D, maps to "Description" column
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int stock;

    // Product owns the FK (category_id column lives in products table)
    // LAZY: loading a product does NOT automatically load and JOIN the full category row
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")      // nullable = true so existing products without category still work
    private Category category;
}