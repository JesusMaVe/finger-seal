package com.dataforge.export;

import com.dataforge.connection.ConnectionConfig;
import com.dataforge.connection.ConnectionRepository;
import com.dataforge.query.DataSourceManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

/**
 * ponytail: streaming XLSX via SXSSF (keeps low memory footprint).
 * Each row written to temp files, not held in memory.
 * Ceiling: handles millions of rows if the DB cursor supports it.
 */
@Service
public class ExportXlsxService {

    private final ConnectionRepository connectionRepo;
    private final DataSourceManager dataSourceManager;

    public ExportXlsxService(ConnectionRepository connectionRepo, DataSourceManager dataSourceManager) {
        this.connectionRepo = connectionRepo;
        this.dataSourceManager = dataSourceManager;
    }

    public byte[] exportXlsx(Long connectionId, String sql) {
        ConnectionConfig config = connectionRepo.findById(connectionId)
            .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + connectionId));
        DataSource ds = dataSourceManager.getOrCreate(config);
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        jdbc.setQueryTimeout(30);

        SXSSFWorkbook wb = new SXSSFWorkbook(100);
        Sheet sheet = wb.createSheet("Query Results");

        jdbc.query(sql, (ResultSet rs) -> {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();

            Row header = sheet.createRow(0);
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            for (int i = 1; i <= cols; i++) {
                Cell cell = header.createCell(i - 1);
                cell.setCellValue(meta.getColumnLabel(i));
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 1; i <= cols; i++) {
                    Object val = rs.getObject(i);
                    Cell cell = row.createCell(i - 1);
                    if (val == null) {
                        cell.setCellValue("");
                    } else if (val instanceof Number n) {
                        cell.setCellValue(n.doubleValue());
                    } else {
                        cell.setCellValue(val.toString());
                    }
                }
            }
        });

        try (var bos = new java.io.ByteArrayOutputStream()) {
            wb.write(bos);
            wb.dispose();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("XLSX generation failed", e);
        }
    }
}
