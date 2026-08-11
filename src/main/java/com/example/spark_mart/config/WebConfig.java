package com.example.spark_mart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.spark_mart.security.AdminAuthenticationInterceptor;
import com.example.spark_mart.security.CustomerAuthenticationInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AdminAuthenticationInterceptor adminAuthenticationInterceptor;
    private final CustomerAuthenticationInterceptor customerAuthenticationInterceptor;

    public WebConfig(AdminAuthenticationInterceptor adminAuthenticationInterceptor,
            CustomerAuthenticationInterceptor customerAuthenticationInterceptor) {
        this.adminAuthenticationInterceptor = adminAuthenticationInterceptor;
        this.customerAuthenticationInterceptor = customerAuthenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthenticationInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login", "/admin/logout");

        // Guest/public view is allowed for homepage, categories, product details and navbar search.
        // Customer login is required only for actions that create personal data: cart, checkout,
        // payment, profile and orders.
        registry.addInterceptor(customerAuthenticationInterceptor)
                .addPathPatterns("/cart/**", "/checkout/**", "/payment/**", "/orders/**", "/profile/**");
    }
}
