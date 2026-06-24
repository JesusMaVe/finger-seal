package com.dataforge.query;

import com.dataforge.connection.ConnectionConfig;
import com.dataforge.connection.ConnectionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.function.Consumer;

/**
 * ponytail: row-by-row streaming via SSE cursor.
 * Each row is flushed as it arrives from the DB cursor.
 * Ceiling: holds no more than one row in memory at a time.
 * Upgrade path: add backpressure if clients can't keep up.
 */
@Service
public class QueryStreamService {

    private final ConnectionRepository connectionRepo;
    private final DataSourceManager dataSourceManager;

    public QueryStreamService(ConnectionRepository connectionRepo, DataSourceManager dataSourceManager) {
        this.connectionRepo = connectionRepo;
        this.dataSourceManager = dataSourceManager;
    }

    public void streamQuery(Long connectionId, String sql,
                            Consumer<String> onRow,
                            Runnable onComplete,
                            Consumer<String> onError) {
        try {
            ConnectionConfig config = connectionRepo.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + connectionId));
            DataSource ds = dataSourceManager.getOrCreate(config);
            JdbcTemplate jdbc = new JdbcTemplate(ds);
            jdbc.setQueryTimeout(30);

            jdbc.query(sql, (ResultSet rs) -> {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();

                // Emit header row (column names)
                StringBuilder header = new StringBuilder("{\"type\":\"header\",\"columns\":[");
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) header.append(',');
                    header.append('"').append(meta.getColumnLabel(i)).append('"');
                }
                header.append("]}");
                onRow.accept(header.toString());

                // Emit data rows
                while (rs.next()) {
                    StringBuilder rowJson = new StringBuilder("{\"type\":\"row\",\"values\":[");
                    for (int i = 1; i <= cols; i++) {
                        if (i > 1) rowJson.append(',');
                        Object val = rs.getObject(i);
                        if (val == null) {
                            rowJson.append("null");
                        } else if (val instanceof Number || val instanceof Boolean) {
                            rowJson.append(val);
                        } else {
                            String escaped = val.toString()
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"")
                                .replace("\n", "\\n")
                                .replace("\r", "\\r")
                                .replace("\t", "\\t");
                            rowJson.append('"').append(escaped).append('"');
                        }
                    }
                    rowJson.append("]}");
                    onRow.accept(rowJson.toString());
                }

                // Emit completion
                onRow.accept("{\"type\":\"complete\"}");
                onComplete.run();
            });
        } catch (Exception e) {
            onError.accept("{\"type\":\"error\",\"message\":\"" +
                e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}
