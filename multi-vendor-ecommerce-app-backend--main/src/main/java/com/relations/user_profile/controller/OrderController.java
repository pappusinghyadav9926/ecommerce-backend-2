package com.relations.user_profile.controller;

import com.relations.user_profile.dto.OrderDTO;
import com.relations.user_profile.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO.Response> createOrder(@Valid @RequestBody OrderDTO.Request request) {
        OrderDTO.Response response = orderService.createOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO.Response> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * N+1 Problem Solution Endpoint:
     * Uses JPQL JOIN FETCH to retrieve orders and line items in a single database roundtrip.
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDTO.Response>> getOrdersByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
    }

    /**
     * Orphan Removal Endpoint:
     * Removes an OrderItem from an existing Order, triggering Hibernate's orphanRemoval=true DELETE query.
     */
    @DeleteMapping("/{orderId}/items/{orderItemId}")
    public ResponseEntity<OrderDTO.Response> removeOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long orderItemId) {
        return ResponseEntity.ok(orderService.removeItemFromOrder(orderId, orderItemId));
    }
}
