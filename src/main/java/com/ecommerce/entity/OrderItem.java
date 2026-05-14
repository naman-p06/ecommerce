package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "order_items",
        indexes = {
                // Resume claims index on product_id — this is where it lives for order queries
                @Index(name = "idx_orderitem_product", columnList = "product_id")
        }
)
@Data
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Proper FK instead of raw Long productId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantity;

    // Snapshot of price at time of order — important because product price can change later
    // Never recalculate from Product.price; always use this stored value
    @Column(nullable = false)
    private double priceAtPurchase;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}