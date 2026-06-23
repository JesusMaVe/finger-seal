# Backend Tests Implementation Plan

> **For agentic workers:** Tasks are sequential. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Add integration tests for the Spring Boot backend covering connection CRUD, query execution, and schema exploration.

**Architecture:** Tests use `@SpringBootTest` with the embedded H2 (already configured for config persistence). Query/schema tests create a second H2 in-memory database as the "target" database, initialize it with test tables, and run SQL against it.

**Tech Stack:** Spring Boot 3.4, JUnit 5, H2, Testcontainers (not needed — H2 covers all test scenarios)

---
### File Structure

| File | Type | Responsibility |
|------|------|----------------|
| `src/test/java/com/dataforge/connection/ConnectionControllerTest.java` | Create | Test connection CRUD endpoints |
| `src/test/java/com/dataforge/query/QueryFlowTest.java` | Create | Test query execution + schema listing end-to-end |

---

### Task 1: ConnectionControllerTest

**Files:**
- Create: `src/test/java/com/dataforge/connection/ConnectionControllerTest.java`

Test the full CRUD lifecycle plus the test-connection endpoint.

- [ ] **Step 1: Create the test class**

```java
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
        // Create
        ResponseEntity<ConnectionConfig> created = rest.postForEntity("/api/connections", validConfig(), ConnectionConfig.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().getId()).isNotNull();
        Long id = created.getBody().getId();

        // List
        ResponseEntity<ConnectionConfig[]> list = rest.getForEntity("/api/connections", ConnectionConfig[].class);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(list.getBody()).hasSize(1);

        // Get by id
        ResponseEntity<ConnectionConfig> got = rest.getForEntity("/api/connections/" + id, ConnectionConfig.class);
        assertThat(got.getBody().getName()).isEqualTo("Test PG");

        // Delete
        rest.delete("/api/connections/" + id);
        ResponseEntity<ConnectionConfig> afterDelete = rest.getForEntity("/api/connections/" + id, ConnectionConfig.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testConnectionFailsGracefully() {
        // No real PostgreSQL running, but should get 400, not 500
        ResponseEntity<String> res = rest.postForEntity("/api/connections/test", validConfig(), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testMissingConnectionReturns404() {
        ResponseEntity<String> res = rest.getForEntity("/api/connections/9999", String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
```

- [ ] **Step 2: Run the test**

Run: `cd /Users/jesusmarentes/Documents/finger seal/backend && ./gradlew test --tests "com.dataforge.connection.ConnectionControllerTest"`

Expected: `BUILD SUCCESSFUL`, all 3 tests pass

- [ ] **Step 3: Commit**

```bash
git add backend/src/test/java/com/dataforge/connection/ConnectionControllerTest.java
git commit -m "test(backend): add connection CRUD integration tests"
```

---

### Task 2: QueryFlowTest

**Files:**
- Create: `src/test/java/com/dataforge/query/QueryFlowTest.java`

Tests query execution and schema listing against a real H2 in-memory database. This test:
1. Creates a ConnectionConfig that points to a second H2 instance (the "target" DB)
2. Initializes that target H2 with tables and data
3. Executes SELECT/INSERT queries via the QueryController
4. Lists tables/columns via the SchemaController

The trick: we need two H2 databases — one for config storage (auto-configured by Spring), one as the query target.

- [ ] **Step 1: Create the test class**

```java
package com.dataforge.query;

import com.dataforge.connection.ConnectionConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.MethodName.class)
class QueryFlowTest {

    private static Long connectionId;
    private static boolean tablesInitialized;

    @Autowired
    private TestRestTemplate rest;

    @BeforeEach
    void ensureTargetDb() {
        if (connectionId == null) {
            // Create a saved connection pointing to an H2 in-memory DB
            ConnectionConfig config = new ConnectionConfig();
            config.setName("H2 Target");
            config.setDbType("POSTGRESQL");
            config.setHost("localhost");
            config.setPort(0);
            config.setDatabase("jdbc:h2:mem:targetdb;DB_CLOSE_DELAY=-1");
            config.setUsername("sa");
            config.setPassword("");

            ResponseEntity<ConnectionConfig> saved = rest.postForEntity("/api/connections", config, ConnectionConfig.class);
            connectionId = saved.getBody().getId();
        }
        if (!tablesInitialized) {
            // Initialize H2 target DB with test tables via the query endpoint
            tablesInitialized = true;
            QueryRequest create = new QueryRequest();
            create.setConnectionId(connectionId);
            create.setSql("CREATE TABLE IF NOT EXISTS users (id INT PRIMARY KEY, name VARCHAR(100), email VARCHAR(255))");
            rest.postForEntity("/api/query", create, QueryResult.class);

            QueryRequest insert1 = new QueryRequest();
            insert1.setConnectionId(connectionId);
            insert1.setSql("INSERT INTO users VALUES (1, 'Alice', 'alice@test.com')");
            rest.postForEntity("/api/query", insert1, QueryResult.class);

            QueryRequest insert2 = new QueryRequest();
            insert2.setConnectionId(connectionId);
            insert2.setSql("INSERT INTO users VALUES (2, 'Bob', 'bob@test.com')");
            rest.postForEntity("/api/query", insert2, QueryResult.class);
        }
    }

    @Test
    void executeSelectQuery() {
        QueryRequest req = new QueryRequest();
        req.setConnectionId(connectionId);
        req.setSql("SELECT * FROM users ORDER BY id");

        ResponseEntity<QueryResult> res = rest.postForEntity("/api/query", req, QueryResult.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().getColumns()).containsExactly("ID", "NAME", "EMAIL");
        assertThat(res.getBody().getRows()).hasSize(2);
        assertThat(res.getBody().getRows().get(0).get("NAME")).isEqualTo("Alice");
    }

    @Test
    void executeInsertQuery() {
        QueryRequest req = new QueryRequest();
        req.setConnectionId(connectionId);
        req.setSql("INSERT INTO users VALUES (3, 'Charlie', 'charlie@test.com')");

        ResponseEntity<QueryResult> res = rest.postForEntity("/api/query", req, QueryResult.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().getAffectedRows()).isEqualTo(1);
    }

    @Test
    void listSchemas() {
        ResponseEntity<Map[]> res = rest.getForEntity("/api/connections/" + connectionId + "/schemas", Map[].class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void listColumns() {
        ResponseEntity<Map[]> res = rest.getForEntity(
                "/api/connections/" + connectionId + "/schemas/tables/USERS/columns", Map[].class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).hasSize(3);
    }

    @Test
    void queryErrorReturnsGracefully() {
        QueryRequest req = new QueryRequest();
        req.setConnectionId(connectionId);
        req.setSql("SELECT * FROM nonexistent_table");

        ResponseEntity<QueryResult> res = rest.postForEntity("/api/query", req, QueryResult.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().getError()).isNotNull();
    }
}
```

- [ ] **Step 2: Run the tests**

Run: `cd /Users/jesusmarentes/Documents/finger seal/backend && ./gradlew test --tests "com.dataforge.query.QueryFlowTest"`

Expected: `BUILD SUCCESSFUL`, all tests pass

- [ ] **Step 3: Run all tests together**

Run: `cd /Users/jesusmarentes/Documents/finger seal/backend && ./gradlew test`

Expected: `BUILD SUCCESSFUL`, all 8+ tests pass

- [ ] **Step 4: Commit**

```bash
git add backend/src/test/java/com/dataforge/query/QueryFlowTest.java
git commit -m "test(backend): add query execution and schema integration tests"
```
