package com.santanu.Spring_Security_Project.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {

    private String address;
    private String city;
    private String zipCode;
    private String phoneNo;
    private String username;
    private String role;
}
