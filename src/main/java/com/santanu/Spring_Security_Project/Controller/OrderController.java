package com.santanu.Spring_Security_Project.Controller;
import com.santanu.Spring_Security_Project.Model.OrderResponseDTO;
import com.santanu.Spring_Security_Project.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;
    @GetMapping("/my-order")
    public ResponseEntity<Map<String, Object>> GetMyOrder(Authentication authentication)
    {
        Map<String, Object> response = new HashMap<>();
        try{
            List<OrderResponseDTO> orderResponseDTO =orderService.getMyOrders(authentication);

            response.put("success", true);
            response.put("message", "Fetched User orders successfully");
            response.put("myOrder", orderResponseDTO);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    //Get UserOrder
    @GetMapping("/user-order/{userId}")
    public ResponseEntity<Map<String, Object>> getUserOrder(@PathVariable Integer userId,Authentication authentication)
    {
        Map<String, Object> response = new HashMap<>();
        try{
            List<OrderResponseDTO> orderResponseDTO =orderService.getUserOrder(userId,authentication);

            response.put("success", true);
            response.put("message", "Fetched User orders successfully");
            response.put("myOrder", orderResponseDTO);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    //Get All UserOrder
    @GetMapping("/allUser-order")
    public ResponseEntity<Map<String, Object>> getAllUserOrder(Authentication authentication)
    {
        Map<String, Object> response = new HashMap<>();
        try{
            List<OrderResponseDTO> orderResponseDTO =orderService.getAllUserOrder(authentication);
            response.put("success", true);
            response.put("message", "Fetched All User orders successfully");
            response.put("myOrder", orderResponseDTO);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    //Get SalesData --> Total Users,Total products,totalNoOfOrders,TotalAmount(PAID)
    // Sales Grp by 30 Days -> showing the 30days Panel & each date paid sells amount
    @GetMapping("/admin-panel")
    public ResponseEntity<Map<String, Object>> getAdminPanel(Authentication authentication)
    {
        Map<String, Object> response = new HashMap<>();
        try{
            Map<String,Object> myOrder = orderService.getAdminPanel(authentication);
            response.put("success", true);
            response.put("message", "Fetched Admin Panel successfully");
            response.put("myOrder", myOrder);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}
