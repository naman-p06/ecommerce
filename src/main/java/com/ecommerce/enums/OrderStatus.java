package com.ecommerce.enums;

public enum OrderStatus {
    CREATED,     // Order saved, awaiting payment
    CONFIRMED,   // Payment succeeded, seller notified
    SHIPPED,     // Package dispatched
    DELIVERED,   // Customer received
    CANCELLED,   // Cancelled before shipping
    FAILED       // Payment failed
}