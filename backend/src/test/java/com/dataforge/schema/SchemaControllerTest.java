package com.dataforge.schema;

import com.dataforge.connection.ConnectionConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SchemaControllerTest {

    @Autowired
    private TestRestTemplate rest;

    private Long createTestConnection() {
        ConnectionConfig config = new ConnectionConfig();
        config.setName("Schema Test");
        config.setDbType("H2");
        config.setHost("localhost");
        config.setPort(0);
        config.setDatabase("jdbc:h2:mem:schematest;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");

        ResponseEntity<ConnectionConfig> saved = rest.postForEntity("/api/connections", config, ConnectionConfig.class);
        Long cid = saved.getBody().getId();

        rest.postForEntity("/api/query", Map.of("connectionId", cid, "sql",
            "CREATE TABLE schema_test_table (id INT PRIMARY KEY, label VARCHAR(50), created_at TIMESTAMP)"),
            String.class);

        return cid;
    }

    @Test
    void listTables() {
        Long cid = createTestConnection();
        ResponseEntity<List> res = rest.getForEntity("/api/connections/" + cid + "/schemas", List.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isNotEmpty();
    }

    @Test
    void tableColumns() {
        Long cid = createTestConnection();
        ResponseEntity<List> res = rest.getForEntity(
            "/api/connections/" + cid + "/schemas/tables/SCHEMA_TEST_TABLE/columns", List.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).hasSize(3);
    }

    @Test
    void tableData() {
        Long cid = createTestConnection();
        ResponseEntity<List> res = rest.getForEntity(
            "/api/connections/" + cid + "/schemas/tables/SCHEMA_TEST_TABLE/data?limit=10", List.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
