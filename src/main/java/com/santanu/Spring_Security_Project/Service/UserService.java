package com.santanu.Spring_Security_Project.Service;

import com.santanu.Spring_Security_Project.Model.*;
import com.santanu.Spring_Security_Project.dao.UserRepo;
import com.santanu.Spring_Security_Project.dao.VerificationTokenRepo;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private VerificationTokenRepo tokenRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CloudinaryService cloudinaryService;

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder(12);

    // REGISTER USER
    public void saveUser(User user) {

        if (userRepo.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepo.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username is already exist");
        }
        user.setRole(Role.USER);
        user.setPassword(encoder.encode(user.getPassword()));
        user.setEmailVerified(false);

        User savedUser = userRepo.save(user);

        String token = UUID.randomUUID().toString();

        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(savedUser);
        vt.setExpiryDate(LocalDateTime.now().plusHours(24));

        tokenRepo.save(vt);

        emailService.sendVerificationEmail(savedUser.getEmail(), token);
    }

    // RESEND VERIFICATION --> USER EMAIL
    public void resendVerificationEmail(String email) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email does not exist"));

        if (user.getEmailVerified()) {
            throw new RuntimeException("Email already verified");
        }

        VerificationToken oldToken =
                tokenRepo.findByUser(user).orElse(null);

        if (oldToken != null) {
            tokenRepo.delete(oldToken);
        }

        String newToken = UUID.randomUUID().toString();

        VerificationToken vt = new VerificationToken();
        vt.setToken(newToken);
        vt.setUser(user);
        vt.setExpiryDate(LocalDateTime.now().plusHours(24));

        tokenRepo.save(vt);

        emailService.sendVerificationEmail(user.getEmail(), newToken);
    }

    public void sendOtp(String email) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        String otp = String.valueOf(
                (int) (Math.random() * 900000) + 100000
        );

        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));

        userRepo.save(user);

        emailService.sendOtpEmail(email, otp);
    }

    // RESET PASSWORD
    public void resetPassword(String email, String otp, String newPassword) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        user.setPassword(encoder.encode(newPassword));
        user.setOtp(null);
        user.setOtpExpiry(null);

        userRepo.save(user);
    }

    // getting all user
    public List<User> getAllUser(Authentication authentication) {
        User loggedInUser = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));
        if (loggedInUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only ADMIN Can Access UserDetails");
        }
        return userRepo.findAll();
    }

    public User updateUserProfile(
            Integer userId,
            UpdateProfileRequest request,
            MultipartFile image,
            Authentication authentication
    ) {

        User loggedInUser = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

        User targetUser = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = loggedInUser.getRole() == Role.ADMIN;
        boolean isSelf = loggedInUser.getId().equals(targetUser.getId());

        if (!isAdmin && !isSelf) {
            throw new AccessDeniedException("Unauthorized");
        }

        // IMAGE upload
        if (image != null && !image.isEmpty()) {

            Map<String, Object> uploadResult =
                    cloudinaryService.uploadImage(image);

            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            if (imageUrl == null || publicId == null) {
                throw new RuntimeException("Profile image upload failed");
            }

            // delete old image AFTER success
            if (targetUser.getProfileImagePublicId() != null) {
                cloudinaryService.deleteImage(
                        targetUser.getProfileImagePublicId()
                );
            }

            targetUser.setProfileImageUrl(imageUrl);
            targetUser.setProfileImagePublicId(publicId);
        }

        // PROFILE FIELDS
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            targetUser.setUsername(request.getUsername());
        }

        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            targetUser.setAddress(request.getAddress());
        }

        if (request.getCity() != null && !request.getCity().isBlank()) {
            targetUser.setCity(request.getCity());
        }

        if (request.getPhoneNo() != null && !request.getPhoneNo().isBlank()) {
            targetUser.setPhoneNo(request.getPhoneNo());
        }

        if (request.getZipCode() != null && !request.getZipCode().isBlank()) {
            targetUser.setZipCode(request.getZipCode());
        }
        // role --> ADMIN
        if (isAdmin && request.getRole() != null && !request.getRole().isBlank()) {
            targetUser.setRole(Role.valueOf(request.getRole())); // Role enum must match "USER" or "ADMIN"
        }

        return userRepo.save(targetUser);
    }


    public User adminUpdateUser(Integer userId, UpdateProfileRequest request, MultipartFile image, Authentication authentication) {
        // 1. Check if logged-in user is admin
        User loggedInUser = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

        if (loggedInUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Unauthorized");
        }

        // 2. Fetch the target user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }
        if (request.getPhoneNo() != null && !request.getPhoneNo().isBlank()) {
            user.setPhoneNo(request.getPhoneNo());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            user.setAddress(request.getAddress());
        }
        if (request.getCity() != null && !request.getCity().isBlank()) {
            user.setCity(request.getCity());
        }
        if (request.getZipCode() != null && !request.getZipCode().isBlank()) {
            user.setZipCode(request.getZipCode());
        }

        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                user.setRole(Role.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role value: " + request.getRole());
            }
        }
        if (image != null && !image.isEmpty()) {
            Map<String, Object> uploadResult = cloudinaryService.uploadImage(image);

            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            if (imageUrl == null || publicId == null) {
                throw new RuntimeException("Profile image upload failed");
            }

            // Delete old image AFTER success
            if (user.getProfileImagePublicId() != null) {
                cloudinaryService.deleteImage(user.getProfileImagePublicId());
            }

            user.setProfileImageUrl(imageUrl);
            user.setProfileImagePublicId(publicId);
        }

        return userRepo.save(user);
    }

}
