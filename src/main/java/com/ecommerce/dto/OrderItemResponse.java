package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {

    private Long   productId;
    private String productName;
    private int    quantity;
    private double priceAtPurchase;
    private double lineTotal;
}