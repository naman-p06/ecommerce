package com.ecommerce.controller;

import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.UserRequest;
import com.ecommerce.dto.UserResponse;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService=userService;
    }


    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest userRequest){
            return ResponseEntity.ok(userService.register(userRequest));
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest loginRequest){
        return userService.login(loginRequest);
    }
}
