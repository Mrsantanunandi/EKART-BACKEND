package com.santanu.Spring_Security_Project.Service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.santanu.Spring_Security_Project.Model.*;
import com.santanu.Spring_Security_Project.dao.OrderRepository;
import com.santanu.Spring_Security_Project.dao.UserRepo;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

@Service
public class RazorpayService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderRepository orderRepository;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    @Transactional
    public Order createRazorpayOrder(Authentication authentication)
            throws RazorpayException {

        try {
            User user = userRepo.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Cart cart = cartService.getCart(authentication);

            if (cart == null || cart.getItems().isEmpty()) {
                throw new RuntimeException("Cart is empty");
            }

            BigDecimal subtotal = cart.getTotalPrice();
            if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Invalid cart total");
            }

            BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.05));
            BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(399)) > 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(40);

            BigDecimal total = subtotal.add(tax).add(shipping);

            System.out.println("Subtotal: " + subtotal);
            System.out.println("Tax: " + tax);
            System.out.println("Shipping: " + shipping);
            System.out.println("Total: " + total);

            OrderModel order = new OrderModel();
            order.setUser(user);
            order.setAmount(subtotal);
            order.setTax(tax);
            order.setShipping(shipping);
            order.setTotalAmount(total);
            order.setStatus(Status.PENDING);

            // Create NEW list
            List<OrderItem> orderItems = new ArrayList<>();

            for (CartItem item : cart.getItems()) {

                OrderItem orderItem = new OrderItem();

                orderItem.setProduct(item.getProduct());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setProductPrice(item.getProductPrice());
                orderItem.setSubTotal(item.getSubTotal());
                orderItem.setOrder(order);

                orderItems.add(orderItem);
            }

            order.setItems(orderItems);
            order = orderRepository.save(order);

            JSONObject options = new JSONObject();
            //System.out.println("Amount"+ total.multiply(BigDecimal.valueOf(100)).longValue());
            options.put("amount", total.multiply(BigDecimal.valueOf(100)).longValue());
            //System.out.println("Currency"+ "INR");
            options.put("currency", "INR");
            //System.out.println("receipt"+ "order_" + order.getId());
            options.put("receipt", "order_" + order.getId());

            Order razorpayOrder = razorpayClient.orders.create(options);

            order.setRazorpayOrderId(razorpayOrder.get("id"));
            orderRepository.save(order);

            return razorpayOrder;
        } catch (RazorpayException e) {
            e.printStackTrace();
            throw new RuntimeException("Razorpay order creation failed");
        }
    }

    @Transactional
    public OrderModel verifyAndUpdatePayment(
            RazorpayVerifyRequest request,
            Authentication authentication
    ) {

        User user = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        OrderModel order = orderRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized verification");
        }

        String data = request.getRazorpayOrderId() + "|" +
                request.getRazorpayPaymentId();

        String generatedSignature = hmacSha256(data, razorpaySecret);

        if (!MessageDigest.isEqual(
                generatedSignature.getBytes(StandardCharsets.UTF_8),
                request.getRazorpaySignature().getBytes(StandardCharsets.UTF_8))) {

            order.setStatus(Status.FAILED);
            orderRepository.save(order);
            throw new RuntimeException("Payment verification failed");
        }


        // Payment Success
        order.setRazorpayPaymentId(request.getRazorpayPaymentId());
        order.setRazorpaySignature(request.getRazorpaySignature());
        order.setStatus(Status.PAID);

        orderRepository.saveAndFlush(order);

        cartService.clearCart(authentication); // optional

        return order;
    }

    // ================= HMAC =================
    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec key =
                    new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                            "HmacSHA256");

            mac.init(key);

            byte[] raw =
                    mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : raw) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException("HMAC generation failed", e);
        }
    }

}
