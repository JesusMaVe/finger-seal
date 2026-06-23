package com.dataforge.connection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConnectionControllerTest {

    @Autowired
    private TestRestTemplate rest;

    private ConnectionConfig validConfig() {
        ConnectionConfig c = new ConnectionConfig();
        c.setName("Test PG");
        c.setDbType("POSTGRESQL");
        c.setHost("localhost");
        c.setPort(5432);
        c.setDatabase("testdb");
        c.setUsername("user");
        c.setPassword("pass");
        return c;
    }

    @Test
    void createAndList() {
        ResponseEntity<ConnectionConfig> created = rest.postForEntity("/api/connections", validConfig(), ConnectionConfig.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().getId()).isNotNull();
        Long id = created.getBody().getId();

        ResponseEntity<ConnectionConfig[]> list = rest.getForEntity("/api/connections", ConnectionConfig[].class);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(list.getBody()).hasSize(1);

        ResponseEntity<ConnectionConfig> got = rest.getForEntity("/api/connections/" + id, ConnectionConfig.class);
        assertThat(got.getBody().getName()).isEqualTo("Test PG");

        rest.delete("/api/connections/" + id);

        ResponseEntity<ConnectionConfig> afterDelete = rest.getForEntity("/api/connections/" + id, ConnectionConfig.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testConnectionFailsGracefully() {
        ResponseEntity<String> res = rest.postForEntity("/api/connections/test", validConfig(), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testMissingConnectionReturns404() {
        ResponseEntity<String> res = rest.getForEntity("/api/connections/9999", String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
