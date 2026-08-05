package com.skuli.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point. Component scanning is widened from the default {@code com.skuli.app}
 * package to the whole {@code com.skuli} tree so every module's beans are picked up while the app
 * remains the single deployable. JPA entity/repository scanning lives in
 * {@link com.skuli.app.config.PersistenceConfig} (kept off this class so web-slice tests don't
 * pull in the persistence layer).
 */
@SpringBootApplication(scanBasePackages = "com.skuli")
public class SchoolPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchoolPlatformApplication.class, args);
    }
}
