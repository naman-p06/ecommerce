package com.ecommerce.dto;

import com.ecommerce.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {

    private Long              id;
    private OrderStatus       status;
    private double            totalAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime     createdAt;

    private List<OrderItemResponse> items;

    private String shippingAddress;
}