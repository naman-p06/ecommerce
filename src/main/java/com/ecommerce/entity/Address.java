package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "addresses",
        indexes = {
                // Indexed because every "get my addresses" query filters by user_id
                @Index(name = "idx_address_user", columnList = "user_id")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Proper @ManyToOne instead of a raw Long userId field
    // One user can have many addresses (home, office, etc.)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String pincode;

    @Column(nullable = false)
    private String country;

    // Convenience flag — at checkout "use default address" pre-selects this one
    @Column(nullable = false)
    private boolean isDefault = false;
}