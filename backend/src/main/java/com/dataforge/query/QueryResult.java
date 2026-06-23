package com.dataforge.query;

import java.util.List;
import java.util.Map;

public class QueryResult {

    private List<String> columns;
    private List<Map<String, Object>> rows;
    private int affectedRows;
    private long elapsedMs;
    private String error;

    public QueryResult() {}

    public QueryResult(List<String> columns, List<Map<String, Object>> rows, long elapsedMs) {
        this.columns = columns;
        this.rows = rows;
        this.elapsedMs = elapsedMs;
    }

    public QueryResult(String error, long elapsedMs) {
        this.error = error;
        this.elapsedMs = elapsedMs;
    }

    public QueryResult(int affectedRows, long elapsedMs) {
        this.affectedRows = affectedRows;
        this.elapsedMs = elapsedMs;
    }

    public List<String> getColumns() { return columns; }
    public void setColumns(List<String> columns) { this.columns = columns; }
    public List<Map<String, Object>> getRows() { return rows; }
    public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }
    public int getAffectedRows() { return affectedRows; }
    public void setAffectedRows(int affectedRows) { this.affectedRows = affectedRows; }
    public long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(long elapsedMs) { this.elapsedMs = elapsedMs; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
