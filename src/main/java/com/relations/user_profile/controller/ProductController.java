package com.relations.user_profile.controller;

import com.relations.user_profile.dto.ProductDTO;
import com.relations.user_profile.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDTO.Response> createProduct(@Valid @RequestBody ProductDTO.Request request) {
        ProductDTO.Response response = productService.createProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Bulk Upload Batch Endpoint for Sellers (e.g. uploading 10,000 products).
     * Tuned via hibernate.jdbc.batch_size=50 and periodic EntityManager flush() and clear().
     */
    @PostMapping("/batch")
    public ResponseEntity<List<ProductDTO.Response>> createBatchProducts(@RequestBody ProductDTO.BatchRequest batchRequest) {
        List<ProductDTO.Response> responses = productService.saveBatchProducts(batchRequest.getProducts());
        return new ResponseEntity<>(responses, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO.Response> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Paginated Product Search & Catalog Query.
     */
    @GetMapping
    public ResponseEntity<Page<ProductDTO.Response>> searchProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductDTO.Response> products = productService.searchProducts(
                categoryId, vendorId, minPrice, maxPrice, search, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Soft Delete Product Endpoint.
     * Triggers Hibernate @SQLDelete ("UPDATE product SET is_deleted = true WHERE id = ?").
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
