package com.dataforge.query;

import com.dataforge.connection.ConnectionConfig;
import com.dataforge.connection.ConnectionRepository;
import com.dataforge.ws.EventPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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
    private final MeterRegistry meterRegistry;
    private final Timer queryTimer;

    public QueryService(ConnectionRepository connectionRepo, DataSourceManager dataSourceManager, QueryHistoryRepository historyRepo, EventPublisher eventPublisher, MeterRegistry meterRegistry) {
        this.connectionRepo = connectionRepo;
        this.dataSourceManager = dataSourceManager;
        this.historyRepo = historyRepo;
        this.eventPublisher = eventPublisher;
        this.meterRegistry = meterRegistry;
        this.queryTimer = Timer.builder("query.execution")
            .description("Query execution time")
            .register(meterRegistry);
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
            queryTimer.record(System.currentTimeMillis() - start, java.util.concurrent.TimeUnit.MILLISECONDS);
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
            queryTimer.record(System.currentTimeMillis() - start, java.util.concurrent.TimeUnit.MILLISECONDS);
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

    private static final java.util.regex.Pattern SAFE_IDENTIFIER =
        java.util.regex.Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_$]*$");

    // ponytail: inline cell UPDATE. Primary key is assumed to be the first column.
    // Upgrade to real PK detection from schema metadata if needed.
    public QueryResult inlineEdit(InlineEditRequest request) {
        long start = System.currentTimeMillis();
        ConnectionConfig config = connectionRepo.findById(request.getConnectionId())
            .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + request.getConnectionId()));

        if (!SAFE_IDENTIFIER.matcher(request.getTable()).matches()) {
            return new QueryResult("Invalid table name", System.currentTimeMillis() - start);
        }
        for (String pkCol : request.getPrimaryKey().keySet()) {
            if (!SAFE_IDENTIFIER.matcher(pkCol).matches()) {
                return new QueryResult("Invalid column name: " + pkCol, System.currentTimeMillis() - start);
            }
        }
        if (!SAFE_IDENTIFIER.matcher(request.getColumn()).matches()) {
            return new QueryResult("Invalid column name", System.currentTimeMillis() - start);
        }

        DataSource ds = dataSourceManager.getOrCreate(config);
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        jdbc.setQueryTimeout(10);

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(request.getTable()).append(" SET ").append(request.getColumn()).append(" = ?");
        sql.append(" WHERE ");
        var pkEntries = new ArrayList<>(request.getPrimaryKey().entrySet());
        for (int i = 0; i < pkEntries.size(); i++) {
            if (i > 0) sql.append(" AND ");
            sql.append(pkEntries.get(i).getKey()).append(" = ?");
        }

        List<Object> params = new ArrayList<>();
        params.add(request.getValue());
        for (var entry : pkEntries) {
            params.add(entry.getValue());
        }

        int affected = jdbc.update(sql.toString(), params.toArray());

        // Record in history
        historyRepo.save(new QueryHistory(
            request.getConnectionId(),
            "UPDATE " + request.getTable() + " SET " + request.getColumn() + " = ? WHERE ...",
            "SUCCESS", System.currentTimeMillis() - start, affected, null
        ));
        eventPublisher.queryExecuted(
            request.getConnectionId(),
            "UPDATE " + request.getTable() + " SET " + request.getColumn() + " = ...",
            "SUCCESS", System.currentTimeMillis() - start, affected, null
        );

        return new QueryResult(affected, System.currentTimeMillis() - start);
    }
}
