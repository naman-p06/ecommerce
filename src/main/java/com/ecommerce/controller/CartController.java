package com.ecommerce.controller;

import com.ecommerce.dto.CartItemRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.service.CartService;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@AllArgsConstructor
public class CartController {

    private CartService cartService;

    @PostMapping
    public ResponseEntity<String> addToCart(
            @RequestBody CartItemRequest request,
            Authentication auth) {

        String email=auth.name(); // correct

        cartService.addToCart(email, request.getProductId(), request.getQuantity());

        return ResponseEntity.ok("Added to cart");
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication auth) {

        String email = auth.name();
        return ResponseEntity.ok(cartService.getCart(email));
    }

    @PutMapping
    public ResponseEntity<String> updateCart(
            @RequestBody CartItemRequest request,
            Authentication auth) {

        String email = auth.name();

        cartService.updateQuantity(email, request.getProductId(), request.getQuantity());

        return ResponseEntity.ok("Cart updated");
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<String> removeItem(
            @PathVariable Long productId,
            Authentication auth) {

        String email = auth.name();

        cartService.removeItem(email, productId);

        return ResponseEntity.ok("Item removed");
    }
}
