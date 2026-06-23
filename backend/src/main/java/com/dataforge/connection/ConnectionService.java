package com.dataforge.connection;

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

    public ConnectionService(ConnectionRepository repo, DataSourceManager dataSourceManager) {
        this.repo = repo;
        this.dataSourceManager = dataSourceManager;
    }

    public List<ConnectionConfig> list() {
        return (List<ConnectionConfig>) repo.findAll();
    }

    public Optional<ConnectionConfig> findById(Long id) {
        return repo.findById(id);
    }

    public ConnectionConfig save(ConnectionConfig config) {
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
