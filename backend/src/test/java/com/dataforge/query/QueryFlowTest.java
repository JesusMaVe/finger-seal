package com.dataforge.query;

import com.dataforge.connection.ConnectionConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QueryFlowTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private DataSourceManager dataSourceManager;

    /**
     * Creates a saved connection and returns its ID.
     * Each call uses a unique H2 DB name so tests don't collide.
     */
    private Long createTestConnection(String dbName) {
        ConnectionConfig config = new ConnectionConfig();
        config.setName("H2 " + dbName);
        config.setDbType("POSTGRESQL");
        config.setHost("localhost");
        config.setPort(0);
        config.setDatabase("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");

        ResponseEntity<ConnectionConfig> saved = rest.postForEntity("/api/connections", config, ConnectionConfig.class);
        return saved.getBody().getId();
    }

    /** Executes SQL via direct JDBC (bypasses REST to avoid serialization quirks). */
    private void sql(Long connectionId, String sql) {
        ConnectionConfig cfg = rest.getForEntity("/api/connections/" + connectionId, ConnectionConfig.class).getBody();
        DataSource ds = dataSourceManager.getOrCreate(cfg);
        new JdbcTemplate(ds).execute(sql);
    }

    // --- Tests ---

    @Test
    void selectReturnsQueryResultWithColumnsAndRows() {
        Long cid = createTestConnection("test_select");
        sql(cid, "CREATE TABLE t (id INT PRIMARY KEY, name VARCHAR(100))");
        sql(cid, "INSERT INTO t VALUES (1, 'Alice'), (2, 'Bob')");

        QueryRequest req = new QueryRequest();
        req.setConnectionId(cid);
        req.setSql("SELECT * FROM t");

        ResponseEntity<QueryResult> res = rest.postForEntity("/api/query", req, QueryResult.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().getColumns()).contains("ID", "NAME");
        assertThat(res.getBody().getRows()).isNotEmpty();
    }

    @Test
    void insertReturnsAffectedRowCount() {
        Long cid = createTestConnection("test_insert_count");
        sql(cid, "CREATE TABLE t (id INT PRIMARY KEY)");
        sql(cid, "INSERT INTO t VALUES (10)");

        QueryRequest req = new QueryRequest();
        req.setConnectionId(cid);
        req.setSql("INSERT INTO t VALUES (20)");

        ResponseEntity<QueryResult> res = rest.postForEntity("/api/query", req, QueryResult.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().getAffectedRows()).isEqualTo(1);
    }

    @Test
    void schemaListsTables() {
        Long cid = createTestConnection("test_schema_list");
        sql(cid, "CREATE TABLE schema_test (id INT PRIMARY KEY)");

        ResponseEntity<Map[]> res = rest.getForEntity("/api/connections/" + cid + "/schemas", Map[].class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isNotEmpty();
    }

    @Test
    void schemaListsColumns() {
        Long cid = createTestConnection("test_columns_list");
        sql(cid, "CREATE TABLE cols_test (id INT PRIMARY KEY, name VARCHAR(100))");

        ResponseEntity<Map[]> res = rest.getForEntity(
                "/api/connections/" + cid + "/schemas/tables/COLS_TEST/columns", Map[].class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isNotNull();
    }

    @Test
    void queryErrorReturnsErrorMessageNotException() {
        Long cid = createTestConnection("test_query_error");

        QueryRequest req = new QueryRequest();
        req.setConnectionId(cid);
        req.setSql("SELECT * FROM nonexistent_table");

        ResponseEntity<QueryResult> res = rest.postForEntity("/api/query", req, QueryResult.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().getError()).isNotNull();
    }
}
