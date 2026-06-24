package com.dataforge.query;

import com.dataforge.config.EncryptionService;
import com.dataforge.connection.ConnectionConfig;
import com.dataforge.connection.SshTunnelService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DataSourceManager {

    private final Map<Long, HikariDataSource> pools = new ConcurrentHashMap<>();
    private final SshTunnelService sshTunnelService;
    private final EncryptionService encryptionService;

    public DataSourceManager(SshTunnelService sshTunnelService, EncryptionService encryptionService) {
        this.sshTunnelService = sshTunnelService;
        this.encryptionService = encryptionService;
    }

    public DataSource getOrCreate(ConnectionConfig config) {
        return pools.computeIfAbsent(config.getId(), id -> createDataSource(config));
    }

    public HikariDataSource createDataSource(ConnectionConfig config) {
        HikariConfig hikari = new HikariConfig();

        int dbPort = config.getPort();
        String dbHost = config.getHost();
        if (config.isUseSshTunnel()) {
            dbPort = sshTunnelService.openTunnel(config.getId(), config);
            dbHost = "localhost";
        }

        String url = buildJdbcUrl(config, dbHost, dbPort);
        hikari.setJdbcUrl(url);
        hikari.setUsername(config.getUsername());
        // ponytail: decrypt if password was encrypted at rest (ENC: prefix)
        String password = config.getPassword();
        if (password != null && password.startsWith("ENC:")) {
            password = encryptionService.decrypt(password.substring(4));
        }
        hikari.setPassword(password);
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
        return buildJdbcUrl(c, c.getHost(), c.getPort());
    }

    private String buildJdbcUrl(ConnectionConfig c, String host, int port) {
        // ponytail: accept raw JDBC URL in database field (e.g. for tests)
        if (c.getDatabase() != null && c.getDatabase().startsWith("jdbc:")) {
            return c.getDatabase();
        }
        return switch (c.getDbType()) {
            case "POSTGRESQL" -> "jdbc:postgresql://" + host + ":" + port + "/" + c.getDatabase();
            case "MYSQL" -> "jdbc:mysql://" + host + ":" + port + "/" + c.getDatabase();
            case "SQLITE" -> "jdbc:sqlite:" + c.getDatabase();
            case "ORACLE" -> "jdbc:oracle:thin:@//" + host + ":" + port + "/" + c.getDatabase();
            default -> throw new IllegalArgumentException("Unsupported DB type: " + c.getDbType());
        };
    }

    private String driverClass(String dbType) {
        return switch (dbType) {
            case "POSTGRESQL" -> "org.postgresql.Driver";
            case "MYSQL" -> "com.mysql.cj.jdbc.Driver";
            case "SQLITE" -> "org.sqlite.JDBC";
            case "ORACLE" -> "oracle.jdbc.OracleDriver";
            case "H2" -> "org.h2.Driver";
            default -> throw new IllegalArgumentException("Unsupported DB type: " + dbType);
        };
    }
}
