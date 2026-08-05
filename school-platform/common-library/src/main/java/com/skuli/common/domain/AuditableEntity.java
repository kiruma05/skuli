package com.skuli.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Tenant-owned entity that also tracks when its row was first created, mirroring the Prisma
 * {@code createdAt @default(now())} on Student/Teacher/Parent. Uses Spring Data JPA auditing
 * ({@link CreatedDate}) rather than a raw Hibernate timestamp so that {@code @CreatedBy} — the
 * foundation of the audit trail — can be added in a later phase without changing the mechanism.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity extends TenantAware {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
