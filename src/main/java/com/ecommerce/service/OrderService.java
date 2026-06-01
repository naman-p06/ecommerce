package com.ecommerce.service;


import com.ecommerce.dto.OrderItemResponse;
import com.ecommerce.dto.OrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.entity.*;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.CustomException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Transactional
    public OrderResponse placeOrder(String email, OrderRequest request){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long userId=user.getId();

        Cart cart=cartRepository.findByUserId(userId).orElseThrow(()->new BadRequestException("cart not found"));

        List<CartItem> items=cartItemRepository.findByCartId(cart.getId());
        if(items.isEmpty()){
            throw new BadRequestException("cart is empty");
        }

        Address shippingAddress=resolveShippingAddress(user,request);

        Order order=new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);
        order.setShippingAddress(shippingAddress);
        order=orderRepository.save(order);

        double totalAmount=0;

        for(CartItem item: items){
            Product product=productRepository.findByIdWithLock(item.getProduct().getId());
            if(item.getQuantity()>product.getStock()){
                throw new BadRequestException(
                        "Insufficient stock for '" + product.getName() +
                                "'. Available: " + product.getStock() +
                                ", Requested: " + item.getQuantity()
                );
            }
            OrderItem orderItem=new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());

            orderItemRepository.save(orderItem);

            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);

            order.getItems().add(orderItem);
            totalAmount += product.getPrice() * item.getQuantity();
        }
        order.setTotalAmount(totalAmount);

        PaymentService.PaymentResult paymentResult = paymentService.processPayment(totalAmount);

        if (paymentResult.success()) {
            order.setStatus(OrderStatus.CONFIRMED);
            cart.getItems().clear();
            cartRepository.save(cart);

            log.info("Order {} placed successfully for user {}", order.getId(), email);
        } else {
            order.setStatus(OrderStatus.FAILED);

            // Rollback stock — refund what was deducted
            for (CartItem cartItem : items) {
                Product product = productRepository
                        .findByIdWithLock(cartItem.getProduct().getId());
                product.setStock(product.getStock() + cartItem.getQuantity());
                productRepository.save(product);
            }
            log.warn("Order {} failed for user {}. Reason: {}", order.getId(), email, paymentResult.message());
        }

        orderRepository.save(order);
        return toResponse(order);
    }

    public List<OrderResponse> getOrderHistory(String email){
        User user=userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User not found"));

        return orderRepository.findByUserId(user.getId()).stream().map(this::toResponse).toList();
    }

    public OrderResponse getOrderById(String email, Long orderId){
        User user=userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // Guard: can't move a delivered or cancelled order to another state
        if (order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException(
                    "Cannot update a " + order.getStatus() + " order"
            );
        }

        order.setStatus(newStatus);
        return toResponse(orderRepository.save(order));
    }

    private Address resolveShippingAddress(User user, OrderRequest request) {
        if (request.getAddressId() != null) {
            // Validate the address exists AND belongs to this user
            return addressRepository.findByIdAndUserId(request.getAddressId(), user.getId())
                    .orElseThrow(() -> new UnauthorizedException(
                            "Address not found or does not belong to you"));
        }

        // Fall back to default address
        return addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .orElseThrow(() -> new BadRequestException(
                        "No default address found. Please provide an address ID or set a default address."));
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());

        if (order.getShippingAddress() != null) {
            Address addr = order.getShippingAddress();
            response.setShippingAddress(
                    addr.getStreet() + ", " + addr.getCity() +
                            " - " + addr.getPincode() + ", " + addr.getState()
            );
        }

        if (order.getItems() != null) {
            List<OrderItemResponse> itemResponses = order.getItems().stream().map(item -> {
                OrderItemResponse ir = new OrderItemResponse();
                ir.setProductId(item.getProduct().getId());
                ir.setProductName(item.getProduct().getName());
                ir.setQuantity(item.getQuantity());
                ir.setPriceAtPurchase(item.getPriceAtPurchase());
                ir.setLineTotal(item.getQuantity() * item.getPriceAtPurchase());
                return ir;
            }).toList();
            response.setItems(itemResponses);
        }

        return response;
    }
}
