package com.ecommerce.controller;

import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.LoginResponse;
import com.ecommerce.dto.UserRequest;
import com.ecommerce.dto.UserResponse;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest userRequest) {
        // 201 Created is more correct than 200 OK for a resource creation endpoint
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(userRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {

        String token = userService.login(loginRequest);
        return ResponseEntity.ok(new LoginResponse(token));
    }
}