package com.supermarket.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * HTTP缓存拦截器 - 为GET类API响应添加Cache-Control头
 * 首屏<1.5s是生死线，每100ms延迟流失1%用户
 */
@Component
public class HttpCacheInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        // 商品列表和分类列表：缓存60秒（热数据，可接受短暂不一致）
        if (path.startsWith("/api/products/list") || path.startsWith("/api/products/onShelf")
                || path.startsWith("/api/categories/list")) {
            response.setHeader("Cache-Control", "public, max-age=60");
            return true;
        }

        // 商品详情和分类详情：缓存30秒
        if (path.matches("/api/products/get/\\d+") || path.matches("/api/categories/get/\\d+")) {
            response.setHeader("Cache-Control", "public, max-age=30");
            return true;
        }

        // 静态文件（图片）：缓存7天（文件名含UUID，内容不变）
        if (path.startsWith("/file/") || path.startsWith("/api/file/")) {
            response.setHeader("Cache-Control", "public, max-age=604800, immutable");
            return true;
        }

        // 搜索等其他GET请求：不缓存
        if (path.startsWith("/api/")) {
            response.setHeader("Cache-Control", "no-cache");
        }

        return true;
    }
}
