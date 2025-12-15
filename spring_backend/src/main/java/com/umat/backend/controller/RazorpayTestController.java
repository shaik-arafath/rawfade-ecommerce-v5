package com.umat.backend.controller;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class RazorpayTestController {
    
    @Value("${razorpay.key}")
    private String razorpayKeyId;
    
    @Value("${razorpay.secret}")
    private String razorpayKeySecret;
    
    @GetMapping("/razorpay-config")
    public ResponseEntity<?> testRazorpayConfig() {
        try {
            // Try to create a Razorpay client; constructor will throw if config is invalid
            new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            // If we reach here, the configuration is correct
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Razorpay configuration OK: key loaded successfully");
            response.put("key_id", razorpayKeyId);
            
            return ResponseEntity.ok().body(response);
        } catch (RazorpayException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Razorpay configuration failed: " + e.getMessage());
            response.put("key_id", razorpayKeyId);
            response.put("key_secret_length", razorpayKeySecret != null ? razorpayKeySecret.length() : 0);
            
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Unexpected error: " + e.getMessage());
            response.put("key_id", razorpayKeyId != null ? "Present" : "Missing");
            response.put("key_secret", razorpayKeySecret != null ? "Present" : "Missing");
            
            return ResponseEntity.status(500).body(response);
        }
    }
}