package com.dataforge.query;

import com.dataforge.connection.ConnectionConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.MethodName.class)
class QueryHistoryTest {

    private static Long connectionId;

    @Autowired
    private TestRestTemplate rest;

    @BeforeEach
    void ensureTargetDb() {
        if (connectionId == null) {
            ConnectionConfig config = new ConnectionConfig();
            config.setName("History Test");
            config.setDbType("POSTGRESQL");
            config.setHost("localhost");
            config.setPort(0);
            config.setDatabase("jdbc:h2:mem:historytest;DB_CLOSE_DELAY=-1");
            config.setUsername("sa");
            config.setPassword("");

            ResponseEntity<ConnectionConfig> saved = rest.postForEntity("/api/connections", config, ConnectionConfig.class);
            connectionId = saved.getBody().getId();

            QueryRequest create = new QueryRequest();
            create.setConnectionId(connectionId);
            create.setSql("CREATE TABLE IF NOT EXISTS items (id INT PRIMARY KEY, label VARCHAR(100))");
            rest.postForEntity("/api/query", create, QueryResult.class);
        }
    }

    @Test
    void historyRecordsSuccess() {
        QueryRequest req = new QueryRequest();
        req.setConnectionId(connectionId);
        req.setSql("INSERT INTO items VALUES (1, 'test')");
        rest.postForEntity("/api/query", req, QueryResult.class);

        ResponseEntity<QueryHistory[]> res = rest.getForEntity("/api/connections/" + connectionId + "/history", QueryHistory[].class);
        assertThat(res.getBody()).isNotEmpty();
        assertThat(res.getBody()[0].getStatus()).isEqualTo("SUCCESS");
        assertThat(res.getBody()[0].getSql()).contains("INSERT");
    }

    @Test
    void historyRecordsError() {
        QueryRequest req = new QueryRequest();
        req.setConnectionId(connectionId);
        req.setSql("SELECT * FROM nonexistent");

        ResponseEntity<QueryResult> execRes = rest.postForEntity("/api/query", req, QueryResult.class);
        assertThat(execRes.getBody().getError()).isNotNull();

        ResponseEntity<QueryHistory[]> res = rest.getForEntity("/api/connections/" + connectionId + "/history", QueryHistory[].class);
        assertThat(res.getBody()).isNotEmpty();
        assertThat(res.getBody()[0].getStatus()).isEqualTo("ERROR");
    }

    @Test
    void clearHistory() {
        rest.delete("/api/connections/" + connectionId + "/history");

        ResponseEntity<QueryHistory[]> res = rest.getForEntity("/api/connections/" + connectionId + "/history", QueryHistory[].class);
        assertThat(res.getBody()).isEmpty();
    }
}
