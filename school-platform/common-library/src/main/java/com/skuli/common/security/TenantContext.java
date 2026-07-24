package com.skuli.common.security;

/**
 * Holds the current request's tenant (school) id in a thread-local, populated from the
 * {@code tenant_id} JWT claim by {@link TenantContextFilter}. Read by the Hibernate tenant
 * filter (added in Phase 2) so every tenant-owned query is automatically scoped.
 *
 * <p>Model: one user = one school, so the tenant id is a single scalar value.
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(String tenantId) {
        CURRENT.set(tenantId);
    }

    public static String get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
