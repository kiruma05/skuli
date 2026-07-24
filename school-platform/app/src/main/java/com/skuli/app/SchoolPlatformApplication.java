package com.skuli.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point. Scans the whole {@code com.skuli} tree so every module's components
 * are picked up while the app remains the single deployable.
 */
@SpringBootApplication(scanBasePackages = "com.skuli")
public class SchoolPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchoolPlatformApplication.class, args);
    }
}
