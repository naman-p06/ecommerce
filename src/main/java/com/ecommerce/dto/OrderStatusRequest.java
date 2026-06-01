package com.ecommerce.dto;

import com.ecommerce.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusRequest {

    @NotNull(message = "Order status is required")
    private OrderStatus status;
}