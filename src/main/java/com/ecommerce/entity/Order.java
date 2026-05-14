package com.ecommerce.entity;

import com.ecommerce.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "orders",
        indexes = {
                // Resume explicitly claims "strategic indexing on high-query columns
                // (product_id, user_id, order_status)" — user_id and status live here
                @Index(name = "idx_order_user",   columnList = "user_id"),
                @Index(name = "idx_order_status", columnList = "status")
        }
)
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Long order_id;

    // Changed from raw Long userId to proper FK relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Optional: link to the address used at time of checkout
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address shippingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}