package com.relations.user_profile.service;

import com.relations.user_profile.dto.ProductDTO;
import com.relations.user_profile.entity.Category;
import com.relations.user_profile.entity.Product;
import com.relations.user_profile.entity.Vendor;
import com.relations.user_profile.exception.ResourceNotFoundException;
import com.relations.user_profile.repository.CategoryRepository;
import com.relations.user_profile.repository.ProductRepository;
import com.relations.user_profile.repository.VendorRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final VendorRepository vendorRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size:50}")
    private int batchSize;

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO.Response createProduct(ProductDTO.Request request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + request.getVendorId()));

        Product product = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .category(category)
                .vendor(vendor)
                .isDeleted(false)
                .build();

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    /**
     * Optimized Batch Processing for Bulk Inventory Updates (e.g., 10,000 products uploaded by sellers).
     * Leverages hibernate.jdbc.batch_size and periodically invokes flush() and clear()
     * to manage memory efficiently and dispatch batched SQL INSERT statements.
     */
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public List<ProductDTO.Response> saveBatchProducts(List<ProductDTO.Request> requests) {
        List<ProductDTO.Response> responses = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            ProductDTO.Request req = requests.get(i);
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + req.getCategoryId()));
            Vendor vendor = vendorRepository.findById(req.getVendorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + req.getVendorId()));

            Product product = Product.builder()
                    .name(req.getName())
                    .sku(req.getSku())
                    .description(req.getDescription())
                    .price(req.getPrice())
                    .stockQuantity(req.getStockQuantity())
                    .category(category)
                    .vendor(vendor)
                    .isDeleted(false)
                    .build();

            entityManager.persist(product);
            responses.add(mapToResponse(product));

            if ((i + 1) % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
        return responses;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductDTO.Response getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> searchProducts(
            Long categoryId, Long vendorId, BigDecimal minPrice, BigDecimal maxPrice, String search, Pageable pageable) {
        return productRepository.searchProducts(categoryId, vendorId, minPrice, maxPrice, search, pageable)
                .map(ProductService::mapToResponse);
    }

    /**
     * Soft Delete Feature: Calling repository.deleteById(id) executes Hibernate's @SQLDelete:
     * "UPDATE product SET is_deleted = true WHERE id = ?"
     * Historic order records referencing this product remain intact in database!
     */
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    public static ProductDTO.Response mapToResponse(Product product) {
        return ProductDTO.Response.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .isDeleted(product.isDeleted())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .vendorId(product.getVendor() != null ? product.getVendor().getId() : null)
                .storeName(product.getVendor() != null ? product.getVendor().getStoreName() : null)
                .build();
    }
}
