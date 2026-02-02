package com.santanu.Spring_Security_Project.dao;

import com.santanu.Spring_Security_Project.Model.Cart;
import com.santanu.Spring_Security_Project.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart,Long> {
    Optional<Cart> findByUser(User loggedInuser);
}
