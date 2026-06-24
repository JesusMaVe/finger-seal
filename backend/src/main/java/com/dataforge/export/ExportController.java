package com.dataforge.export;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @PostMapping("/json")
    public ResponseEntity<String> exportJson(@RequestBody Map<String, Object> request) {
        Long connectionId = Long.valueOf(request.get("connectionId").toString());
        String sql = (String) request.get("sql");
        String json = exportService.exportJson(connectionId, sql);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=export.json")
            .body(json);
    }

    @PostMapping("/csv")
    public ResponseEntity<String> exportCsv(@RequestBody Map<String, Object> request) {
        Long connectionId = Long.valueOf(request.get("connectionId").toString());
        String sql = (String) request.get("sql");
        String csv = exportService.exportCsv(connectionId, sql);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("text/csv"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=export.csv")
            .body(csv);
    }

    @PostMapping("/xlsx")
    public ResponseEntity<byte[]> exportXlsx(@RequestBody Map<String, Object> request) {
        Long connectionId = Long.valueOf(request.get("connectionId").toString());
        String sql = (String) request.get("sql");
        byte[] data = exportService.exportXlsx(connectionId, sql);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=export.xlsx")
            .body(data);
    }

    @PostMapping("/sql")
    public ResponseEntity<String> exportSql(@RequestBody Map<String, Object> request) {
        Long connectionId = Long.valueOf(request.get("connectionId").toString());
        String sql = (String) request.get("sql");
        String tableName = (String) request.get("tableName");
        String result = exportService.exportSql(connectionId, sql, tableName);
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=export.sql")
            .body(result);
    }
}
