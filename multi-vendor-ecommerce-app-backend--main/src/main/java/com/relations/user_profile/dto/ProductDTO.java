package com.relations.user_profile.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

public class ProductDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "Product name is required")
        private String name;

        @NotBlank(message = "SKU is required")
        private String sku;

        private String description;

        @NotNull(message = "Price is required")
        @Min(value = 0, message = "Price must be non-negative")
        private BigDecimal price;

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity must be non-negative")
        private Integer stockQuantity;

        @NotNull(message = "Category ID is required")
        private Long categoryId;

        @NotNull(message = "Vendor ID is required")
        private Long vendorId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchRequest {
        private List<Request> products;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String sku;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
        private boolean isDeleted;
        private Long categoryId;
        private String categoryName;
        private Long vendorId;
        private String storeName;
    }
}
