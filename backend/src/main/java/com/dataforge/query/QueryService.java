package com.dataforge.query;

import com.dataforge.connection.ConnectionConfig;
import com.dataforge.connection.ConnectionRepository;
import com.dataforge.ws.EventPublisher;
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
    private final QueryHistoryRepository historyRepo;
    private final EventPublisher eventPublisher;

    public QueryService(ConnectionRepository connectionRepo, DataSourceManager dataSourceManager, QueryHistoryRepository historyRepo, EventPublisher eventPublisher) {
        this.connectionRepo = connectionRepo;
        this.dataSourceManager = dataSourceManager;
        this.historyRepo = historyRepo;
        this.eventPublisher = eventPublisher;
    }

    public QueryResult execute(QueryRequest request) {
        long start = System.currentTimeMillis();
        ConnectionConfig config = connectionRepo.findById(request.getConnectionId())
                .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + request.getConnectionId()));

        DataSource ds = dataSourceManager.getOrCreate(config);
        JdbcTemplate jdbc = new JdbcTemplate(ds);

        try {
            String rawSql = request.getSql().trim();
            // ponytail: strip trailing semicolons (JDBC doesn't accept them)
            while (rawSql.endsWith(";")) rawSql = rawSql.substring(0, rawSql.length() - 1).trim();
            String sql = rawSql.toUpperCase();
            QueryResult result;
            if (sql.startsWith("SELECT") || sql.startsWith("WITH") || sql.startsWith("EXPLAIN") || sql.startsWith("DESCRIBE") || sql.startsWith("SHOW")) {
                result = executeQuery(jdbc, rawSql, start);
            } else {
                result = executeUpdate(jdbc, rawSql, start);
            }
            historyRepo.save(new QueryHistory(
                request.getConnectionId(), request.getSql(), "SUCCESS",
                result.getElapsedMs(),
                result.getRows() != null ? result.getRows().size() : result.getAffectedRows(),
                null
            ));
            eventPublisher.queryExecuted(
                request.getConnectionId(), request.getSql(),
                "SUCCESS",
                result.getElapsedMs(),
                result.getRows() != null ? result.getRows().size() : result.getAffectedRows(),
                null
            );
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            QueryResult result = new QueryResult(e.getMessage(), elapsed);
            historyRepo.save(new QueryHistory(
                request.getConnectionId(), request.getSql(), "ERROR",
                elapsed, null, e.getMessage()
            ));
            eventPublisher.queryExecuted(
                request.getConnectionId(), request.getSql(),
                "ERROR",
                elapsed,
                null,
                e.getMessage()
            );
            return result;
        }
    }

    private QueryResult executeQuery(JdbcTemplate jdbc, String sql, long start) {
        List<String> columns = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        jdbc.setQueryTimeout(30);
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
        jdbc.setQueryTimeout(30);
        int affected = jdbc.update(sql);
        long elapsed = System.currentTimeMillis() - start;
        return new QueryResult(affected, elapsed);
    }
}
