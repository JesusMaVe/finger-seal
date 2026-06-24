package com.dataforge.query;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class InlineEditRequest {

    @NotNull
    private Long connectionId;

    @NotBlank
    private String table;

    @NotNull
    private Map<String, Object> primaryKey;

    @NotBlank
    private String column;

    private Object value;

    public Long getConnectionId() { return connectionId; }
    public void setConnectionId(Long connectionId) { this.connectionId = connectionId; }
    public String getTable() { return table; }
    public void setTable(String table) { this.table = table; }
    public Map<String, Object> getPrimaryKey() { return primaryKey; }
    public void setPrimaryKey(Map<String, Object> primaryKey) { this.primaryKey = primaryKey; }
    public String getColumn() { return column; }
    public void setColumn(String column) { this.column = column; }
    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
}
