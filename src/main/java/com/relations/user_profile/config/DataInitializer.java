package com.relations.user_profile.config;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.relations.user_profile.entity.Category;
import com.relations.user_profile.entity.CustomerProfile;
import com.relations.user_profile.entity.Order;
import com.relations.user_profile.entity.OrderItem;
import com.relations.user_profile.entity.Product;
import com.relations.user_profile.entity.User;
import com.relations.user_profile.entity.Vendor;
import com.relations.user_profile.repository.CategoryRepository;
import com.relations.user_profile.repository.OrderRepository;
import com.relations.user_profile.repository.ProductRepository;
import com.relations.user_profile.repository.UserRepository;
import com.relations.user_profile.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            // Data already initialized
            return;
        }

        // 1. Create Indian Customers & Profiles (@OneToOne)
        User user1 = User.builder()
                .username("rahul_sharma")
                .email("rahul.sharma@example.in")
                .password("Password@123")
                .role("CUSTOMER")
                .build();
        CustomerProfile profile1 = CustomerProfile.builder()
                .firstName("Rahul")
                .lastName("Sharma")
                .phone("+91 98765 43210")
                .shippingAddress("102, Sunrise Apartments, MG Road, Indiranagar")
                .city("Bengaluru")
                .state("Karnataka")
                .zipCode("560038")
                .country("India")
                .build();
        user1.setCustomerProfile(profile1);
        User savedUser1 = userRepository.save(user1);

        User user2 = User.builder()
                .username("priya_patel")
                .email("priya.patel@example.in")
                .password("Password@123")
                .role("CUSTOMER")
                .build();
        CustomerProfile profile2 = CustomerProfile.builder()
                .firstName("Priya")
                .lastName("Patel")
                .phone("+91 98123 45678")
                .shippingAddress("45, Shanti Nagar, CG Road, Navrangpura")
                .city("Ahmedabad")
                .state("Gujarat")
                .zipCode("380009")
                .country("India")
                .build();
        user2.setCustomerProfile(profile2);
        User savedUser2 = userRepository.save(user2);

        User user3 = User.builder()
                .username("amit_verma")
                .email("amit.verma@example.in")
                .password("Password@123")
                .role("CUSTOMER")
                .build();
        CustomerProfile profile3 = CustomerProfile.builder()
                .firstName("Amit")
                .lastName("Verma")
                .phone("+91 99887 76655")
                .shippingAddress("B-12, Block Inner Circle, Connaught Place")
                .city("New Delhi")
                .state("Delhi")
                .zipCode("110001")
                .country("India")
                .build();
        user3.setCustomerProfile(profile3);
        User savedUser3 = userRepository.save(user3);

        // 2. Create Indian Vendors
        Vendor vendor1 = vendorRepository.save(Vendor.builder()
                .storeName("Bharat Electronics Ltd")
                .sellerCode("VEND-IND-001")
                .contactEmail("sales@bharatelectronics.in")
                .phoneNumber("+91 80222 11100")
                .rating(4.8)
                .build());

        Vendor vendor2 = vendorRepository.save(Vendor.builder()
                .storeName("Jaipur Ethnic Crafts")
                .sellerCode("VEND-IND-002")
                .contactEmail("support@jaipurethnic.in")
                .phoneNumber("+91 14125 44332")
                .rating(4.9)
                .build());

        Vendor vendor3 = vendorRepository.save(Vendor.builder()
                .storeName("Desi Home & Spices")
                .sellerCode("VEND-IND-003")
                .contactEmail("contact@desihome.in")
                .phoneNumber("+91 11456 78900")
                .rating(4.7)
                .build());

        // 3. Create Product Categories
        Category catElectronics = categoryRepository.save(Category.builder()
                .name("Electronics & Mobile Accessories")
                .description("Smartphones, TWS earbuds, fast chargers & gadgets")
                .build());

        Category catFashion = categoryRepository.save(Category.builder()
                .name("Indian Ethnic Wear & Fashion")
                .description("Handloom Sarees, Chikankari Kurtas, Silk Shawls & Dupattas")
                .build());

        Category catHome = categoryRepository.save(Category.builder()
                .name("Home, Kitchen & Puja Essentials")
                .description("Brass Diyas, Stainless Steel Cookers, Copper Bottles & Decor")
                .build());

        Category catSpices = categoryRepository.save(Category.builder()
                .name("Organic Spices & Foods")
                .description("Kashmiri Saffron, Organic Turmeric, Darjeeling Tea & Dry Fruits")
                .build());

        // 4. Create Indian E-Commerce Products (Prices in INR ₹)
        Product p1 = productRepository.save(Product.builder()
                .name("boAt Airdopes 141 TWS Earbuds (Bold Black)")
                .sku("SKU-BOAT-AD141")
                .description("42H Playtime, Beast Mode 80ms Latency, ENx Tech, IPX4 Water Resistance")
                .price(new BigDecimal("1299.00"))
                .stockQuantity(150)
                .category(catElectronics)
                .vendor(vendor1)
                .isDeleted(false)
                .build());

        Product p2 = productRepository.save(Product.builder()
                .name("OnePlus Nord CE 3 Lite 5G (Pastel Lime, 8GB RAM, 128GB Storage)")
                .sku("SKU-1PLUS-NORD3")
                .description("108 MP Main Camera, 67W SUPERVOOC Fast Charging, 5000 mAh Battery")
                .price(new BigDecimal("17999.00"))
                .stockQuantity(45)
                .category(catElectronics)
                .vendor(vendor1)
                .isDeleted(false)
                .build());

        Product p3 = productRepository.save(Product.builder()
                .name("Pure Chanderi Silk Saree with Zari Woven Border")
                .sku("SKU-SILK-SAREE-01")
                .description("Authentic handwoven Silk Saree with matching blouse piece from MP weavers")
                .price(new BigDecimal("3499.00"))
                .stockQuantity(30)
                .category(catFashion)
                .vendor(vendor2)
                .isDeleted(false)
                .build());

        Product p4 = productRepository.save(Product.builder()
                .name("Men's Cotton Lucknawi Chikankari Kurta Set (Royal White)")
                .sku("SKU-KURTA-WHITE-M")
                .description("Handcrafted Chikankari embroidery on breathable premium cotton fabric")
                .price(new BigDecimal("1899.00"))
                .stockQuantity(60)
                .category(catFashion)
                .vendor(vendor2)
                .isDeleted(false)
                .build());

        Product p5 = productRepository.save(Product.builder()
                .name("Prestige Deluxe Alpha Stainless Steel Pressure Cooker 3L")
                .sku("SKU-COOKER-PRESTIGE-3L")
                .description("Alpha base compatible with Gas & Induction, durable precision weight valve")
                .price(new BigDecimal("2150.00"))
                .stockQuantity(80)
                .category(catHome)
                .vendor(vendor3)
                .isDeleted(false)
                .build());

        Product p6 = productRepository.save(Product.builder()
                .name("Pure Copper Water Bottle 1000ml (Hammered Antique Finish)")
                .sku("SKU-COPPER-BTL-1L")
                .description("100% Ayurvedic Pure Copper Bottle, leakproof cap, jointless construction")
                .price(new BigDecimal("799.00"))
                .stockQuantity(120)
                .category(catHome)
                .vendor(vendor3)
                .isDeleted(false)
                .build());

        Product p7 = productRepository.save(Product.builder()
                .name("Organic Kashmir Saffron / Kesar 1 Gram Pack")
                .sku("SKU-KESAR-KASHMIR-1G")
                .description("GI Tagged Grade A1 Original Saffron threads for Milk, Biryani & Sweets")
                .price(new BigDecimal("499.00"))
                .stockQuantity(200)
                .category(catSpices)
                .vendor(vendor3)
                .isDeleted(false)
                .build());

        // 5. Create Initial Sample Orders
        Order order1 = Order.builder()
                .orderNumber("ORD-IND-1001")
                .customer(savedUser1.getCustomerProfile())
                .status("PROCESSING")
                .orderItems(new ArrayList<>())
                .build();

        OrderItem item1 = OrderItem.builder()
                .product(p1)
                .quantity(2)
                .unitPrice(p1.getPrice())
                .subtotal(p1.getPrice().multiply(new BigDecimal("2")))
                .build();

        OrderItem item2 = OrderItem.builder()
                .product(p6)
                .quantity(1)
                .unitPrice(p6.getPrice())
                .subtotal(p6.getPrice())
                .build();

        order1.addOrderItem(item1);
        order1.addOrderItem(item2);
        order1.setTotalAmount(item1.getSubtotal().add(item2.getSubtotal()));
        orderRepository.save(order1);

        Order order2 = Order.builder()
                .orderNumber("ORD-IND-1002")
                .customer(savedUser2.getCustomerProfile())
                .status("SHIPPED")
                .orderItems(new ArrayList<>())
                .build();

        OrderItem item3 = OrderItem.builder()
                .product(p3)
                .quantity(1)
                .unitPrice(p3.getPrice())
                .subtotal(p3.getPrice())
                .build();

        order2.addOrderItem(item3);
        order2.setTotalAmount(item3.getSubtotal());
        orderRepository.save(order2);
    }
}
