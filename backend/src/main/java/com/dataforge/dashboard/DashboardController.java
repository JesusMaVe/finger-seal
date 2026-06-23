package com.dataforge.dashboard;

import com.dataforge.connection.ConnectionConfig;
import com.dataforge.connection.ConnectionRepository;
import com.dataforge.query.DataSourceManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/connections/{id}")
public class DashboardController {

    private final ConnectionRepository connectionRepo;
    private final DataSourceManager dataSourceManager;
    private final JdbcTemplate configJdbc;

    public DashboardController(ConnectionRepository connectionRepo, DataSourceManager dataSourceManager, JdbcTemplate configJdbc) {
        this.connectionRepo = connectionRepo;
        this.dataSourceManager = dataSourceManager;
        this.configJdbc = configJdbc;
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics(@PathVariable Long id) {
        ConnectionConfig config = connectionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + id));
        DataSource ds = dataSourceManager.getOrCreate(config);
        JdbcTemplate jdbc = new JdbcTemplate(ds);

        Map<String, Object> m = new LinkedHashMap<>();
        String dbType = config.getDbType();

        // Active queries / sessions
        try {
            m.put("activeQueries", switch (dbType) {
                case "POSTGRESQL" -> jdbc.queryForObject("SELECT count(*) FROM pg_stat_activity WHERE state = 'active'", Integer.class);
                case "MYSQL" -> jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.PROCESSLIST", Integer.class);
                case "ORACLE" -> jdbc.queryForObject("SELECT COUNT(*) FROM v$session WHERE status = 'ACTIVE'", Integer.class);
                default -> 0;
            });
        } catch (Exception e) {
            m.put("activeQueries", null);
        }

        // Transactions
        try {
            m.put("transactions", switch (dbType) {
                case "POSTGRESQL" -> jdbc.queryForObject("SELECT xact_commit + xact_rollback FROM pg_stat_database WHERE datname = current_database()", Long.class);
                case "MYSQL" -> jdbc.queryForObject("SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME = 'Com_commit'", Long.class);
                case "ORACLE" -> jdbc.queryForObject("SELECT COUNT(*) FROM v$transaction", Long.class);
                default -> 0L;
            });
        } catch (Exception e) {
            m.put("transactions", null);
        }

        // Storage size (bytes)
        try {
            m.put("storageBytes", switch (dbType) {
                case "POSTGRESQL" -> jdbc.queryForObject("SELECT COALESCE(sum(pg_total_relation_size(relid)), 0) FROM pg_stat_user_tables", Long.class);
                case "MYSQL" -> jdbc.queryForObject("SELECT COALESCE(SUM(DATA_LENGTH + INDEX_LENGTH), 0) FROM information_schema.tables WHERE table_schema = DATABASE()", Long.class);
                case "ORACLE" -> jdbc.queryForObject("SELECT COALESCE(SUM(bytes), 0) FROM user_segments", Long.class);
                default -> 0L;
            });
        } catch (Exception e) {
            m.put("storageBytes", null);
        }

        // Table count
        try {
            m.put("tableCount", switch (dbType) {
                case "POSTGRESQL" -> jdbc.queryForObject("SELECT count(*) FROM information_schema.tables WHERE table_schema NOT IN ('pg_catalog','information_schema')", Integer.class);
                case "MYSQL" -> jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()", Integer.class);
                case "ORACLE" -> jdbc.queryForObject("SELECT COUNT(*) FROM user_tables", Integer.class);
                case "SQLITE" -> jdbc.queryForObject("SELECT COUNT(*) FROM sqlite_master WHERE type='table'", Integer.class);
                default -> 0;
            });
        } catch (Exception e) {
            m.put("tableCount", null);
        }

        // Per-table storage breakdown (top 10)
        try {
            List<Map<String, Object>> breakdown = switch (dbType) {
                case "POSTGRESQL" -> jdbc.query("SELECT relname AS name, pg_total_relation_size(relid) AS bytes FROM pg_stat_user_tables ORDER BY bytes DESC LIMIT 10",
                    (rs, i) -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("name", rs.getString("name"));
                        row.put("bytes", rs.getLong("bytes"));
                        return row;
                    });
                case "MYSQL" -> jdbc.query("SELECT TABLE_NAME AS name, (COALESCE(DATA_LENGTH,0) + COALESCE(INDEX_LENGTH,0)) AS bytes FROM information_schema.tables WHERE table_schema = DATABASE() ORDER BY bytes DESC LIMIT 10",
                    (rs, i) -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("name", rs.getString("name"));
                        row.put("bytes", rs.getLong("bytes"));
                        return row;
                    });
                case "ORACLE" -> jdbc.query("SELECT t.table_name AS name, COALESCE(SUM(s.bytes), 0) AS bytes FROM user_tables t LEFT JOIN user_segments s ON s.segment_name = t.table_name GROUP BY t.table_name ORDER BY bytes DESC FETCH FIRST 10 ROWS ONLY",
                    (rs, i) -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("name", rs.getString("name"));
                        row.put("bytes", rs.getLong("bytes"));
                        return row;
                    });
                default -> Collections.emptyList();
            };
            m.put("storageBreakdown", breakdown);
        } catch (Exception e) {
            m.put("storageBreakdown", Collections.emptyList());
        }

        // Query intensity: last 28 days from query history (H2 config DB)
        try {
            List<Map<String, Object>> rawDays = configJdbc.query(
                "SELECT CAST(created_at AS DATE) AS day, COUNT(*) AS cnt FROM query_history WHERE connection_id = ? AND created_at >= CURRENT_DATE - 27 GROUP BY CAST(created_at AS DATE) ORDER BY day",
                (rs, i) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("day", rs.getDate("day").toLocalDate().toString());
                    row.put("cnt", rs.getInt("cnt"));
                    return row;
                },
                id
            );
            // Build 28-day array with 0 for missing days
            List<Map<String, Object>> intensity = new ArrayList<>(28);
            LocalDate today = LocalDate.now();
            Map<String, Integer> dayMap = new HashMap<>();
            for (Map<String, Object> r : rawDays) {
                dayMap.put((String) r.get("day"), (Integer) r.get("cnt"));
            }
            for (int i = 27; i >= 0; i--) {
                LocalDate d = today.minusDays(i);
                String dayStr = d.toString();
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("day", dayStr);
                entry.put("count", dayMap.getOrDefault(dayStr, 0));
                intensity.add(entry);
            }
            m.put("queryIntensity", intensity);
        } catch (Exception e) {
            m.put("queryIntensity", Collections.emptyList());
        }

        return m;
    }
}
