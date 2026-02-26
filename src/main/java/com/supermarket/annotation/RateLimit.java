package com.supermarket.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * 使用 Redis 实现滑动窗口限流，防止高并发下数据库崩溃
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流的 key 前缀（默认使用方法名）
     */
    String key() default "";

    /**
     * 时间窗口内允许的最大请求次数
     */
    int maxRequests() default 10;

    /**
     * 时间窗口（秒）
     */
    int windowSeconds() default 1;

    /**
     * 限流提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
}
