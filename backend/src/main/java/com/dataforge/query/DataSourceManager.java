package com.dataforge.query;

import com.dataforge.connection.ConnectionConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DataSourceManager {

    private final Map<Long, HikariDataSource> pools = new ConcurrentHashMap<>();

    public DataSource getOrCreate(ConnectionConfig config) {
        return pools.computeIfAbsent(config.getId(), id -> createDataSource(config));
    }

    public HikariDataSource createDataSource(ConnectionConfig config) {
        HikariConfig hikari = new HikariConfig();
        String url = buildJdbcUrl(config);
        hikari.setJdbcUrl(url);
        hikari.setUsername(config.getUsername());
        hikari.setPassword(config.getPassword());
        hikari.setMaximumPoolSize(5);
        hikari.setMinimumIdle(1);
        hikari.setIdleTimeout(300_000);
        hikari.setConnectionTimeout(5_000);
        // ponytail: auto-detect driver from raw JDBC URL
        if (url.startsWith("jdbc:")) {
            String driver = url.substring(5);
            int colon = driver.indexOf(':');
            String vendor = colon > 0 ? driver.substring(0, colon) : driver;
            hikari.setDriverClassName(driverClass(vendor.toUpperCase()));
        } else {
            hikari.setDriverClassName(driverClass(config.getDbType()));
        }
        return new HikariDataSource(hikari);
    }

    public void remove(Long id) {
        HikariDataSource ds = pools.remove(id);
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
    }

    private String buildJdbcUrl(ConnectionConfig c) {
        // ponytail: accept raw JDBC URL in database field (e.g. for tests)
        if (c.getDatabase() != null && c.getDatabase().startsWith("jdbc:")) {
            return c.getDatabase();
        }
        return switch (c.getDbType()) {
            case "POSTGRESQL" -> "jdbc:postgresql://" + c.getHost() + ":" + c.getPort() + "/" + c.getDatabase();
            case "MYSQL" -> "jdbc:mysql://" + c.getHost() + ":" + c.getPort() + "/" + c.getDatabase();
            case "SQLITE" -> "jdbc:sqlite:" + c.getDatabase();
            default -> throw new IllegalArgumentException("Unsupported DB type: " + c.getDbType());
        };
    }

    private String driverClass(String dbType) {
        return switch (dbType) {
            case "POSTGRESQL" -> "org.postgresql.Driver";
            case "MYSQL" -> "com.mysql.cj.jdbc.Driver";
            case "SQLITE" -> "org.sqlite.JDBC";
            case "H2" -> "org.h2.Driver";
            default -> throw new IllegalArgumentException("Unsupported DB type: " + dbType);
        };
    }
}
