package com.dataforge.schema;

import com.dataforge.connection.ConnectionConfig;
import com.dataforge.connection.ConnectionRepository;
import com.dataforge.query.DataSourceManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/connections/{id}/schemas")
public class SchemaController {

    private final ConnectionRepository connectionRepo;
    private final DataSourceManager dataSourceManager;

    public SchemaController(ConnectionRepository connectionRepo, DataSourceManager dataSourceManager) {
        this.connectionRepo = connectionRepo;
        this.dataSourceManager = dataSourceManager;
    }

    @GetMapping
    public List<Map<String, Object>> listTables(@PathVariable Long id) {
        ConnectionConfig config = connectionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + id));
        DataSource ds = dataSourceManager.getOrCreate(config);
        JdbcTemplate jdbc = new JdbcTemplate(ds);

        // ponytail: naive catalog/schema — works for MySQL, PG, SQLite
        String sql = switch (config.getDbType()) {
            case "POSTGRESQL" -> """
                SELECT table_schema AS schema_name, table_name, table_type
                FROM information_schema.tables
                WHERE table_schema NOT IN ('pg_catalog', 'information_schema')
                ORDER BY table_schema, table_name
                """;
            case "MYSQL" -> """
                SELECT table_schema AS schema_name, table_name, table_type
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                ORDER BY table_name
                """;
            case "SQLITE" -> "SELECT name AS table_name, type AS table_type FROM sqlite_master WHERE type IN ('table','view') ORDER BY name";
            case "ORACLE" -> "SELECT USER AS \"schema_name\", table_name AS \"table_name\", 'TABLE' AS \"table_type\" FROM user_tables ORDER BY table_name";
            default -> throw new IllegalArgumentException("Unsupported DB type");
        };

        return jdbc.query(sql, (ResultSet rs) -> {
            List<Map<String, Object>> tables = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= cols; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                tables.add(row);
            }
            return tables;
        });
    }

    @GetMapping("/tables/{tableName}/columns")
    public List<Map<String, Object>> tableColumns(@PathVariable Long id, @PathVariable String tableName) throws SQLException {
        ConnectionConfig config = connectionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + id));
        DataSource ds = dataSourceManager.getOrCreate(config);
        List<Map<String, Object>> cols = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            var meta = conn.getMetaData();
            String schema = config.getDbType().equals("POSTGRESQL") ? "public" : null;
            try (var rs = meta.getColumns(null, schema, tableName, null)) {
                while (rs.next()) {
                    Map<String, Object> col = new LinkedHashMap<>();
                    col.put("name", rs.getString("COLUMN_NAME"));
                    col.put("type", rs.getString("TYPE_NAME"));
                    col.put("nullable", rs.getString("IS_NULLABLE"));
                    col.put("default", rs.getString("COLUMN_DEF"));
                    col.put("size", rs.getInt("COLUMN_SIZE"));
                    cols.add(col);
                }
            }
        }
        return cols;
    }

    @GetMapping("/tables/{tableName}/data")
    public List<Map<String, Object>> tableData(@PathVariable Long id, @PathVariable String tableName,
            @RequestParam(defaultValue = "100") int limit) {
        ConnectionConfig config = connectionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + id));
        DataSource ds = dataSourceManager.getOrCreate(config);
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        String sql = switch (config.getDbType()) {
            case "ORACLE" -> "SELECT * FROM " + tableName + " FETCH FIRST " + limit + " ROWS ONLY";
            default -> "SELECT * FROM " + tableName + " LIMIT " + limit;
        };
        return jdbc.query(sql, (ResultSet rs) -> {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }
            return rows;
        });
    }

    @GetMapping("/tables/{tableName}/stats")
    public Map<String, Object> tableStats(@PathVariable Long id, @PathVariable String tableName) {
        ConnectionConfig config = connectionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + id));
        DataSource ds = dataSourceManager.getOrCreate(config);
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        String sql = switch (config.getDbType()) {
            case "POSTGRESQL" -> "SELECT reltuples::bigint AS row_count, pg_size_pretty(pg_total_relation_size(?)) AS total_size, pg_size_pretty(pg_indexes_size(?)) AS index_size FROM pg_class WHERE relname = ?";
            case "MYSQL" -> "SELECT TABLE_ROWS AS row_count, ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 1) AS total_size_mb, ROUND(INDEX_LENGTH / 1024 / 1024, 1) AS index_size_mb FROM information_schema.tables WHERE TABLE_NAME = ? AND TABLE_SCHEMA = DATABASE()";
            case "SQLITE" -> "SELECT COUNT(*) AS row_count, 'N/A' AS total_size FROM " + tableName;
            case "ORACLE" -> "SELECT num_rows AS \"row_count\", 'N/A' AS \"total_size\" FROM user_tables WHERE table_name = ?";
            default -> throw new IllegalArgumentException("Unsupported DB type");
        };
        if (config.getDbType().equals("POSTGRESQL")) {
            return jdbc.queryForMap(sql, tableName, tableName, tableName);
        }
        return jdbc.queryForMap(sql, tableName);
    }

    @GetMapping("/tables/{tableName}/foreign-keys")
    public List<Map<String, Object>> tableForeignKeys(@PathVariable Long id, @PathVariable String tableName) throws SQLException {
        ConnectionConfig config = connectionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + id));
        DataSource ds = dataSourceManager.getOrCreate(config);
        List<Map<String, Object>> fks = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            var meta = conn.getMetaData();
            String schema = config.getDbType().equals("POSTGRESQL") ? "public" : null;
            try (var rs = meta.getImportedKeys(null, schema, tableName)) {
                while (rs.next()) {
                    Map<String, Object> fk = new LinkedHashMap<>();
                    fk.put("pk_table", rs.getString("PKTABLE_NAME"));
                    fk.put("pk_column", rs.getString("PKCOLUMN_NAME"));
                    fk.put("fk_column", rs.getString("FKCOLUMN_NAME"));
                    fk.put("fk_name", rs.getString("FK_NAME"));
                    fks.add(fk);
                }
            }
        }
        return fks;
    }
}
