package com.ecommerce.service;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public boolean processPayment() {
        return Math.random() > 0.3;
    }
}
