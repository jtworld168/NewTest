package com.supermarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UnmannedSupermarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnmannedSupermarketApplication.class, args);
    }
}
