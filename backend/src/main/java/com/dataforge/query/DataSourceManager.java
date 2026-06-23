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
        hikari.setJdbcUrl(buildJdbcUrl(config));
        hikari.setUsername(config.getUsername());
        hikari.setPassword(config.getPassword());
        hikari.setMaximumPoolSize(5);
        hikari.setMinimumIdle(1);
        hikari.setIdleTimeout(300_000);
        hikari.setConnectionTimeout(5_000);
        hikari.setDriverClassName(driverClass(config.getDbType()));
        return new HikariDataSource(hikari);
    }

    public void remove(Long id) {
        HikariDataSource ds = pools.remove(id);
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
    }

    private String buildJdbcUrl(ConnectionConfig c) {
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
            default -> throw new IllegalArgumentException("Unsupported DB type: " + dbType);
        };
    }
}
