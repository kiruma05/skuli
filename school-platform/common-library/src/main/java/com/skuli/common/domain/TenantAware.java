package com.skuli.common.domain;

import com.skuli.common.security.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

/**
 * Base type for every tenant-owned entity. Carries the {@code tenant_id} column that scopes a row
 * to a single school, decided at Phase 2 (multi-school SaaS is a goal).
 *
 * <p>On persist, the tenant id is populated from {@link TenantContext} when not already set, so
 * application code never has to remember to stamp it. The column is {@code NOT NULL} at the DB
 * level, making the design fail-closed: a write with no tenant in scope is rejected rather than
 * silently leaking across schools.
 *
 * <p>Row-level read scoping (enabling a Hibernate {@code @Filter} per request) is deferred to
 * Phase 3, where it lives alongside the request-scoped security filter that populates
 * {@link TenantContext} from the JWT.
 */
@MappedSuperclass
public abstract class TenantAware {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @PrePersist
    void applyTenant() {
        if (tenantId == null) {
            tenantId = TenantContext.get();
        }
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
