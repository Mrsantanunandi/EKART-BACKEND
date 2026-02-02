package com.santanu.Spring_Security_Project.Service;

import com.santanu.Spring_Security_Project.Model.Cart;
import com.santanu.Spring_Security_Project.Model.CartItem;
import com.santanu.Spring_Security_Project.Model.Product;
import com.santanu.Spring_Security_Project.Model.User;
import com.santanu.Spring_Security_Project.dao.CartItemRepo;
import com.santanu.Spring_Security_Project.dao.CartRepo;
import com.santanu.Spring_Security_Project.dao.ProductRepo;
import com.santanu.Spring_Security_Project.dao.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;

@Service
public class CartService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CartItemRepo cartItemRepo;

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private UserRepo userRepo;

    // Calculate cart total
    public BigDecimal calculateCartTotal(Cart cart) {
        BigDecimal total = cart.getItems().stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalPrice(total);
        return cartRepo.save(cart).getTotalPrice(); // save once
    }

    // Add product to cart
    public void addToCart(Long productId, Authentication authentication) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));

        if (product.getQuantity() == 0) {
            throw new RuntimeException("Product is Out Of Stock");
        }

        User user = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        // Create cart if not exists
        Cart cart = cartRepo.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setItems(new ArrayList<>());
            return cartRepo.save(newCart);
        });

        // Add product to cart
        CartItem cartItem = cartItemRepo.findByCartAndProduct(cart, product).orElseGet(() -> {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setProductPrice(product.getProductPrice());
            newItem.setQuantity(0);
            cart.getItems().add(newItem); // add to cart's item list
            return newItem;
        });
        //If cartItem exist then add +1
        cartItem.setQuantity(cartItem.getQuantity() + 1);
        cartItem.updateSubTotal();
        cartItemRepo.save(cartItem);

        // Update cart total
        calculateCartTotal(cart);
    }

    public Cart getCart(Authentication authentication) {
        if(authentication==null || !authentication.isAuthenticated())
        {
            throw new RuntimeException("User Must Be LoggedIn");
        }
        User user=userRepo.findByUsername(authentication.getName()).orElseThrow(()->
                new RuntimeException("User Not Found"));
        Cart cart = cartRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart Not Found"));

        // Sort items by ID to keep order consistent
        cart.getItems().sort(Comparator.comparing(CartItem::getId));
        calculateCartTotal(cart);
        return cartRepo.findByUser(user).orElseThrow(()->new RuntimeException("Cart not Found"));
    }

    public void updateCart(Long cartItemId, Authentication authentication,boolean type) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User must be logged in");
        }
        User user = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        Cart cart = cartRepo.findByUser(user).orElseThrow(
                ()->new RuntimeException("Cart Not Found"));

        CartItem cartItem = cartItemRepo
                .findByIdAndCart(cartItemId, cart)
                .orElseThrow(() -> new RuntimeException("Cart Item is Not Found"));

        //type - true ---> increase else decrease
        if(type)
        {
            cartItem.setQuantity(cartItem.getQuantity()+1);
        }
        else {
            if (cartItem.getQuantity() <= 1) {
                cartItemRepo.delete(cartItem);
                cart.getItems().remove(cartItem);
                calculateCartTotal(cart);
                return;
            }
            cartItem.setQuantity(cartItem.getQuantity() - 1);
        }
        cartItem.updateSubTotal();
        cartItemRepo.save(cartItem);

        // Update cart total
        calculateCartTotal(cart);
    }

    public void removeFromCart(Long cartItemId, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User must be logged in");
        }
        User user = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        Cart cart = cartRepo.findByUser(user).orElseThrow(
                ()->new RuntimeException("Cart Not Found"));

        CartItem cartItem = cartItemRepo
                .findByIdAndCart(cartItemId, cart)
                .orElseThrow(() -> new RuntimeException("Cart Item is Not Found"));

        cartItemRepo.delete(cartItem);
        cart.getItems().remove(cartItem);
        calculateCartTotal(cart);
    }

    public void clearCart(Authentication authentication)
    {
        User user = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartItemRepo.deleteAll(cart.getItems());

        cart.getItems().clear();

        cart.setTotalPrice(BigDecimal.ZERO);

        cartRepo.save(cart);
    }

}
