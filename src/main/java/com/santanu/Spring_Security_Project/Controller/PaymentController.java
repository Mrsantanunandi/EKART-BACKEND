package com.santanu.Spring_Security_Project.Controller;

import com.razorpay.Order;
import com.santanu.Spring_Security_Project.Model.OrderModel;
import com.santanu.Spring_Security_Project.Model.RazorpayVerifyRequest;
import com.santanu.Spring_Security_Project.Service.RazorpayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private RazorpayService razorpayService;

    // ✅ Create Razorpay Order
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order order = razorpayService.createRazorpayOrder(authentication);

            Map<String, Object> razorpayOrderMap =
                    order.toJson().toMap();
            response.put("success", true);
            response.put("message", "Razorpay order created successfully");
            response.put("order", razorpayOrderMap);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    //Now It's time to verify Payment
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody RazorpayVerifyRequest request,
            Authentication authentication
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            OrderModel order = razorpayService
                    .verifyAndUpdatePayment(request, authentication);

            response.put("success", true);
            response.put("message", "Payment verified successfully");
            response.put("order", order);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


}
