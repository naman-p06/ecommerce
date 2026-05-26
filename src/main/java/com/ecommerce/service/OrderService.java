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
    private final UserRepository userRepository;


    @Transactional
    public Order createOrder(String email){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));

        Long userId=user.getId();

        Cart cart=cartRepository.findByUserId(userId).orElseThrow(()->new CustomException("cart not found"));

        List<CartItem> items=cartItemRepository.findByCartId(cart.getId());
        if(items.isEmpty()){
            throw new CustomException("cart is empty");
        }

        Order order=new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);

        order=orderRepository.save(order);
        double totalAmount=0;

        for(CartItem item: items){
            Product product=productRepository.findByIdWithLock(item.getProduct().getId());
            if(item.getQuantity()>product.getStock()){
                throw new RuntimeException("Insufficient stock for Product");
            }
            OrderItem orderItem=new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());

            orderItemRepository.save(orderItem);
            totalAmount += product.getPrice() * item.getQuantity();
        }
        order.setTotalAmount(totalAmount);
        orderRepository.save(order);

        return order;
    }

    @Transactional
    public void completeOrder(Order order,String email,boolean paymentSuccess){
        if(paymentSuccess){
            order.setStatus(OrderStatus.SUCCESS);

            User user=userRepository.findByEmail(email).orElseThrow(()->new CustomException("user not found"));
            Cart cart=cartRepository.findByUserId(user.getId()).orElseThrow(() -> new CustomException("Cart not found"));

            List<CartItem> items=cartItemRepository.findByCartId(cart.getId());
            for(CartItem item:items){
                Product product=productRepository.findByIdWithLock(item.getProduct().getId());
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

    public String placeOrder(String email) {

        Order order = createOrder(email);

        boolean paymentSuccess = paymentService.processPayment();

        completeOrder(order, email ,paymentSuccess);

        return paymentSuccess ? "Order Success" : "Order Failed";
    }
}
