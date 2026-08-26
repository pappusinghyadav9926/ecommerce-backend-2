package com.relations.user_profile.repository;

import com.relations.user_profile.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Solves the N+1 SELECT problem in order retrieval by using JPQL JOIN FETCH.
     * Fetches Order, OrderItems, and Product in a single SQL JOIN query instead of 1 + N queries.
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.orderItems oi JOIN FETCH oi.product WHERE o.customer.id = :customerId")
    List<Order> findByCustomerIdWithItemsAndProducts(@Param("customerId") Long customerId);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.orderItems oi JOIN FETCH oi.product WHERE o.id = :orderId")
    Optional<Order> findByIdWithItemsAndProducts(@Param("orderId") Long orderId);

    List<Order> findByCustomerId(Long customerId);
}
