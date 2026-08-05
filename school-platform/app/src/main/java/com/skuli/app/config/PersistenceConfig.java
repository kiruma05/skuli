package com.skuli.app.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Persistence wiring for the modular monolith: widens JPA entity discovery and Spring Data
 * repository scanning to the whole {@code com.skuli} tree, and activates auditing so
 * {@code @CreatedDate} on {@link com.skuli.common.domain.AuditableEntity} is populated on insert.
 * {@code @CreatedBy} (and its {@code AuditorAware}) is wired once the security principal is
 * available on the request thread.
 *
 * <p>Kept as a standalone {@code @Configuration} (not on the application class) so that sliced
 * web-layer tests can load controllers + security without bootstrapping the persistence layer.
 */
@Configuration
@EntityScan("com.skuli")
@EnableJpaRepositories("com.skuli")
@EnableJpaAuditing
public class PersistenceConfig {
}
