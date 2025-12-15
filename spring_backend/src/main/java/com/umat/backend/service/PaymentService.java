package com.umat.backend.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    @Value("${razorpay.key}")
    private String keyId;

    @Value("${razorpay.secret}")
    private String keySecret;

    public Order createRazorpayOrder(int amount) throws RazorpayException {
        RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", UUID.randomUUID().toString());

        return razorpay.orders.create(orderRequest);
    }
}