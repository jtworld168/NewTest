package com.supermarket.config;

import org.springframework.context.annotation.Configuration;

/**
 * MongoDB configuration. Repository scanning is handled by Spring Boot auto-configuration.
 * In test environment, MongoDB auto-configuration is excluded via spring.autoconfigure.exclude.
 */
@Configuration
public class MongoConfig {
}
