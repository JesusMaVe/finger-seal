package com.dataforge.export;

import com.dataforge.connection.ConnectionConfig;
import com.dataforge.connection.ConnectionRepository;
import com.dataforge.query.DataSourceManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

/**
 * Generates INSERT statements from query results.
 * Useful for data migration or backup scenarios.
 */
@Service
public class ExportSqlService {

    private final ConnectionRepository connectionRepo;
    private final DataSourceManager dataSourceManager;

    public ExportSqlService(ConnectionRepository connectionRepo, DataSourceManager dataSourceManager) {
        this.connectionRepo = connectionRepo;
        this.dataSourceManager = dataSourceManager;
    }

    public String exportSql(Long connectionId, String sql, String tableName) {
        ConnectionConfig config = connectionRepo.findById(connectionId)
            .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + connectionId));
        DataSource ds = dataSourceManager.getOrCreate(config);
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        jdbc.setQueryTimeout(30);

        StringBuilder sb = new StringBuilder();
        String safeTable = tableName != null ? tableName : "exported_data";

        jdbc.query(sql, (ResultSet rs) -> {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();

            while (rs.next()) {
                sb.append("INSERT INTO ").append(safeTable).append(" (");
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) sb.append(", ");
                    sb.append(meta.getColumnLabel(i));
                }
                sb.append(") VALUES (");
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) sb.append(", ");
                    Object val = rs.getObject(i);
                    if (val == null) {
                        sb.append("NULL");
                    } else if (val instanceof Number || val instanceof Boolean) {
                        sb.append(val);
                    } else {
                        String escaped = val.toString().replace("'", "''");
                        sb.append("'").append(escaped).append("'");
                    }
                }
                sb.append(");\n");
            }
        });

        return sb.toString();
    }
}
