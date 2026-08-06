package com.skuli.common.security;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

/**
 * Feeds Hibernate the current request's tenant (from {@link TenantContext}) for discriminator-based
 * multi-tenancy — the value Hibernate stamps onto new rows and filters existing rows by.
 *
 * <p>When no tenant is in scope (e.g. a background task, or a request that never authenticated),
 * it falls back to the seeded {@value #DEFAULT_TENANT} tenant rather than returning {@code null},
 * which Hibernate does not allow. API requests always carry a tenant (the security filter sets it),
 * so the fallback only guards non-request code paths.
 */
public class TenantContextIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    public static final String DEFAULT_TENANT = "default";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.get();
        return tenant != null ? tenant : DEFAULT_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}
