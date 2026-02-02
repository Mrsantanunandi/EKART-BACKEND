package com.santanu.Spring_Security_Project.Service;

import com.santanu.Spring_Security_Project.Model.User;
import com.santanu.Spring_Security_Project.Model.UserDetailsPrinciple;
import com.santanu.Spring_Security_Project.dao.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user=userRepo.findByUsername(username).orElseThrow(()->new RuntimeException("User Not Found"));

        if(user==null)
        {
            System.out.println("User Not FOUND");
            throw new UsernameNotFoundException("User 404");
        }
        //If User Email is not Verified then Block him to login
        if (!user.getEmailVerified()) {
            throw new RuntimeException("Email not verified. Please verify first.");
        }
        return new UserDetailsPrinciple(user);
    }
}
