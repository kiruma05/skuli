package com.skuli.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Application entry point. Component scanning, JPA entity discovery, and Spring Data repository
 * scanning are all widened from the default {@code com.skuli.app} package to the whole
 * {@code com.skuli} tree, so every module's beans, {@code @Entity} classes, and repositories are
 * picked up while the app remains the single deployable.
 */
@SpringBootApplication(scanBasePackages = "com.skuli")
@EntityScan("com.skuli")
@EnableJpaRepositories("com.skuli")
public class SchoolPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchoolPlatformApplication.class, args);
    }
}
