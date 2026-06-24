package com.dataforge.export;

import com.dataforge.connection.ConnectionConfig;
import com.dataforge.connection.ConnectionRepository;
import com.dataforge.query.DataSourceManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

@Service
public class ExportService {

    private final ConnectionRepository connectionRepo;
    private final DataSourceManager dataSourceManager;

    public ExportService(ConnectionRepository connectionRepo, DataSourceManager dataSourceManager) {
        this.connectionRepo = connectionRepo;
        this.dataSourceManager = dataSourceManager;
    }

    public String exportJson(Long connectionId, String sql) {
        ConnectionConfig config = connectionRepo.findById(connectionId)
            .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + connectionId));
        DataSource ds = dataSourceManager.getOrCreate(config);
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        jdbc.setQueryTimeout(30);

        List<Map<String, Object>> rows = jdbc.query(sql, (ResultSet rs) -> {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            List<Map<String, Object>> result = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= cols; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                result.add(row);
            }
            return result;
        });

        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(rows);
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    public String exportCsv(Long connectionId, String sql) {
        ConnectionConfig config = connectionRepo.findById(connectionId)
            .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + connectionId));
        DataSource ds = dataSourceManager.getOrCreate(config);
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        jdbc.setQueryTimeout(30);

        return jdbc.query(sql, (ResultSet rs) -> {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= cols; i++) {
                if (i > 1) sb.append(',');
                sb.append(escapeCsv(meta.getColumnLabel(i)));
            }
            sb.append('\n');
            while (rs.next()) {
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) sb.append(',');
                    Object val = rs.getObject(i);
                    sb.append(escapeCsv(val != null ? val.toString() : ""));
                }
                sb.append('\n');
            }
            return sb.toString();
        });
    }

    private String escapeCsv(String val) {
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
