package com.dataforge.connection;

import com.dataforge.config.EncryptionService;
import com.dataforge.query.DataSourceManager;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class ConnectionService {

    private final ConnectionRepository repo;
    private final DataSourceManager dataSourceManager;
    private final EncryptionService encryptionService;

    public ConnectionService(ConnectionRepository repo, DataSourceManager dataSourceManager, EncryptionService encryptionService) {
        this.repo = repo;
        this.dataSourceManager = dataSourceManager;
        this.encryptionService = encryptionService;
    }

    public List<ConnectionConfig> list() {
        List<ConnectionConfig> configs = (List<ConnectionConfig>) repo.findAll();
        for (ConnectionConfig c : configs) {
            if (c.getPassword() != null && c.getPassword().startsWith("ENC:")) {
                try {
                    c.setPassword(encryptionService.decrypt(c.getPassword().substring(4)));
                } catch (Exception e) {
                    c.setPassword("");
                }
            }
        }
        return configs;
    }

    public Optional<ConnectionConfig> findById(Long id) {
        return repo.findById(id).map(c -> {
            if (c.getPassword() != null && c.getPassword().startsWith("ENC:")) {
                try {
                    c.setPassword(encryptionService.decrypt(c.getPassword().substring(4)));
                } catch (Exception e) {
                    c.setPassword("");
                }
            }
            return c;
        });
    }

    public ConnectionConfig save(ConnectionConfig config) {
        if (config.getPassword() != null && !config.getPassword().isEmpty() && !config.getPassword().startsWith("ENC:")) {
            config.setPassword("ENC:" + encryptionService.encrypt(config.getPassword()));
        }
        return repo.save(config);
    }

    public void delete(Long id) {
        dataSourceManager.remove(id);
        repo.deleteById(id);
    }

    public boolean test(Long id) {
        return findById(id).map(this::test).orElse(false);
    }

    public boolean test(ConnectionConfig config) {
        try {
            DataSource ds = dataSourceManager.createDataSource(config);
            try (Connection c = ds.getConnection()) {
                return c.isValid(5);
            }
        } catch (Exception e) {
            return false;
        }
    }
}
