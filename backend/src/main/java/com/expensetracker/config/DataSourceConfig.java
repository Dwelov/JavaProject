package com.expensetracker.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import jakarta.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class DataSourceConfig {
    private final Environment env;
    private HikariDataSource hds;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public DataSourceConfig(Environment env) {
        this.env = env;
    }

    @Primary
    @org.springframework.context.annotation.Bean
    public DataSource dataSource() {
        String url = env.getProperty("SPRING_DATASOURCE_URL", env.getProperty("spring.datasource.url"));
        String user = env.getProperty("SPRING_DATASOURCE_USERNAME", env.getProperty("spring.datasource.username", "postgres"));
        String pass = env.getProperty("SPRING_DATASOURCE_PASSWORD", env.getProperty("spring.datasource.password", ""));
        String driver = env.getProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");

        try {
            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl(url);
            cfg.setUsername(user);
            cfg.setPassword(pass);
            cfg.setDriverClassName(driver);
            cfg.setConnectionTimeout(2000);

            hds = new HikariDataSource(cfg);
            try (Connection c = hds.getConnection()) {
                log.info("Connected to primary DB: {}", url);
                return hds;
            } catch (SQLException ex) {
                hds.close();
                throw ex;
            }
        } catch (Exception ex) {
            log.warn("Primary DB unavailable ({}). Falling back to in-memory H2.", ex.getMessage());
            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl("jdbc:h2:mem:expensedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            cfg.setDriverClassName("org.h2.Driver");
            cfg.setUsername("sa");
            cfg.setPassword("");

            hds = new HikariDataSource(cfg);
            return hds;
        }
    }

    @PreDestroy
    public void close() {
        if (hds != null) hds.close();
    }
}
