package com.ecommerce.service;

import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.UserRequest;
import com.ecommerce.dto.UserResponse;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.UnauthorizedException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserResponse register(UserRequest userRequest) {

        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new BadRequestException("An account with email '"
                    + userRequest.getEmail() + "' already exists");
        }

        User user = new User();
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole(User.Role.USER);

        User savedUser = userRepository.save(user);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(savedUser.getId());
        userResponse.setName(savedUser.getName());
        userResponse.setEmail(savedUser.getEmail());
        return userResponse;
    }

    public String login(LoginRequest loginRequest) {


        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UnauthorizedException("No account found with this email"));

        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Incorrect password");
        }

        return jwtUtil.generateToken(loginRequest.getEmail(), user.getRole().name());
    }
}