package com.skuli.auth.internal.repository;

import com.skuli.auth.internal.domain.ProvisioningAudit;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link ProvisioningAudit}. Tenant-scoped automatically via {@code @TenantId}. */
public interface ProvisioningAuditRepository extends JpaRepository<ProvisioningAudit, Long> {
}
