package com.example.spark_mart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security setup (Chapter 5).
 *
 * Scope, by design:
 *  - The existing hand-rolled session login (Admin + Seller, both going
 *    through AdminController -> AdminService / CustomerService) keeps
 *    working exactly as before — every application route stays permitAll()
 *    at the Spring Security layer, so no existing customer/seller/admin
 *    flow is put at risk by this change.
 *  - What genuinely runs through Spring Security here: password hashing
 *    (BCryptPasswordEncoder, used by AdminService and CustomerService —
 *    see those classes), and a real, RBAC-protected boundary on the
 *    Actuator endpoints, matching the course's own example
 *    (.requestMatchers("/actuator/**").hasRole("ADMIN")).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager actuatorUserDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${sparkmart.admin.username:admin@sparkmart.com}") String adminUsername,
            @Value("${sparkmart.admin.password:admin123}") String adminPassword) {
        UserDetails admin = User.withUsername(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/actuator/**"));
        return http.build();
    }
}
