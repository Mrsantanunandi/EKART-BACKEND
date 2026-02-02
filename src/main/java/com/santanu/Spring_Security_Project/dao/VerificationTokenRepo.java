package com.santanu.Spring_Security_Project.dao;

import com.santanu.Spring_Security_Project.Model.User;
import com.santanu.Spring_Security_Project.Model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface VerificationTokenRepo
        extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUser(User user);
}

