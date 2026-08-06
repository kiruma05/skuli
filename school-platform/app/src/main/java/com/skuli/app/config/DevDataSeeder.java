package com.skuli.app.config;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

/**
 * Loads a representative development dataset for the default tenant on startup — but only under the
 * {@code dev} profile (so production is never seeded with fixtures) and only when the database is
 * empty (so restarts don't duplicate or clash). The data is defined in
 * {@code db/seed/dev-data.sql}; this runner just decides whether to apply it.
 */
@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final Resource seedScript;

    public DevDataSeeder(DataSource dataSource, JdbcTemplate jdbcTemplate,
                         @org.springframework.beans.factory.annotation.Value(
                                 "classpath:db/seed/dev-data.sql") Resource seedScript) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.seedScript = seedScript;
    }

    @Override
    public void run(String... args) {
        Integer subjects = jdbcTemplate.queryForObject("SELECT count(*) FROM subject", Integer.class);
        if (subjects != null && subjects > 0) {
            log.info("Dev seed skipped: database already contains data ({} subjects)", subjects);
            return;
        }
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(seedScript);
        populator.execute(dataSource);
        log.info("Dev seed applied from {}", seedScript.getFilename());
    }
}
