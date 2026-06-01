package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.OrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.dto.OrderStatusRequest;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @RequestBody OrderRequest request,
            Principal principal) {

        OrderResponse order = orderService.placeOrder(principal.getName(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Order placed successfully", order));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrderHistory(
            Principal principal) {

        List<OrderResponse> orders = orderService.getOrderHistory(principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Order history fetched", orders));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable Long id,
            Principal principal) {

        OrderResponse order = orderService.getOrderById(principal.getName(), id);
        return ResponseEntity.ok(ApiResponse.ok("Order fetched", order));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusRequest request) {

        OrderResponse updated = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.ok("Order status updated to " + request.getStatus(), updated));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(
            @PathVariable OrderStatus status) {


        return ResponseEntity.ok(
                ApiResponse.ok("Orders with status " + status,
                        orderService.getOrderHistory("admin")) // placeholder — proper admin query in repo
        );
    }
}