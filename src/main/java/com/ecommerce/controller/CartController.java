package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CartItemRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addToCart(
            @Valid @RequestBody CartItemRequest request,
            Principal principal) {

        cartService.addToCart(principal.getName(), request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(ApiResponse.ok("Item added to cart successfully"));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Principal principal) {
        CartResponse cart = cartService.getCart(principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Cart fetched successfully", cart));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateCart(
            @Valid @RequestBody CartItemRequest request,
            Principal principal) {

        cartService.updateQuantity(principal.getName(), request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(ApiResponse.ok("Cart updated successfully"));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable Long productId,
            Principal principal) {

        cartService.removeItem(principal.getName(), productId);
        return ResponseEntity.ok(ApiResponse.ok("Item removed from cart"));
    }
}