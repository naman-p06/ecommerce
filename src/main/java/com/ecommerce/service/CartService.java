package com.ecommerce.service;

import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.User;
import com.ecommerce.exception.CustomException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CartService {
                 private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;


    public void addToCart(String email,Long ProductId,int quantity){


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));

        Long userId = user.getId();

        Cart cart=cartRepository.findByUserId(userId).orElseGet(
                ()->{
                    Cart newCart=new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                }
        );

        CartItem item=cartItemRepository.findByCartIdAndProductId(cart.getId(), ProductId).orElse(null);

        if(item!=null){
            item.setQuantity(item.getQuantity()+quantity);
        }
        else{
            item=new CartItem();
            item.setCartId(cart.getId());
            item.setQuantity(quantity);
            item.setProductId(ProductId);
        }
        cartItemRepository.save(item);
    }

    public CartResponse getCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));

        Long userId = user.getId();
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("Cart not found"));

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        List<CartItemResponse> responseItems = items.stream().map(item -> {
            CartItemResponse res = new CartItemResponse();
            res.setProductId(item.getProductId());
            res.setQuantity(item.getQuantity());
            return res;
        }).toList();

        CartResponse response = new CartResponse();
        response.setItems(responseItems);

        return response;
    }

    public void updateQuantity(String email, Long productId, int quantity) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));

        Long userId = user.getId();
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("Cart not found"));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CustomException("Item not found"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    public void removeItem(String email, Long productId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));

        Long userId = user.getId();
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("Cart not found"));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CustomException("Item not found"));

        cartItemRepository.delete(item);
    }
}
