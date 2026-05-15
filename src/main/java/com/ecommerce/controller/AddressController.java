package com.ecommerce.controller;

import com.ecommerce.dto.AddressRequest;
import com.ecommerce.dto.AddressResponse;
import com.ecommerce.service.AddressService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users/addresses")
@AllArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
            @Valid @RequestBody AddressRequest request,
            Principal principal) {
        return ResponseEntity.ok(addressService.addAddress(principal.getName(), request));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses(Principal principal) {
        return ResponseEntity.ok(addressService.getUserAddresses(principal.getName()));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(
            @PathVariable Long addressId,
            Principal principal) {
        addressService.deleteAddress(principal.getName(), addressId);
        return ResponseEntity.ok("Address deleted");
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<AddressResponse> setDefault(
            @PathVariable Long addressId,
            Principal principal) {
        return ResponseEntity.ok(addressService.setDefault(principal.getName(), addressId));
    }
}