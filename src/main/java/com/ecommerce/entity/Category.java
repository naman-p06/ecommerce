package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "categories")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // slug is a URL-friendly version of name e.g. "Mobile Phones" → "mobile-phones"
    // Used in search URLs: /api/products/search?category=mobile-phones
    @Column(nullable = false, unique = true)
    private String slug;

    // mappedBy means Category is NOT the owner of the FK — Product holds category_id
    // cascade PERSIST/MERGE so saving a category cascades to products if needed
    // FetchType.LAZY: don't load all products just because you loaded a category
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products;
}