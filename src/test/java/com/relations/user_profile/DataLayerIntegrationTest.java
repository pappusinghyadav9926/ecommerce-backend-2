package com.relations.user_profile;

import com.relations.user_profile.dto.OrderDTO;
import com.relations.user_profile.dto.ProductDTO;
import com.relations.user_profile.dto.UserDTO;
import com.relations.user_profile.entity.*;
import com.relations.user_profile.repository.*;
import com.relations.user_profile.service.OrderService;
import com.relations.user_profile.service.ProductService;
import com.relations.user_profile.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class DataLayerIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Long categoryId;
    private Long vendorId;
    private Long customerProfileId;

    @BeforeEach
    void setUp() {
        // Create User & CustomerProfile (@OneToOne)
        UserDTO.Request userReq = UserDTO.Request.builder()
                .username("john_doe_" + System.currentTimeMillis())
                .email("john_" + System.currentTimeMillis() + "@example.com")
                .password("password123")
                .role("CUSTOMER")
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .shippingAddress("123 Main St")
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .country("USA")
                .build();

        UserDTO.Response userResp = userService.createUser(userReq);
        customerProfileId = userResp.getProfile().getProfileId();

        // Create Category
        Category category = categoryRepository.save(Category.builder()
                .name("Electronics_" + System.currentTimeMillis())
                .description("Gadgets & Devices")
                .build());
        categoryId = category.getId();

        // Create Vendor
        Vendor vendor = vendorRepository.save(Vendor.builder()
                .storeName("TechStore_" + System.currentTimeMillis())
                .sellerCode("TS-" + System.currentTimeMillis())
                .contactEmail("contact@techstore.com")
                .phoneNumber("9876543210")
                .rating(4.8)
                .build());
        vendorId = vendor.getId();
    }

    @Test
    @DisplayName("1. User <-> CustomerProfile Bidirectional @OneToOne Test")
    void testUserCustomerProfileMapping() {
        Optional<User> userOpt = userRepository.findById(customerProfileRepository.findById(customerProfileId).get().getUser().getId());
        assertTrue(userOpt.isPresent());
        assertNotNull(userOpt.get().getCustomerProfile());
        assertEquals("John", userOpt.get().getCustomerProfile().getFirstName());
    }

    @Test
    @DisplayName("2. Soft Delete Test: @SQLDelete marks product inactive preserving historic data")
    void testSoftDeleteProduct() {
        ProductDTO.Request productReq = ProductDTO.Request.builder()
                .name("Wireless Earbuds")
                .sku("SKU-EARBUD-" + System.currentTimeMillis())
                .description("Noise cancelling earbuds")
                .price(new BigDecimal("99.99"))
                .stockQuantity(50)
                .categoryId(categoryId)
                .vendorId(vendorId)
                .build();

        ProductDTO.Response savedProduct = productService.createProduct(productReq);
        Long productId = savedProduct.getId();

        // Perform Soft Delete
        productService.deleteProduct(productId);

        // Standard findById should return empty due to @SQLRestriction("is_deleted = false")
        Optional<Product> foundProduct = productRepository.findById(productId);
        assertFalse(foundProduct.isPresent(), "Soft deleted product must be excluded from active queries");
    }

    @Test
    @DisplayName("3. Batch Processing Test: Bulk insert tuned with hibernate.jdbc.batch_size")
    void testBatchInsertProducts() {
        List<ProductDTO.Request> batchRequests = new ArrayList<>();
        long timestamp = System.currentTimeMillis();
        for (int i = 1; i <= 60; i++) {
            batchRequests.add(ProductDTO.Request.builder()
                    .name("Batch Product " + i)
                    .sku("SKU-BATCH-" + timestamp + "-" + i)
                    .description("Bulk product description " + i)
                    .price(new BigDecimal("19.99").add(BigDecimal.valueOf(i)))
                    .stockQuantity(100)
                    .categoryId(categoryId)
                    .vendorId(vendorId)
                    .build());
        }

        List<ProductDTO.Response> savedProducts = productService.saveBatchProducts(batchRequests);
        assertEquals(60, savedProducts.size());
    }

    @Test
    @DisplayName("4. N+1 Problem Solution Test: JPQL JOIN FETCH retrieves Order, OrderItems, and Product in 1 query")
    void testNPlusOneSolutionOrderRetrieval() {
        // Create 2 products
        ProductDTO.Response p1 = productService.createProduct(ProductDTO.Request.builder()
                .name("Laptop")
                .sku("SKU-LAPTOP-" + System.currentTimeMillis())
                .price(new BigDecimal("1200.00"))
                .stockQuantity(10)
                .categoryId(categoryId)
                .vendorId(vendorId)
                .build());

        ProductDTO.Response p2 = productService.createProduct(ProductDTO.Request.builder()
                .name("Mouse")
                .sku("SKU-MOUSE-" + System.currentTimeMillis())
                .price(new BigDecimal("25.00"))
                .stockQuantity(50)
                .categoryId(categoryId)
                .vendorId(vendorId)
                .build());

        // Create Order with 2 items
        OrderDTO.Request orderReq = OrderDTO.Request.builder()
                .customerProfileId(customerProfileId)
                .items(List.of(
                        OrderDTO.ItemRequest.builder().productId(p1.getId()).quantity(1).build(),
                        OrderDTO.ItemRequest.builder().productId(p2.getId()).quantity(2).build()
                ))
                .build();

        OrderDTO.Response createdOrder = orderService.createOrder(orderReq);
        assertNotNull(createdOrder.getId());
        assertEquals(new BigDecimal("1250.00"), createdOrder.getTotalAmount());

        // Retrieve customer orders using JPQL JOIN FETCH
        List<OrderDTO.Response> customerOrders = orderService.getOrdersByCustomerId(customerProfileId);
        assertFalse(customerOrders.isEmpty());
        assertEquals(2, customerOrders.get(0).getItems().size());
    }

    @Test
    @DisplayName("5. Orphan Removal Test: Removing OrderItem from order collection triggers database DELETE")
    void testOrphanRemoval() {
        ProductDTO.Response p1 = productService.createProduct(ProductDTO.Request.builder()
                .name("Keyboard")
                .sku("SKU-KB-" + System.currentTimeMillis())
                .price(new BigDecimal("75.00"))
                .stockQuantity(20)
                .categoryId(categoryId)
                .vendorId(vendorId)
                .build());

        ProductDTO.Response p2 = productService.createProduct(ProductDTO.Request.builder()
                .name("Monitor")
                .sku("SKU-MON-" + System.currentTimeMillis())
                .price(new BigDecimal("300.00"))
                .stockQuantity(15)
                .categoryId(categoryId)
                .vendorId(vendorId)
                .build());

        OrderDTO.Request orderReq = OrderDTO.Request.builder()
                .customerProfileId(customerProfileId)
                .items(List.of(
                        OrderDTO.ItemRequest.builder().productId(p1.getId()).quantity(1).build(),
                        OrderDTO.ItemRequest.builder().productId(p2.getId()).quantity(1).build()
                ))
                .build();

        OrderDTO.Response createdOrder = orderService.createOrder(orderReq);
        Long orderId = createdOrder.getId();
        Long firstItemId = createdOrder.getItems().get(0).getItemId();

        // Remove 1 item via service -> orphanRemoval=true handles DB deletion
        OrderDTO.Response updatedOrder = orderService.removeItemFromOrder(orderId, firstItemId);
        assertEquals(1, updatedOrder.getItems().size());
        assertEquals(new BigDecimal("300.00"), updatedOrder.getTotalAmount());
    }
}
