package com.supermarket.config;

import cn.dev33.satoken.exception.NotLoginException;
import com.supermarket.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLogin(NotLoginException e) {
        return Result.error(401, "未登录，请先登录");
    }
}
