package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.PagedResponse;
import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(
            @Valid @RequestBody ProductRequest request) {

        ProductResponse product = productService.addProduct(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Product added successfully", product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<ProductResponse> paged =
                new PagedResponse<>(productService.getAllProducts(page, size));
        return ResponseEntity.ok(ApiResponse.ok("Products fetched successfully", paged));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Product fetched successfully", productService.findById(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @Valid @RequestBody ProductRequest request,
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.ok("Product updated successfully", productService.updateProduct(request, id)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam String keyword) {

        return ResponseEntity.ok(
                ApiResponse.ok("Search results", productService.searchProducts(keyword)));
    }
}