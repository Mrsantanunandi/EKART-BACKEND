package com.santanu.Spring_Security_Project.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean emailVerified = false; // instead of primitive boolean

    // Cloudinary profile picture
    @Column(nullable = true)
    private String profileImageUrl;

    @Column(nullable = true)
    private String profileImagePublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Role role = Role.USER;   // DEFAULT USER

    @Column(nullable = true)
    @JsonIgnore
    private String otp;

    @Column(nullable = true)
    @JsonIgnore
    private LocalDateTime otpExpiry;

    private String address;

    private String city;

    private String zipCode;

    private String phoneNo;

}

