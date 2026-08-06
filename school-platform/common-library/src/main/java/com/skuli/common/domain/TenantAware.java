package com.skuli.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.TenantId;

/**
 * Base type for every tenant-owned entity, carrying the {@code tenant_id} column that scopes a row
 * to a single school (multi-school SaaS is a goal, decided at Phase 2).
 *
 * <p>The field is annotated with Hibernate's {@link TenantId}, which turns on discriminator-based
 * multi-tenancy: Hibernate <em>automatically</em> stamps the column on insert from the current
 * {@code CurrentTenantIdentifierResolver} and <em>automatically</em> appends {@code tenant_id = ?}
 * to every select, update, and delete for the entity. Application code therefore never has to
 * filter by tenant by hand, and a row can never leak across schools even if a query forgets to
 * scope itself. The resolver reads the request's tenant from {@code TenantContext}.
 */
@MappedSuperclass
public abstract class TenantAware {

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
