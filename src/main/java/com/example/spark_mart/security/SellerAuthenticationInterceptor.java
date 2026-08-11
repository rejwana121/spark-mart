package com.example.spark_mart.security;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.spark_mart.admin.AdminService;
import com.example.spark_mart.customer.CustomerService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SellerAuthenticationInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (request.getSession().getAttribute(AdminService.SESSION_SELLER) != null) {
            return true;
        }
        response.sendRedirect("/seller/login");
        return false;
    }
}
