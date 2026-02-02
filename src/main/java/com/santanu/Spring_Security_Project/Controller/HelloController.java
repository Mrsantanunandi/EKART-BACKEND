package com.santanu.Spring_Security_Project.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "Backend is running!";
    }
    @GetMapping("hello")
    public String greet(HttpServletRequest request)
    {
        return "Hello mr! " + request.getSession().getId();
    }

    @GetMapping("about")
    public String about(HttpServletRequest request)
    {
        return "This ia about page..  "+ request.getSession().getId();
    }
}
