package com.skuli.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Activates Spring Data JPA auditing so {@code @CreatedDate} on
 * {@link com.skuli.common.domain.AuditableEntity} is populated on insert. {@code @CreatedBy}
 * (and the {@code AuditorAware} that backs it) is wired in Phase 3 once the security principal
 * is available on the request thread.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
