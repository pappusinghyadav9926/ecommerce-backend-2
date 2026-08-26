package com.relations.user_profile.service;

import com.relations.user_profile.dto.OrderDTO;
import com.relations.user_profile.entity.CustomerProfile;
import com.relations.user_profile.entity.Order;
import com.relations.user_profile.entity.OrderItem;
import com.relations.user_profile.entity.Product;
import com.relations.user_profile.exception.ResourceNotFoundException;
import com.relations.user_profile.repository.CustomerProfileRepository;
import com.relations.user_profile.repository.OrderRepository;
import com.relations.user_profile.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderDTO.Response createOrder(OrderDTO.Request request) {
        CustomerProfile customer = customerProfileRepository.findById(request.getCustomerProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found with id: " + request.getCustomerProfileId()));

        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customer(customer)
                .status("PENDING")
                .totalAmount(BigDecimal.ZERO)
                .orderItems(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderDTO.ItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemReq.getProductId()));

            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            // Deduct stock quantity
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();

            // Bidirectional relationship mapping via helper method
            order.addOrderItem(orderItem);
        }

        order.setTotalAmount(total);

        // Saved with CascadeType.ALL - cascades persist to orderItems
        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    /**
     * Interview Challenge Solution: N+1 Problem Prevention in Order Retrieval.
     * Uses JPQL JOIN FETCH in orderRepository.findByCustomerIdWithItemsAndProducts(customerId)
     * which issues 1 single optimized SQL JOIN statement instead of 1 + N queries.
     */
    @Transactional(readOnly = true)
    public List<OrderDTO.Response> getOrdersByCustomerId(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerIdWithItemsAndProducts(customerId);
        return orders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDTO.Response getOrderById(Long orderId) {
        Order order = orderRepository.findByIdWithItemsAndProducts(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapToResponse(order);
    }

    /**
     * Showcase Orphan Removal:
     * Removing OrderItem from order.getOrderItems() collection triggers Hibernate
     * to execute an explicit SQL DELETE statement for that item.
     */
    @Transactional
    public OrderDTO.Response removeItemFromOrder(Long orderId, Long orderItemId) {
        Order order = orderRepository.findByIdWithItemsAndProducts(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderItem itemToRemove = order.getOrderItems().stream()
                .filter(item -> item.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem not found with id: " + orderItemId));

        // Subtract subtotal from order total amount
        order.setTotalAmount(order.getTotalAmount().subtract(itemToRemove.getSubtotal()));

        // Remove item from orderItems collection -> orphanRemoval=true handles database row deletion
        order.removeOrderItem(itemToRemove);

        Order updatedOrder = orderRepository.save(order);
        return mapToResponse(updatedOrder);
    }

    private OrderDTO.Response mapToResponse(Order order) {
        List<OrderDTO.ItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> OrderDTO.ItemResponse.builder()
                        .itemId(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productSku(item.getProduct().getSku())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        String customerName = order.getCustomer() != null ?
                (order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName()) : "Unknown";

        return OrderDTO.Response.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerProfileId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .customerName(customerName)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .items(itemResponses)
                .build();
    }
}
