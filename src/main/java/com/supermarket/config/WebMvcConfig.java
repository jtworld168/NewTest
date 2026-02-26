package com.supermarket.config;

import com.supermarket.interceptor.HttpCacheInterceptor;
import com.supermarket.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:file}")
    private String uploadDir;

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    private HttpCacheInterceptor httpCacheInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**");
        registry.addInterceptor(httpCacheInterceptor)
                .addPathPatterns("/api/**", "/file/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Images are served via /api/file/{filename} controller endpoint
        // This static handler is kept as fallback for /file/** direct access with CDN-friendly cache
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/file/**")
                .addResourceLocations(absolutePath)
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("satoken")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
