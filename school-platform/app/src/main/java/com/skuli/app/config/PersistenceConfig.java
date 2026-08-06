package com.skuli.app.config;

import com.skuli.common.security.TenantContextIdentifierResolver;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Persistence wiring for the modular monolith: widens JPA entity discovery and Spring Data
 * repository scanning to the whole {@code com.skuli} tree, activates auditing so
 * {@code @CreatedDate} on {@link com.skuli.common.domain.AuditableEntity} is populated on insert,
 * and registers the tenant resolver that drives Hibernate's {@code @TenantId} multi-tenancy.
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

    /**
     * Registers the {@link TenantContextIdentifierResolver} so Hibernate stamps and filters every
     * tenant-owned entity by the current request's tenant automatically.
     */
    @Bean
    public HibernatePropertiesCustomizer tenantIdentifierResolverCustomizer() {
        return properties -> properties.put(
                AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER,
                new TenantContextIdentifierResolver());
    }
}
