package com.ecommerce.dto;

import lombok.Data;

@Data
public class CartItemResponse {
    private Long productId;
    private int quantity;
}
