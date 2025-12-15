package com.umat.backend.admin;

import com.umat.backend.model.Order;
import com.umat.backend.repository.OrderRepository;
import com.umat.backend.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DashboardController(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long productCount = productRepository.count();
        long orderCount = orderRepository.count();
        
        // Calculate total revenue from paid orders by parsing cart items JSON
        Double totalRevenue = 0.0;
        try {
            List<Order> paidOrders = orderRepository.findAll()
                .stream()
                .filter(order -> "PAID".equals(order.getStatus()))
                .toList();
            
            for (Order order : paidOrders) {
                try {
                    // Parse cart items from JSON and calculate total
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> cartItems = objectMapper.readValue(
                        order.getCartItems(), 
                        List.class
                    );
                    
                    double orderTotal = 0.0;
                    for (Map<String, Object> item : cartItems) {
                        Double price = (Double) item.get("price");
                        Integer quantity = (Integer) item.get("quantity");
                        if (price != null && quantity != null) {
                            orderTotal += price * quantity;
                        }
                    }
                    totalRevenue += orderTotal;
                } catch (Exception e) {
                    // Skip orders with invalid cart data
                    System.err.println("Error parsing cart for order " + order.getId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error calculating revenue: " + e.getMessage());
        }
        
        stats.put("totalProducts", productCount);
        stats.put("totalOrders", orderCount);
        stats.put("totalRevenue", totalRevenue);
        
        return stats;
    }
}