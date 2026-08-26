package com.relations.user_profile.service;

import com.relations.user_profile.dto.CartDTO;
import com.relations.user_profile.dto.ProductDTO;
import com.relations.user_profile.entity.Cart;
import com.relations.user_profile.entity.CustomerProfile;
import com.relations.user_profile.entity.Product;
import com.relations.user_profile.exception.ResourceNotFoundException;
import com.relations.user_profile.repository.CartRepository;
import com.relations.user_profile.repository.CustomerProfileRepository;
import com.relations.user_profile.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartDTO.Response addProductToCart(Long customerProfileId, Long productId) {
        CustomerProfile profile = customerProfileRepository.findById(customerProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found with id: " + customerProfileId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Cart cart = cartRepository.findByCustomerProfileId(customerProfileId)
                .orElseGet(() -> Cart.builder().customerProfile(profile).build());

        // Bidirectional ManyToMany maintenance
        cart.getProducts().add(product);
        product.getCarts().add(cart);

        Cart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public CartDTO.Response getCartByCustomerProfileId(Long customerProfileId) {
        Cart cart = cartRepository.findByCustomerProfileId(customerProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for customer profile id: " + customerProfileId));
        return mapToResponse(cart);
    }

    @Transactional
    public CartDTO.Response removeProductFromCart(Long customerProfileId, Long productId) {
        Cart cart = cartRepository.findByCustomerProfileId(customerProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for customer profile id: " + customerProfileId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        cart.getProducts().remove(product);
        product.getCarts().remove(cart);

        Cart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    private CartDTO.Response mapToResponse(Cart cart) {
        return CartDTO.Response.builder()
                .cartId(cart.getId())
                .customerProfileId(cart.getCustomerProfile() != null ? cart.getCustomerProfile().getId() : null)
                .products(cart.getProducts().stream().map(ProductService::mapToResponse).collect(Collectors.toList()))
                .totalItems(cart.getProducts().size())
                .build();
    }
}
