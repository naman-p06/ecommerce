package com.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    @NotBlank(message = "Name is required")
    @Size(min=2,max=50,message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank
    @Email
    private String email;

    @Size(min = 8,message = "Password must be at least 8 characters")
    private String password;
}
