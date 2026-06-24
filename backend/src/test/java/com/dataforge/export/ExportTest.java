package com.dataforge.export;

import com.dataforge.connection.ConnectionConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExportTest {

    @Autowired
    private TestRestTemplate rest;

    private Long cid;

    @BeforeEach
    void setup() {
        ConnectionConfig config = new ConnectionConfig();
        config.setName("Export Test DB");
        config.setDbType("H2");
        config.setHost("mem");
        config.setPort(0);
        config.setDatabase("jdbc:h2:mem:exporttest;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");

        ResponseEntity<ConnectionConfig> saved = rest.postForEntity("/api/connections", config, ConnectionConfig.class);
        cid = saved.getBody().getId();

        // Create table + seed data
        rest.postForEntity("/api/query", Map.of("connectionId", cid, "sql",
            "CREATE TABLE export_data (id INT PRIMARY KEY, name VARCHAR(50), amount DECIMAL(10,2))"),
            String.class);
        rest.postForEntity("/api/query", Map.of("connectionId", cid, "sql",
            "INSERT INTO export_data VALUES (1, 'Alice', 100.50)"), String.class);
        rest.postForEntity("/api/query", Map.of("connectionId", cid, "sql",
            "INSERT INTO export_data VALUES (2, 'Bob', 200.75)"), String.class);
    }

    @Test
    void exportJson() {
        ResponseEntity<String> res = rest.postForEntity("/api/export/json",
            Map.of("connectionId", cid, "sql", "SELECT * FROM export_data"), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("\"ID\" : 1");
        assertThat(res.getBody()).contains("\"NAME\" : \"Alice\"");
        assertThat(res.getBody()).contains("\"AMOUNT\" : 100.50");
    }

    @Test
    void exportCsv() {
        ResponseEntity<String> res = rest.postForEntity("/api/export/csv",
            Map.of("connectionId", cid, "sql", "SELECT * FROM export_data"), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = res.getBody();
        assertThat(body).contains("ID,NAME,AMOUNT");
        assertThat(body).contains("1,Alice,100.50");
    }
}
