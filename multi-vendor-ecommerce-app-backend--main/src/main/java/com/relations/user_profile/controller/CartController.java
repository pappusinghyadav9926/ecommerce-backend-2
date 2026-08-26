package com.relations.user_profile.controller;

import com.relations.user_profile.dto.CartDTO;
import com.relations.user_profile.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<CartDTO.Response> addProductToCart(@RequestBody CartDTO.AddItemRequest request) {
        CartDTO.Response response = cartService.addProductToCart(
                request.getCustomerProfileId(), request.getProductId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerProfileId}")
    public ResponseEntity<CartDTO.Response> getCartByCustomerProfileId(@PathVariable Long customerProfileId) {
        return ResponseEntity.ok(cartService.getCartByCustomerProfileId(customerProfileId));
    }

    @DeleteMapping("/items")
    public ResponseEntity<CartDTO.Response> removeProductFromCart(
            @RequestParam Long customerProfileId,
            @RequestParam Long productId) {
        return ResponseEntity.ok(cartService.removeProductFromCart(customerProfileId, productId));
    }
}
