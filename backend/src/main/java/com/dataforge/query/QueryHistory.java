package com.dataforge.query;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("query_history")
public class QueryHistory {

    @Id
    private Long id;
    private Long connectionId;
    private String sql;
    private String status;
    private Long elapsedMs;
    private Integer rowsCount;
    private String errorMsg;
    private LocalDateTime createdAt;

    public QueryHistory() {}

    public QueryHistory(Long connectionId, String sql, String status, Long elapsedMs, Integer rowsCount, String errorMsg) {
        this.connectionId = connectionId;
        this.sql = sql;
        this.status = status;
        this.elapsedMs = elapsedMs;
        this.rowsCount = rowsCount;
        this.errorMsg = errorMsg;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConnectionId() { return connectionId; }
    public void setConnectionId(Long connectionId) { this.connectionId = connectionId; }
    public String getSql() { return sql; }
    public void setSql(String sql) { this.sql = sql; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(Long elapsedMs) { this.elapsedMs = elapsedMs; }
    public Integer getRowsCount() { return rowsCount; }
    public void setRowsCount(Integer rowsCount) { this.rowsCount = rowsCount; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
