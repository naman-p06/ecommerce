package com.ecommerce.service;

import com.ecommerce.dto.AddressRequest;
import com.ecommerce.dto.AddressResponse;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.User;
import com.ecommerce.exception.CustomException;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressResponse addAddress(String email, AddressRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));

        // If this address is being set as default, unset any existing default first
        if (request.isDefault()) {
            clearExistingDefault(user.getId());
        }

        Address address = new Address();
        address.setUser(user);
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setCountry(request.getCountry());
        address.setDefault(request.isDefault());

        return toResponse(addressRepository.save(address));
    }

    public List<AddressResponse> getUserAddresses(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));
        return addressRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteAddress(String email, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new CustomException("Address not found"));

        // Security: users can only delete their own addresses
        if (!address.getUser().getEmail().equals(email)) {
            throw new CustomException("Unauthorized: cannot delete another user's address");
        }
        addressRepository.delete(address);
    }

    @Transactional
    public AddressResponse setDefault(String email, Long addressId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));

        clearExistingDefault(user.getId());

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new CustomException("Address not found"));

        if (!address.getUser().getEmail().equals(email)) {
            throw new CustomException("Unauthorized");
        }

        address.setDefault(true);
        return toResponse(addressRepository.save(address));
    }

    // Helper: set all current default addresses for user to false
    private void clearExistingDefault(Long userId) {
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(existing -> {
                    existing.setDefault(false);
                    addressRepository.save(existing);
                });
    }

    private AddressResponse toResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setStreet(address.getStreet());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setPincode(address.getPincode());
        response.setCountry(address.getCountry());
        response.setDefault(address.isDefault());
        return response;
    }
}