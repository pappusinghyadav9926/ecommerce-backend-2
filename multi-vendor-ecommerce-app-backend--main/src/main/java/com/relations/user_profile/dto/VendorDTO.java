package com.relations.user_profile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class VendorDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "Store name is required")
        private String storeName;

        @NotBlank(message = "Seller code is required")
        private String sellerCode;

        private String contactEmail;
        private String phoneNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String storeName;
        private String sellerCode;
        private String contactEmail;
        private String phoneNumber;
        private Double rating;
        private int totalProducts;
    }
}
