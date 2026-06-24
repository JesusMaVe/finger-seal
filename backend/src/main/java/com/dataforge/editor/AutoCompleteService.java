package com.dataforge.editor;

import com.dataforge.connection.ConnectionConfig;
import com.dataforge.connection.ConnectionRepository;
import com.dataforge.query.DataSourceManager;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class AutoCompleteService {

    private final ConnectionRepository connectionRepo;
    private final DataSourceManager dataSourceManager;

    public AutoCompleteService(ConnectionRepository connectionRepo, DataSourceManager dataSourceManager) {
        this.connectionRepo = connectionRepo;
        this.dataSourceManager = dataSourceManager;
    }

    public record SchemaSuggestion(
        String name,      // table or column name
        String type,      // "table", "column", "keyword"
        String parent,    // table name for columns, null for tables
        String schema     // schema name
    ) {}

    public record AutoCompleteResult(List<SchemaSuggestion> suggestions, long elapsedMs) {}

    // ponytail: returns tables + columns for a connection — keywords stay frontend-only
    public AutoCompleteResult getSuggestions(Long connectionId, String partial) {
        long start = System.currentTimeMillis();
        List<SchemaSuggestion> suggestions = new ArrayList<>();

        if (connectionId == null) {
            return new AutoCompleteResult(suggestions, 0);
        }

        ConnectionConfig config = connectionRepo.findById(connectionId).orElse(null);
        if (config == null) {
            return new AutoCompleteResult(suggestions, 0);
        }

        try {
            DataSource ds = dataSourceManager.getOrCreate(config);
            try (Connection conn = ds.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                String schema = resolveSchema(config);
                String upperPartial = partial != null ? partial.toUpperCase() : null;

                // Tables
                try (ResultSet rs = meta.getTables(null, schema, "%", new String[]{"TABLE", "VIEW"})) {
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        if (upperPartial == null || tableName.toUpperCase().contains(upperPartial)) {
                            suggestions.add(new SchemaSuggestion(
                                tableName, "table", null,
                                rs.getString("TABLE_SCHEM")
                            ));
                        }
                    }
                }

                // Columns for each table (iterate over snapshot to avoid ConcurrentModification)
                List<SchemaSuggestion> columnSuggests = new ArrayList<>();
                if (suggestions.size() < 50) {
                    List<SchemaSuggestion> tables = List.copyOf(suggestions);
                    for (SchemaSuggestion table : tables) {
                        try (ResultSet rs = meta.getColumns(null, schema, table.name(), "%")) {
                            while (rs.next()) {
                                String colName = rs.getString("COLUMN_NAME");
                                if (upperPartial == null || colName.toUpperCase().contains(upperPartial)) {
                                    columnSuggests.add(new SchemaSuggestion(
                                        colName, "column", table.name(), schema
                                    ));
                                }
                            }
                        }
                    }
                }
                suggestions.addAll(columnSuggests);
            }
        } catch (SQLException e) {
            // Silent fail — return whatever we collected
        }

        long elapsed = System.currentTimeMillis() - start;
        return new AutoCompleteResult(suggestions, elapsed);
    }

    private String resolveSchema(ConnectionConfig config) {
        return switch (config.getDbType()) {
            case "POSTGRESQL" -> "public";
            case "ORACLE" -> config.getUsername().toUpperCase();
            case "MYSQL" -> config.getDatabase();
            default -> null;
        };
    }
}
