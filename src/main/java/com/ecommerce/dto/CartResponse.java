package com.ecommerce.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartResponse {

    private List<CartItemResponse> items;
}
