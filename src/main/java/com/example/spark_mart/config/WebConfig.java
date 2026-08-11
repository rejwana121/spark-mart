package com.example.spark_mart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.spark_mart.security.AdminAuthenticationInterceptor;
import com.example.spark_mart.security.CustomerAuthenticationInterceptor;
import com.example.spark_mart.security.SellerAuthenticationInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AdminAuthenticationInterceptor adminAuthenticationInterceptor;
    private final CustomerAuthenticationInterceptor customerAuthenticationInterceptor;
    private final SellerAuthenticationInterceptor sellerAuthenticationInterceptor;

    public WebConfig(AdminAuthenticationInterceptor adminAuthenticationInterceptor,
            CustomerAuthenticationInterceptor customerAuthenticationInterceptor,
            SellerAuthenticationInterceptor sellerAuthenticationInterceptor) {
        this.adminAuthenticationInterceptor = adminAuthenticationInterceptor;
        this.customerAuthenticationInterceptor = customerAuthenticationInterceptor;
        this.sellerAuthenticationInterceptor = sellerAuthenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthenticationInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login", "/admin/logout");

        registry.addInterceptor(sellerAuthenticationInterceptor)
                .addPathPatterns("/seller/**")
                .excludePathPatterns("/seller/login", "/seller/logout", "/seller/register");

        registry.addInterceptor(customerAuthenticationInterceptor)
                .addPathPatterns("/cart/**", "/checkout/**", "/payment/**", "/orders/**", "/profile/**");
    }
}
