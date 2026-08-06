package com.skuli.app.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import com.skuli.academics.internal.domain.Subject;
import com.skuli.academics.internal.repository.SubjectRepository;
import com.skuli.common.security.TenantContext;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Proves that Hibernate's {@code @TenantId} discriminator isolates tenants end-to-end against a
 * real Postgres: the tenant column is stamped automatically on insert (never set by application
 * code) and every read is automatically filtered to the current tenant, so one school can neither
 * see nor collide with another's rows.
 *
 * <p>Requires a running database, so it is gated on {@code RUN_DB_IT=true} and skipped in the
 * default (mock-only) test run. Executed by {@code scripts/it.sh} against a throwaway Postgres.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_DB_IT", matches = "true")
class TenantIsolationIntegrationTest {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private JdbcTemplate jdbc;

    /** The two schools must exist in the tenant registry (the tenant_id FK enforces this). */
    @BeforeEach
    void seedTenants() {
        jdbc.update("INSERT INTO tenant (id, name) VALUES ('school-a', 'School A') "
                + "ON CONFLICT (id) DO NOTHING");
        jdbc.update("INSERT INTO tenant (id, name) VALUES ('school-b', 'School B') "
                + "ON CONFLICT (id) DO NOTHING");
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void subjectsAreStampedAndFilteredByTenantAutomatically() {
        String mathName = "Math-" + UUID.randomUUID();
        String scienceName = "Science-" + UUID.randomUUID();

        TenantContext.set("school-a");
        Subject math = new Subject();
        math.setName(mathName);
        Integer aId = subjectRepository.saveAndFlush(math).getId();
        // tenant_id was populated by Hibernate from the resolver, not by application code.
        assertThat(math.getTenantId()).isEqualTo("school-a");

        TenantContext.set("school-b");
        Subject science = new Subject();
        science.setName(scienceName);
        Integer bId = subjectRepository.saveAndFlush(science).getId();

        // As school-b: school-a's row is invisible, and its name is "free" to reuse.
        assertThat(subjectRepository.findById(aId)).isEmpty();
        assertThat(subjectRepository.existsByName(mathName)).isFalse();
        assertThat(subjectRepository.findAll()).extracting(Subject::getTenantId)
                .containsOnly("school-b");

        // As school-a: only school-a's row is visible; school-b's is filtered out.
        TenantContext.set("school-a");
        assertThat(subjectRepository.findById(bId)).isEmpty();
        assertThat(subjectRepository.findById(aId)).isPresent();
        assertThat(subjectRepository.existsByName(mathName)).isTrue();
        assertThat(subjectRepository.findAll()).extracting(Subject::getTenantId)
                .containsOnly("school-a");
    }
}
