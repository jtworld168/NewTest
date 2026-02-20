package com.supermarket.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/logout",
                        "/api/file/**",
                        // Public read endpoints (anonymous browsing)
                        "/api/products/list",
                        "/api/products/get/**",
                        "/api/products/search",
                        "/api/products/searchAll",
                        "/api/products/onShelf",
                        "/api/products/getByCategoryId/**",
                        "/api/categories/list",
                        "/api/categories/get/**",
                        "/api/coupons/list",
                        "/api/coupons/get/**",
                        "/api/stores/list",
                        "/api/stores/get/**",
                        "/api/store-products/list",
                        "/api/store-products/getByStoreId/**",
                        "/api/store-products/getByProductId/**",
                        "/api/users/add",
                        "/doc.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/**"
                );
    }
}
