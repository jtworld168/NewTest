package com.supermarket.interceptor;

import com.supermarket.annotation.RateLimit;
import com.supermarket.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * Redis 限流拦截器
 * 基于滑动窗口计数器实现接口限流
 * 防止高并发下数据库崩溃
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true;
        }

        // Without Redis, skip rate limiting
        if (redisTemplate == null) {
            return true;
        }

        String key = buildKey(rateLimit, request, handlerMethod);
        String countKey = "rate_limit:" + key;

        Long currentCount = redisTemplate.opsForValue().increment(countKey);
        if (currentCount != null && currentCount == 1) {
            // First request in window, set expiration
            redisTemplate.expire(countKey, rateLimit.windowSeconds(), TimeUnit.SECONDS);
        }

        if (currentCount != null && currentCount > rateLimit.maxRequests()) {
            log.warn("接口限流触发: key={}, count={}, limit={}", key, currentCount, rateLimit.maxRequests());
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(429);
            response.getWriter().write(objectMapper.writeValueAsString(Result.error(rateLimit.message())));
            return false;
        }

        return true;
    }

    private String buildKey(RateLimit rateLimit, HttpServletRequest request, HandlerMethod handlerMethod) {
        String key = rateLimit.key();
        if (key.isEmpty()) {
            key = handlerMethod.getMethod().getDeclaringClass().getSimpleName() + ":" + handlerMethod.getMethod().getName();
        }
        // Per-IP rate limiting
        String ip = request.getRemoteAddr();
        return key + ":" + ip;
    }
}
