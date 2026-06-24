package com.dataforge.security;

import com.dataforge.connection.ConnectionConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TableNameInjectionTest {

    @Autowired
    private TestRestTemplate rest;

    private Long createTestConnection() {
        ConnectionConfig config = new ConnectionConfig();
        config.setName("Injection Test");
        config.setDbType("H2");
        config.setHost("localhost");
        config.setPort(0);
        config.setDatabase("jdbc:h2:mem:injecttest;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");

        ResponseEntity<ConnectionConfig> created = rest.postForEntity("/api/connections", config, ConnectionConfig.class);
        return created.getBody().getId();
    }

    @Test
    void maliciousTableNameInColumnsRejected() {
        Long cid = createTestConnection();

        ResponseEntity<String> res = rest.getForEntity(
            "/api/connections/" + cid + "/schemas/tables/DROP%20TABLE%20users/columns",
            String.class);

        // Should reject the malicious identifier with 4xx
        assertThat(res.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void maliciousTableNameInDataRejected() {
        Long cid = createTestConnection();

        ResponseEntity<String> res = rest.getForEntity(
            "/api/connections/" + cid + "/schemas/tables/DROP%20TABLE%20users/data?limit=10",
            String.class);

        assertThat(res.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void validTableNameSucceeds() {
        Long cid = createTestConnection();

        // First create a valid table
        rest.postForEntity("/api/query",
            Map.of("connectionId", cid, "sql", "CREATE TABLE valid_users (id INT PRIMARY KEY)"),
            String.class);

        ResponseEntity<String> res = rest.getForEntity(
            "/api/connections/" + cid + "/schemas/tables/VALID_USERS/columns",
            String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
