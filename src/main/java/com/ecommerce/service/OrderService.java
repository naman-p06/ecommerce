package com.ecommerce.service;


import com.ecommerce.entity.*;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.exception.CustomException;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;

    @Transactional
    public Order createOrder(Long userId){
        Cart cart=cartRepository.findByUserId(userId).orElseThrow(()->new CustomException("cart not found"));
        List<CartItem> items=cartItemRepository.findByCartId(cart.getId());
        if(items.isEmpty()){
            throw new CustomException("cart is empty");
        }

        Order order=new Order();
        order.setUserId(userId);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);

        order=orderRepository.save(order);
        double totalAmount=0;
        for(CartItem item: items){
            Product product=productRepository.findByIdWithLock(item.getProductId());
            if(item.getQuantity()>product.getStock()){
                throw new RuntimeException("Insufficient stock for Product");
            }
            OrderItem orderItem=new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getPrice());

            orderItemRepository.save(orderItem);

            // Step 4: Update total
            totalAmount += product.getPrice() * item.getQuantity();
//            product.setStock(product.getStock()- item.getQuantity());
//            productRepository.save(product);
        }
        order.setTotalAmount(totalAmount);
        orderRepository.save(order);

        return order;
    }

    @Transactional
    public void completeOrder(Order order,Long userId,boolean paymentSuccess){
        if(paymentSuccess){
            order.setStatus(OrderStatus.SUCCESS);
            Cart cart=cartRepository.findByUserId(userId).get();
            List<CartItem> items=cartItemRepository.findByCartId(cart.getId());
            for(CartItem item:items){
                Product product=productRepository.findByIdWithLock(item.getProductId());
                product.setStock(product.getStock()- item.getQuantity());
                productRepository.save(product);
            }
            cart.getItems().clear();
            cartRepository.save(cart);
        }
        else{
            order.setStatus(OrderStatus.FAILED);
        }
        orderRepository.save(order);
    }

    public String placeOrder(Long userId) {

        Order order = createOrder(userId);

        boolean paymentSuccess = paymentService.processPayment();

        completeOrder(order, userId,paymentSuccess);

        return paymentSuccess ? "Order Success" : "Order Failed";
    }
}
