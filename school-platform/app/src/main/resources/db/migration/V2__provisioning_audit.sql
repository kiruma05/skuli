-- =====================================================================================
-- V2__provisioning_audit.sql
--
-- Audit trail for the Keycloak-then-DB provisioning compensation (plan §5.2). Keycloak is not
-- transactional with the database, so when a DB write fails after the Keycloak user was created,
-- the service deletes that user to compensate. Every such attempt — and crucially every FAILED
-- attempt, which leaves an orphaned Keycloak user needing manual cleanup — is recorded here so it
-- is visible instead of silent.
-- =====================================================================================
CREATE TABLE provisioning_audit (
    id         BIGSERIAL     NOT NULL,
    tenant_id  VARCHAR(255)  NOT NULL,
    username   VARCHAR(255)  NOT NULL,
    action     VARCHAR(255)  NOT NULL,
    outcome    VARCHAR(255)  NOT NULL,
    detail     VARCHAR(2000),
    created_at TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT provisioning_audit_pkey PRIMARY KEY (id),
    CONSTRAINT provisioning_audit_tenant_fkey FOREIGN KEY (tenant_id) REFERENCES tenant (id),
    CONSTRAINT provisioning_audit_outcome_check CHECK (outcome IN ('SUCCESS', 'FAILURE'))
);

-- Fast path for "show me the compensations that failed" (orphaned Keycloak users).
CREATE INDEX provisioning_audit_outcome_idx ON provisioning_audit (outcome);
