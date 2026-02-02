package com.santanu.Spring_Security_Project.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {

        String link = "https://ekart-frontend-kappa.vercel.app/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("santanuedu2005@gmail.com"); // MUST be verified in Brevo
        message.setTo(to);
        message.setSubject("Please Verify Your Email");
        //Here You write some masg
        String emailBody = "Hello,\n\n" +
                "Thank you for registering! Please click the link below to verify your email address and activate your account:\n\n" +
                link + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not create an account, please ignore this email.\n\n" +
                "Best regards,\n" +
                "EKART Team";
        message.setText(emailBody);

        mailSender.send(message);
    }

    //logic for sent otp
    public void sendOtpEmail(String email, String otp) {
        SimpleMailMessage mailMessage=new SimpleMailMessage();
        mailMessage.setFrom("santanuedu2005@gmail.com");
        mailMessage.setTo(email);
        mailMessage.setSubject("OTP FOR RESET PASSWORD");
        mailMessage.setText("Never Share this OTP to anyone.This is Your Six Digit Otp: \n" + otp);
        mailSender.send(mailMessage);
    }
}



