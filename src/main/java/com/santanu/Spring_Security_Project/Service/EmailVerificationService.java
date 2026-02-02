package com.santanu.Spring_Security_Project.Service;

import com.santanu.Spring_Security_Project.Model.User;
import com.santanu.Spring_Security_Project.Model.VerificationToken;
import com.santanu.Spring_Security_Project.dao.UserRepo;
import com.santanu.Spring_Security_Project.dao.VerificationTokenRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailVerificationService {

    @Autowired
    private VerificationTokenRepo verificationTokenRepo;

    @Autowired
    private UserRepo userRepo;
    public void verifyEmail(String token) {
        VerificationToken vt = verificationTokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token expired");
        }

        User user = vt.getUser();
        user.setEmailVerified(true);
        userRepo.save(user);//save the user as verified
        //After Verifying delete the token
        verificationTokenRepo.delete(vt);
    }
}
