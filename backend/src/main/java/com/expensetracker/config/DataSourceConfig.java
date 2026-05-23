package com.expensetracker.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import javax.sql.DataSource;
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
    public DataSource dataSource() throws SQLException {
        String url = env.getProperty("SPRING_DATASOURCE_URL", env.getProperty("spring.datasource.url"));
        String user = env.getProperty("SPRING_DATASOURCE_USERNAME", env.getProperty("spring.datasource.username", "postgres"));
        String pass = env.getProperty("SPRING_DATASOURCE_PASSWORD", env.getProperty("spring.datasource.password", ""));
        String driver = env.getProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(pass);
        cfg.setDriverClassName(driver);
        cfg.setConnectionTimeout(5000);

        hds = new HikariDataSource(cfg);
        try (Connection c = hds.getConnection()) {
            log.info("Connected to primary DB: {}", url);
            return hds;
        } catch (SQLException ex) {
            log.error("Failed to connect to primary DB ({}).", url);
            hds.close();
            throw ex;
        }
    }

    @PreDestroy
    public void close() {
        if (hds != null) hds.close();
    }
}
