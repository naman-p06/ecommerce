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
    public OrderResponse placeOrder(String email, OrderRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Cart not found"));

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        if (items.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Address shippingAddress = resolveShippingAddress(user, request);

        // ── Step 1: Validate stock for ALL items before touching anything ──
        // Lock all products upfront to prevent phantom reads between validation
        // and deduction. Also computes total so we don't loop twice.
        double totalAmount = 0;
        for (CartItem item : items) {
            Product product = productRepository.findByIdWithLock(item.getProduct().getId());
            if (item.getQuantity() > product.getStock()) {
                throw new BadRequestException(
                        "Insufficient stock for '" + product.getName() +
                                "'. Available: " + product.getStock() +
                                ", Requested: " + item.getQuantity()
                );
            }
            totalAmount += product.getPrice() * item.getQuantity();
        }

        // ── Step 2: Create order record ────────────────────────────────────
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);
        order.setShippingAddress(shippingAddress);
        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        // ── Step 3: Process payment BEFORE touching stock ──────────────────
        // If payment fails here, no stock was deducted — nothing to roll back.
        PaymentService.PaymentResult paymentResult = paymentService.processPayment(totalAmount);

        if (!paymentResult.success()) {
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            log.warn("Order {} failed for user {}. Reason: {}", order.getId(), email, paymentResult.message());
            return toResponse(order);
        }

        // ── Step 4: Payment succeeded — now deduct stock and create items ──
        // At this point we're committed. Any exception here rolls back the
        // entire @Transactional, including the order save above.
        for (CartItem item : items) {
            Product product = productRepository.findByIdWithLock(item.getProduct().getId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            orderItemRepository.save(orderItem);

            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);

            order.getItems().add(orderItem);
        }

        order.setStatus(OrderStatus.CONFIRMED);
        cart.getItems().clear();
        cartRepository.save(cart);
        orderRepository.save(order);

        log.info("Order {} placed successfully for user {}", order.getId(), email);
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
