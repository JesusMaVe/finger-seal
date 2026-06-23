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
}
