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
    private boolean useSshTunnel;
    private String sshHost;
    private int sshPort = 22;
    private String sshUser;
    private String sshPassword;
    private String sshPrivateKeyPath;

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

    public boolean isUseSshTunnel() { return useSshTunnel; }
    public void setUseSshTunnel(boolean useSshTunnel) { this.useSshTunnel = useSshTunnel; }
    public String getSshHost() { return sshHost; }
    public void setSshHost(String sshHost) { this.sshHost = sshHost; }
    public int getSshPort() { return sshPort; }
    public void setSshPort(int sshPort) { this.sshPort = sshPort; }
    public String getSshUser() { return sshUser; }
    public void setSshUser(String sshUser) { this.sshUser = sshUser; }
    public String getSshPassword() { return sshPassword; }
    public void setSshPassword(String sshPassword) { this.sshPassword = sshPassword; }
    public String getSshPrivateKeyPath() { return sshPrivateKeyPath; }
    public void setSshPrivateKeyPath(String sshPrivateKeyPath) { this.sshPrivateKeyPath = sshPrivateKeyPath; }
}
