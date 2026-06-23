# Query History Implementation Plan

> **For agentic workers:** Tasks are sequential. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Automatically record every SQL query executed against any connection, with ability to browse and clear history per connection.

**Architecture:** A new `query_history` table in the config H2 database. `QueryService` records each execution (SQL, status, duration, error) after running it. A new controller exposes list/clear endpoints.

**Tech Stack:** Spring Data JDBC, H2

---
### File Structure

| File | Action | Responsibility |
|------|--------|----------------|
| `backend/src/main/resources/schema.sql` | Modify | Add `query_history` table |
| `backend/src/main/java/com/dataforge/query/QueryHistory.java` | Create | Entity: id, connectionId, sql, status, elapsedMs, rowsCount, error, createdAt |
| `backend/src/main/java/com/dataforge/query/QueryHistoryRepository.java` | Create | Spring Data CrudRepository |
| `backend/src/main/java/com/dataforge/query/QueryService.java` | Modify | After execute(), record history entry |
| `backend/src/main/java/com/dataforge/query/QueryHistoryController.java` | Create | GET + DELETE /api/connections/{id}/history |
| `backend/src/test/java/com/dataforge/query/QueryHistoryTest.java` | Create | Test history recording and listing |

---

### Task 1: Entity + Repository + Schema

**Files:**
- Create: `backend/src/main/java/com/dataforge/query/QueryHistory.java`
- Create: `backend/src/main/java/com/dataforge/query/QueryHistoryRepository.java`
- Modify: `backend/src/main/resources/schema.sql`

- [ ] **Step 1: Add table to schema.sql**

```sql
CREATE TABLE IF NOT EXISTS "query_history" (
    "ID" BIGINT AUTO_INCREMENT PRIMARY KEY,
    "CONNECTION_ID" BIGINT NOT NULL,
    "SQL" CLOB NOT NULL,
    "STATUS" VARCHAR(20) NOT NULL,
    "ELAPSED_MS" BIGINT,
    "ROWS_COUNT" INT,
    "ERROR_MSG" CLOB,
    "CREATED_AT" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

- [ ] **Step 2: Create entity**

```java
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
    private String status;    // SUCCESS, ERROR
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
```

- [ ] **Step 3: Create repository**

```java
package com.dataforge.query;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QueryHistoryRepository extends CrudRepository<QueryHistory, Long> {
    List<QueryHistory> findByConnectionIdOrderByCreatedAtDesc(Long connectionId);

    void deleteByConnectionId(Long connectionId);
}
```

- [ ] **Step 4: Build**

Run: `cd backend && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/resources/schema.sql \
      backend/src/main/java/com/dataforge/query/QueryHistory.java \
      backend/src/main/java/com/dataforge/query/QueryHistoryRepository.java
git commit -m "feat(backend): add query history entity + repository"
```

---

### Task 2: Record history in QueryService

**Files:**
- Modify: `backend/src/main/java/com/dataforge/query/QueryService.java`

Inject `QueryHistoryRepository` into `QueryService`. After each `executeQuery()` or `executeUpdate()`, save a history entry. On exception, save an error entry.

- [ ] **Step 1: Modify QueryService**

Add field + constructor param:

```java
private final QueryHistoryRepository historyRepo;

public QueryService(ConnectionRepository connectionRepo, DataSourceManager dataSourceManager, QueryHistoryRepository historyRepo) {
    this.connectionRepo = connectionRepo;
    this.dataSourceManager = dataSourceManager;
    this.historyRepo = historyRepo;
}
```

Modify `execute()` to record history:

```java
public QueryResult execute(QueryRequest request) {
    long start = System.currentTimeMillis();
    ConnectionConfig config = connectionRepo.findById(request.getConnectionId())
            .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + request.getConnectionId()));

    DataSource ds = dataSourceManager.getOrCreate(config);
    JdbcTemplate jdbc = new JdbcTemplate(ds);

    try {
        String sql = request.getSql().trim().toUpperCase();
        QueryResult result;
        if (sql.startsWith("SELECT") || sql.startsWith("WITH") || sql.startsWith("EXPLAIN") || sql.startsWith("DESCRIBE") || sql.startsWith("SHOW")) {
            result = executeQuery(jdbc, request.getSql(), start);
        } else {
            result = executeUpdate(jdbc, request.getSql(), start);
        }
        historyRepo.save(new QueryHistory(
            request.getConnectionId(), request.getSql(), "SUCCESS",
            result.getElapsedMs(),
            result.getRows() != null ? result.getRows().size() : result.getAffectedRows(),
            null
        ));
        return result;
    } catch (Exception e) {
        long elapsed = System.currentTimeMillis() - start;
        QueryResult result = new QueryResult(e.getMessage(), elapsed);
        historyRepo.save(new QueryHistory(
            request.getConnectionId(), request.getSql(), "ERROR",
            elapsed, null, e.getMessage()
        ));
        return result;
    }
}
```

- [ ] **Step 2: Build + test**

Run: `cd backend && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/dataforge/query/QueryService.java
git commit -m "feat(backend): record query history on execution"
```

---

### Task 3: History controller + Frontend wire

**Files:**
- Create: `backend/src/main/java/com/dataforge/query/QueryHistoryController.java`

- [ ] **Step 1: Create controller**

```java
package com.dataforge.query;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/connections/{connectionId}/history")
public class QueryHistoryController {

    private final QueryHistoryRepository repo;

    public QueryHistoryController(QueryHistoryRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<QueryHistory> list(@PathVariable Long connectionId) {
        return repo.findByConnectionIdOrderByCreatedAtDesc(connectionId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@PathVariable Long connectionId) {
        repo.deleteByConnectionId(connectionId);
    }
}
```

- [ ] **Step 2: Build**

Run: `cd backend && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Add frontend API function**

Add to `src/api/query.ts`:

```typescript
export interface QueryHistoryEntry {
  id: number;
  connectionId: number;
  sql: string;
  status: 'SUCCESS' | 'ERROR';
  elapsedMs: number;
  rowsCount?: number;
  errorMsg?: string;
  createdAt: string;
}

export const queryApi = {
  // ... existing execute ...

  history: (connectionId: number) =>
    apiFetch<QueryHistoryEntry[]>(`/connections/${connectionId}/history`),

  clearHistory: (connectionId: number) =>
    apiFetch<void>(`/connections/${connectionId}/history`, { method: 'DELETE' }),
};
```

- [ ] **Step 4: Add history panel to SqlEditorView**

Right now SqlEditorView shows results below the editor. Add a collapsible history panel:

```html
<!-- History toggle button in the toolbar -->
<button @click="showHistory = !showHistory" class="flex items-center gap-xs px-md py-xs text-on-surface-variant hover:text-on-surface transition-all">
  <span class="material-symbols-outlined text-[18px]">history</span>
  History
</button>

<!-- History panel (collapsible) -->
<div v-if="showHistory" class="border-t border-outline-variant bg-surface-container-low">
  <div class="px-md py-sm border-b border-outline-variant flex justify-between items-center">
    <span class="font-label-caps text-label-caps text-on-surface-variant uppercase">Query History</span>
    <button @click="clearHistory" class="text-body-sm text-primary hover:underline">Clear</button>
  </div>
  <div class="max-h-48 overflow-y-auto custom-scrollbar divide-y divide-outline-variant/20">
    <div v-for="entry in history" :key="entry.id" class="px-md py-xs hover:bg-surface-variant transition-colors cursor-pointer" @click="loadHistorySql(entry.sql)">
      <div class="flex justify-between items-start">
        <code class="font-code-sm text-code-sm text-on-surface truncate flex-1">{{ entry.sql }}</code>
        <span class="text-code-xs text-outline shrink-0 ml-sm">{{ entry.elapsedMs }}ms</span>
      </div>
      <div class="flex gap-sm mt-0.5">
        <span class="text-code-xs" :class="entry.status === 'SUCCESS' ? 'text-primary' : 'text-error'">{{ entry.status }}</span>
        <span class="text-code-xs text-outline">{{ new Date(entry.createdAt).toLocaleTimeString() }}</span>
      </div>
    </div>
    <div v-if="history.length === 0" class="px-md py-sm text-center text-outline text-body-sm italic">No queries executed yet</div>
  </div>
</div>
```

Add to script:

```typescript
const showHistory = ref(false)
const history = ref<QueryHistoryEntry[]>([])

watch(selectedConnectionId, async (id) => {
  if (!id) return
  try {
    history.value = await queryApi.history(id)
  } catch { history.value = [] }
})

async function clearHistory() {
  if (!selectedConnectionId.value) return
  await queryApi.clearHistory(selectedConnectionId.value)
  history.value = []
}

function loadHistorySql(sql: string) {
  sql.value = sql
}
```

- [ ] **Step 5: Build frontend**

Run: `cd /Users/jesusmarentes/Documents/finger seal && npx tsc --noEmit`
Expected: No type errors

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/dataforge/query/QueryHistoryController.java src/api/query.ts src/views/SqlEditorView.vue
git commit -m "feat: add query history controller + frontend panel"
```

---

### Task 4: Test history endpoints

**Files:**
- Create: `backend/src/test/java/com/dataforge/query/QueryHistoryTest.java`

- [ ] **Step 1: Create test**

```java
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

            // Create a table so we can run queries
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
        // Most recent should be the error
        assertThat(res.getBody()[0].getStatus()).isEqualTo("ERROR");
    }

    @Test
    void clearHistory() {
        rest.delete("/api/connections/" + connectionId + "/history");

        ResponseEntity<QueryHistory[]> res = rest.getForEntity("/api/connections/" + connectionId + "/history", QueryHistory[].class);
        assertThat(res.getBody()).isEmpty();
    }
}
```

- [ ] **Step 2: Run tests**

Run: `cd backend && ./gradlew test --tests "com.dataforge.query.QueryHistoryTest"`
Expected: BUILD SUCCESSFUL, 3/3 pass

- [ ] **Step 3: Run all tests**

Run: `cd backend && ./gradlew test`
Expected: BUILD SUCCESSFUL, 11/11 pass

- [ ] **Step 4: Commit**

```bash
git add backend/src/test/java/com/dataforge/query/QueryHistoryTest.java
git commit -m "test(backend): add query history integration tests"
```
