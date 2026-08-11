package com.example.spark_mart.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    public static final String SESSION_ADMIN = "sparkMartAdmin";

    private final String username;
    private final String encodedPassword;
    private final PasswordEncoder passwordEncoder;

    public AdminService(@Value("${sparkmart.admin.username:admin@sparkmart.com}") String username,
            @Value("${sparkmart.admin.password:admin123}") String password,
            PasswordEncoder passwordEncoder) {
        this.username = username;
        this.passwordEncoder = passwordEncoder;
        // The admin password arrives in plain text from configuration (env var / properties),
        // so it is BCrypt-hashed once here; the hash — never the raw password — is what
        // gets compared against on every login attempt.
        this.encodedPassword = passwordEncoder.encode(password);
    }

    public boolean authenticate(String username, String password) {
        return this.username.equals(username) && passwordEncoder.matches(password, this.encodedPassword);
    }
}
