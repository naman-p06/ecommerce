package com.ecommerce.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private String userName;     // who wrote the review
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}