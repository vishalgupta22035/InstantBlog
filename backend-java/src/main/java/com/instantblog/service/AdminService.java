
package com.instantblog.service;

import com.instantblog.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    private final JwtService jwtService;

    public AdminService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String login(String email, String password) {


        if (email == null || password == null) {
            return null;
        }

        if (!adminEmail.equals(email) || !adminPassword.equals(password)) {
            return null;
        }

        return jwtService.generateToken(email);
    }
}
