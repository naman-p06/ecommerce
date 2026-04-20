package com.ecommerce.service;

import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.UserRequest;
import com.ecommerce.dto.UserResponse;
import com.ecommerce.entity.User;
import com.ecommerce.exception.CustomException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder,JwtUtil jwtUtil){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
        this.jwtUtil=jwtUtil;
    }

    public UserResponse register(UserRequest userRequest){
        if(userRepository.findByEmail(userRequest.getEmail()).isPresent()){
            throw new CustomException("Email already exists");
        }
        User user=new User();
        user.setEmail(userRequest.getEmail());
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));  // will hash later
        user.setRole(User.Role.USER);
        User savedUser=userRepository.save(user);

        UserResponse userResponse=new UserResponse();
        userResponse.setEmail(savedUser.getEmail());
        userResponse.setId(savedUser.getId());
        userResponse.setName(savedUser.getName());
        return userResponse;
    }

    public String login(LoginRequest loginRequest){
        User user=userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(()->new RuntimeException("User Not Registered"));
        if(!passwordEncoder.matches( loginRequest.getPassword(),user.getPassword())){
            throw new CustomException("Wrong Credentials");
        }
        return jwtUtil.generateToken(loginRequest.getEmail(),user.getRole().name());
    }
}
