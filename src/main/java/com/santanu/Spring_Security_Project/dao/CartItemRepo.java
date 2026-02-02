package com.santanu.Spring_Security_Project.dao;

import com.santanu.Spring_Security_Project.Model.Cart;
import com.santanu.Spring_Security_Project.Model.CartItem;
import com.santanu.Spring_Security_Project.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem,Long> {


    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    Optional<CartItem> findByIdAndCart(Long cartItemId, Cart cart);
}
