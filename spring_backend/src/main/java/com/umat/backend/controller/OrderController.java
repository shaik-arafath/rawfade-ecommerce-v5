package com.umat.backend.controller;

import com.umat.backend.model.Order;
import com.umat.backend.model.User;
import com.umat.backend.repository.OrderRepository;
import com.umat.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public OrderController(OrderRepository orderRepo, UserRepository userRepo) {
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
    }

    private Optional<User> getUserFromToken(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // For now, we'll use a simple approach - in production, validate JWT properly
            try {
                String email = org.springframework.util.StringUtils.hasText(token) ? "user@example.com" : null;
                if (email != null) {
                    return userRepo.findByEmail(email);
                }
            } catch (Exception e) {
                // Log error in production
            }
        }
        return Optional.empty();
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> orderData, HttpServletRequest req) {
        try {
            // Extract customer information
            String firstName = (String) orderData.get("firstName");
            String lastName = (String) orderData.get("lastName");
            String email = (String) orderData.get("email");
            String phone = (String) orderData.get("phone");
            String address = (String) orderData.get("address");
            String city = (String) orderData.get("city");
            String state = (String) orderData.get("state");
            String postalCode = (String) orderData.get("postalCode");
            String country = (String) orderData.get("country");
            String notes = (String) orderData.get("notes");
            Object cartItemsObj = orderData.get("cartItems");

            // Validate required fields
            if (firstName == null || lastName == null || email == null || phone == null ||
                address == null || city == null || state == null || postalCode == null || country == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            // Convert cart items to JSON string
            String cartItemsJson = objectMapper.writeValueAsString(cartItemsObj);

            // Create order
            Order order = new Order(firstName, lastName, email, phone, address, city, 
                                   state, postalCode, country, notes, cartItemsJson);

            // Associate with user if authenticated
            Optional<User> userOpt = getUserFromToken(req);
            if (userOpt.isPresent()) {
                // In a real implementation, you might want to associate the order with the user
                // For now, we'll just save the order without user association
            }

            // Save order to database
            Order savedOrder = orderRepo.save(order);

            // Return success response with order ID
            Map<String, Object> response = Map.of(
                "id", savedOrder.getId(),
                "message", "Order placed successfully",
                "status", savedOrder.getStatus()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creating order: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id, HttpServletRequest req) {
        Optional<Order> orderOpt = orderRepo.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orderOpt.get());
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(HttpServletRequest req) {
        // For admin purposes - you might want to add authentication check here
        return ResponseEntity.ok(orderRepo.findAll());
    }
}