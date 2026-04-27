package com.ecommerce.entity;

import com.ecommerce.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id",nullable = false)
    private Long order_id;

    @Column(nullable = false)
    private Long userId;

    private double totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // CREATED, FAILED, SUCCESS

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> items;
}

