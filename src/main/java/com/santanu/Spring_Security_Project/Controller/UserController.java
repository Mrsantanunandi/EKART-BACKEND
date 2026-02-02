package com.santanu.Spring_Security_Project.Controller;

import com.santanu.Spring_Security_Project.Model.ResetPasswordRequest;
import com.santanu.Spring_Security_Project.Model.UpdateProfileRequest;
import com.santanu.Spring_Security_Project.Model.User;
import com.santanu.Spring_Security_Project.Model.UserDetailsPrinciple;
import com.santanu.Spring_Security_Project.Service.EmailVerificationService;
import com.santanu.Spring_Security_Project.Service.JwtService;
import com.santanu.Spring_Security_Project.Service.UserService;
import com.santanu.Spring_Security_Project.dao.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

//This is for register a new User

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private EmailVerificationService verificationService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    //-----------------------------------------------------------------
    //For register a new user

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> saveUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        try {
            userService.saveUser(user);
            response.put("success", true);
            response.put("message", "User Registered Successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
//--------------------------------------------------------------------------------
    //for Login a user using jwt token

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(), user.getPassword())
            );

            if (authentication.isAuthenticated()) {
                UserDetailsPrinciple userDetails =  (UserDetailsPrinciple) authentication.getPrincipal();
                String token = jwtService.generateToken(user.getUsername());
                response.put("success", true);
                response.put("token", token);        // JWT token
                assert userDetails != null;
                response.put("user",userDetails.getUser());
                response.put("message", "Login Successful ! -> " + userDetails.getUsername());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Login Failed");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(401).body(response);
        }
    }


    //------------------------------------------------------------------------------
    //Fetching all the User from DB- ONLY ADMIN
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllUser(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<User> allUser =userService.getAllUser(authentication);
            response.put("success", true);
            response.put("message", "All User Fetched Successfully");
            response.put("user",allUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    //-------------------------------------------------------------------------------
    //Forgot-password
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userService.sendOtp(email);
        return ResponseEntity.ok("OTP sent to your email");
    }
    //--------------------------------------------------------------------------------
    //Reset Your Password
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody ResetPasswordRequest request
    ) {
        userService.resetPassword(
                request.getEmail(),
                request.getOtp(),
                request.getNewPassword()
        );

        return ResponseEntity.ok("Password reset successfully");
    }
    //--------------------------------------------------------------------------------
    //Fetching user details only after login

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {

        UserDetailsPrinciple userDetails =
                (UserDetailsPrinciple) authentication.getPrincipal();

        assert userDetails != null;
        return ResponseEntity.ok(userDetails.getUser());
    }
    // --------------------------------------------------------------------------------
    //For Verify a Email.
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        verificationService.verifyEmail(token);
        return ResponseEntity.ok("Email verified successfully!");
    }
    //----------------------------------------------------------------------------------
    @PostMapping("/reverify")
    public ResponseEntity<String> resendVerification(
            @RequestBody Map<String, String> request
    ) {
        String email = request.get("email");

        try {
            userService.resendVerificationEmail(email);
            return ResponseEntity.ok("Verification email sent successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //---------------------------------------------------------------------------
    //Now For Updating User
    @PutMapping(
            value = "/update-user/{userId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Integer userId,
            @RequestPart("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            UpdateProfileRequest request =
                    mapper.readValue(data, UpdateProfileRequest.class);

            User updatedUser =
                    userService.updateUserProfile(userId, request, image, authentication);

            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("user", updatedUser);

            return ResponseEntity.ok(response);

        } catch (AccessDeniedException e) {
            response.put("success", false);
            response.put("message", "You are not allowed to update profile");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }



    //--------------------ADMIN UPDATE USER ----------------------------------
    @PutMapping(
            value = "/admin/update-user/{userId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> adminUpdateUser(
            @PathVariable Integer userId,
            @RequestPart("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            UpdateProfileRequest request = mapper.readValue(data, UpdateProfileRequest.class);

            User updatedUser = userService.adminUpdateUser(userId, request, image,authentication);

            response.put("success", true);
            response.put("message", "User updated successfully by admin");
            response.put("user", updatedUser);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

//-----------------------------------------------------------------------------------
        //Logout is done on client side

}
