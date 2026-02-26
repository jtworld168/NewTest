package com.supermarket.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@Schema(description = "CDN配置")
public class CdnConfig {

    @Value("${cdn.base-url:}")
    private String baseUrl;

    /**
     * 获取完整的CDN资源URL
     * 如果CDN未配置，返回原始路径
     */
    public String getFullUrl(String path) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return path;
        }
        return baseUrl + path;
    }
}
