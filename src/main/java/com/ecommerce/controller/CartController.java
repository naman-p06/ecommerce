package com.ecommerce.controller;

import com.ecommerce.dto.CartItemRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.service.CartService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
@AllArgsConstructor
public class CartController {

    private CartService cartService;

    // Principal is auto-populated by Spring Security from your JwtFilter.
    // principal.getName() returns the email you set as the subject in JwtUtil.generateToken().

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<String> addToCart(
            @RequestBody CartItemRequest request,
            Principal principal) {

        cartService.addToCart(principal.getName(), request.getProductId(), request.getQuantity());
        return ResponseEntity.ok("Added to cart");
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<CartResponse> getCart(Principal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getName()));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<String> updateCart(
            @RequestBody CartItemRequest request,
            Principal principal) {

        cartService.updateQuantity(principal.getName(), request.getProductId(), request.getQuantity());
        return ResponseEntity.ok("Cart updated");
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> removeItem(
            @PathVariable Long productId,
            Principal principal) {

        cartService.removeItem(principal.getName(), productId);
        return ResponseEntity.ok("Item removed");
    }
}