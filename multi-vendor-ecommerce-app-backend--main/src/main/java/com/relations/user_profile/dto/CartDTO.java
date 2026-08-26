package com.relations.user_profile.dto;

import lombok.*;
import java.util.List;

public class CartDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddItemRequest {
        private Long customerProfileId;
        private Long productId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long cartId;
        private Long customerProfileId;
        private List<ProductDTO.Response> products;
        private int totalItems;
    }
}
