package com.dataforge.query;

import com.dataforge.connection.ConnectionConfig;
import com.dataforge.connection.ConnectionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@Service
public class QueryService {

    private final ConnectionRepository connectionRepo;
    private final DataSourceManager dataSourceManager;

    public QueryService(ConnectionRepository connectionRepo, DataSourceManager dataSourceManager) {
        this.connectionRepo = connectionRepo;
        this.dataSourceManager = dataSourceManager;
    }

    public QueryResult execute(QueryRequest request) {
        long start = System.currentTimeMillis();
        ConnectionConfig config = connectionRepo.findById(request.getConnectionId())
                .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + request.getConnectionId()));

        DataSource ds = dataSourceManager.getOrCreate(config);
        JdbcTemplate jdbc = new JdbcTemplate(ds);

        try {
            String sql = request.getSql().trim().toUpperCase();
            if (sql.startsWith("SELECT") || sql.startsWith("WITH") || sql.startsWith("EXPLAIN") || sql.startsWith("DESCRIBE") || sql.startsWith("SHOW")) {
                return executeQuery(jdbc, request.getSql(), start);
            } else {
                return executeUpdate(jdbc, request.getSql(), start);
            }
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            return new QueryResult(e.getMessage(), elapsed);
        }
    }

    private QueryResult executeQuery(JdbcTemplate jdbc, String sql, long start) {
        List<String> columns = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        jdbc.query(sql, (ResultSetExtractor<Void>) rs -> {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                columns.add(meta.getColumnLabel(i));
            }
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }
            return null;
        });

        long elapsed = System.currentTimeMillis() - start;
        return new QueryResult(columns, rows, elapsed);
    }

    private QueryResult executeUpdate(JdbcTemplate jdbc, String sql, long start) {
        int affected = jdbc.update(sql);
        long elapsed = System.currentTimeMillis() - start;
        return new QueryResult(affected, elapsed);
    }
}
