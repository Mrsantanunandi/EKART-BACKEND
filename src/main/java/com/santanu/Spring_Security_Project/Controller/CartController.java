package com.santanu.Spring_Security_Project.Controller;

import com.santanu.Spring_Security_Project.Model.Cart;
import com.santanu.Spring_Security_Project.Service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/cart")
@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    //AddToCart
    @PostMapping("/add/{productId}")
    public ResponseEntity<Map<String,Object>> addToCart(@PathVariable Long productId, Authentication authentication)
    {
        Map<String,Object> response=new HashMap<>();
        try
        {
            cartService.addToCart(productId,authentication);
            response.put("success",true);
            response.put("message","Product Added To Cart Successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success",false);
            response.put("message",e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    //GetUserCart
    @GetMapping("/cartDetails")
    public ResponseEntity<Map<String,Object>> getCart(Authentication authentication)
    {
        Map<String,Object> response=new HashMap<>();
        try
        {
            Cart cart=cartService.getCart(authentication);
            response.put("success",true);
            response.put("message","Fetched Cart Items Successfully");
            response.put("cart",cart);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success",false);
            response.put("message",e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    //UpdateQuantity---------------------------------------------------------------------
    @PutMapping("/update-quantity/{cartItemId}/{type}")
    public ResponseEntity<Map<String,Object>> updateCart(@PathVariable Long cartItemId,
                                                         @PathVariable boolean type,
                                                         Authentication authentication)
    {
        Map<String,Object> response=new HashMap<>();
        try
        {
            cartService.updateCart(cartItemId,authentication,type);
            response.put("success",true);
            response.put("message","Updated Cart Successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success",false);
            response.put("message",e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    //Remove from Cart
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<Map<String,Object>> removeFromCart(@PathVariable Long cartItemId,Authentication authentication)
    {
        Map<String,Object> response=new HashMap<>();
        try
        {
            cartService.removeFromCart(cartItemId,authentication);
            response.put("success",true);
            response.put("message","Product removed from Cart Successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success",false);
            response.put("message",e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }



}
