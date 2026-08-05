package com.skuli.staff.internal.domain;

import com.skuli.common.domain.TenantAware;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A platform administrator. Like other people in the system the id equals the Keycloak username.
 * Ported verbatim from the Prisma {@code Admin} model (no timestamps in the source), with the
 * added {@code tenant_id} from {@link TenantAware}.
 */
@Entity
@Table(name = "admin")
public class Admin extends TenantAware {

    @Id
    private String id;

    @Column(nullable = false)
    private String username;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
