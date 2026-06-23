package com.dataforge.connection;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("connection_configs")
public class ConnectionConfig {

    @Id
    private Long id;
    private String name;
    private String dbType;    // POSTGRESQL, MYSQL, SQLITE, ORACLE
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    public ConnectionConfig() {}

    public ConnectionConfig(String name, String dbType, String host, int port, String database, String username, String password) {
        this.name = name;
        this.dbType = dbType;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDbType() { return dbType; }
    public void setDbType(String dbType) { this.dbType = dbType; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
