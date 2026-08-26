package com.relations.user_profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class UserDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        private String role; // CUSTOMER, VENDOR, ADMIN

        // Customer Profile details
        private String firstName;
        private String lastName;
        private String phone;
        private String shippingAddress;
        private String city;
        private String state;
        private String zipCode;
        private String country;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String username;
        private String email;
        private String role;
        private CustomerProfileResponse profile;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerProfileResponse {
        private Long profileId;
        private String firstName;
        private String lastName;
        private String phone;
        private String shippingAddress;
        private String city;
        private String state;
        private String zipCode;
        private String country;
    }
}
