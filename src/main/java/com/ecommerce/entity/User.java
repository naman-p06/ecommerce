package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                // Enforced at the DB level — duplicate email throws DataIntegrityViolationException
                // which GlobalExceptionHandler maps to 409 Conflict
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public enum Role {
        USER,
        ADMIN
    }
}