package com.ecommerce.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentService {

    // Structured result so callers get both outcome + a message
    // Easy to swap: replace the body of processPayment() with a real
    // Razorpay / Stripe SDK call — the interface to OrderService stays the same
    public record PaymentResult(boolean success, String message) {}

    public PaymentResult processPayment(double amount) {

        boolean success = Math.random() > 0.2;

        if (success) {
            log.info("Payment of ₹{} processed successfully", amount);
            return new PaymentResult(true, "Payment processed successfully");
        } else {
            log.warn("Payment of ₹{} failed", amount);
            return new PaymentResult(false, "Payment declined by gateway");
        }
    }
}