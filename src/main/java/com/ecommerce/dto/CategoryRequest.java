package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    // Slug is auto-generated in the service from name if not provided
    // e.g. "Mobile Phones" → "mobile-phones"
    private String slug;
}