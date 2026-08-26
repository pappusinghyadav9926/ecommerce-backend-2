package com.relations.user_profile.service;

import com.relations.user_profile.dto.UserDTO;
import com.relations.user_profile.entity.CustomerProfile;
import com.relations.user_profile.entity.User;
import com.relations.user_profile.exception.ResourceNotFoundException;
import com.relations.user_profile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDTO.Response createUser(UserDTO.Request request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(request.getRole() != null ? request.getRole() : "CUSTOMER")
                .build();

        CustomerProfile profile = CustomerProfile.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .shippingAddress(request.getShippingAddress())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .build();

        user.setCustomerProfile(profile);

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDTO.Response getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    public static UserDTO.Response mapToResponse(User user) {
        UserDTO.CustomerProfileResponse profileResp = null;
        if (user.getCustomerProfile() != null) {
            CustomerProfile p = user.getCustomerProfile();
            profileResp = UserDTO.CustomerProfileResponse.builder()
                    .profileId(p.getId())
                    .firstName(p.getFirstName())
                    .lastName(p.getLastName())
                    .phone(p.getPhone())
                    .shippingAddress(p.getShippingAddress())
                    .city(p.getCity())
                    .state(p.getState())
                    .zipCode(p.getZipCode())
                    .country(p.getCountry())
                    .build();
        }

        return UserDTO.Response.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .profile(profileResp)
                .build();
    }
}
