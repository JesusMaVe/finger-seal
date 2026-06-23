# Frontend-Backend Integration Plan

> **For agentic workers:** Tasks are sequential. Each produces working, testable software.

**Goal:** Connect all Vue 3 views to the Spring Boot backend so the app works with real data instead of hardcoded values.

**Architecture:** The Vue 3 frontend (Tauri + Vite) talks to the Spring Boot REST API (port 8080) via plain `fetch()`. The backend persists connection configs in an embedded H2 database and dynamically creates HikariCP pools to execute SQL against PostgreSQL/MySQL/SQLite.

**Tech Stack:** Vue 3 (fetch), Spring Boot 3.4 (REST), Tauri v2

---
## File Structure — New & Modified

### Frontend (src/)
| File | Action | Responsibility |
|------|--------|----------------|
| `src/api/client.ts` | **Create** | Base fetch wrapper: base URL, headers, error handling |
| `src/api/connections.ts` | **Create** | Typed functions for `/api/connections/*` |
| `src/api/query.ts` | **Create** | Typed function for `POST /api/query` |
| `src/api/schemas.ts` | **Create** | Typed functions for schema endpoints |
| `src/views/ConnectionManagerView.vue` | **Modify** | Bind form to API, remove hardcoded values |
| `src/views/SqlEditorView.vue` | **Modify** | Execute real queries via API, remove mockResults |
| `src/views/TableExplorerView.vue` | **Modify** | Fetch schema + data preview from API, remove hardcoded columns/activities/preview |
| `src/views/DashboardView.vue` | **Modify** | Fetch metrics from API or hold as empty state |
| `src/components/AppSidebar.vue` | **Modify** | Dynamic table list from API |

### Backend (backend/)
| File | Action | Responsibility |
|------|--------|----------------|
| `src/main/java/com/dataforge/config/CorsConfig.java` | **Create** | Allow `localhost:3000` in dev |
| `src/main/java/com/dataforge/schema/SchemaController.java` | **Modify** | Add table detail + data preview + stats endpoints |

---

## Task 1: CORS Configuration (Backend)

**Files:**
- Create: `backend/src/main/java/com/dataforge/config/CorsConfig.java`

- [ ] **Step 1: Create CORS config**

```java
package com.dataforge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

- [ ] **Step 2: Build and verify**

Run: `cd backend && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/dataforge/config/CorsConfig.java
git commit -m "feat(backend): add CORS config for frontend dev"
```

---

## Task 2: Create API Client (Frontend)

**Files:**
- Create: `src/api/client.ts`
- Create: `src/api/connections.ts`
- Create: `src/api/query.ts`
- Create: `src/api/schemas.ts`

- [ ] **Step 1: Create base client**

```typescript
// src/api/client.ts
const BASE_URL = 'http://localhost:8080/api';

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

export async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    ...options,
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new ApiError(res.status, text || res.statusText);
  }
  const ct = res.headers.get('content-type');
  if (ct && ct.includes('application/json')) return res.json();
  return undefined as T;
}
```

- [ ] **Step 2: Create connections API**

```typescript
// src/api/connections.ts
import { apiFetch } from './client';

export interface ConnectionConfig {
  id?: number;
  name: string;
  dbType: 'POSTGRESQL' | 'MYSQL' | 'SQLITE';
  host: string;
  port: number;
  database: string;
  username: string;
  password: string;
}

export const connectionsApi = {
  list: () => apiFetch<ConnectionConfig[]>('/connections'),
  get: (id: number) => apiFetch<ConnectionConfig>(`/connections/${id}`),
  create: (cfg: ConnectionConfig) =>
    apiFetch<ConnectionConfig>('/connections', { method: 'POST', body: JSON.stringify(cfg) }),
  delete: (id: number) =>
    apiFetch<void>(`/connections/${id}`, { method: 'DELETE' }),
  test: (cfg: ConnectionConfig) =>
    apiFetch<void>('/connections/test', { method: 'POST', body: JSON.stringify(cfg) }),
  testExisting: (id: number) =>
    apiFetch<void>(`/connections/${id}/test`, { method: 'POST' }),
};
```

- [ ] **Step 3: Create query API**

```typescript
// src/api/query.ts
import { apiFetch } from './client';

export interface QueryResult {
  columns?: string[];
  rows?: Record<string, unknown>[];
  affectedRows?: number;
  elapsedMs: number;
  error?: string;
}

export const queryApi = {
  execute: (connectionId: number, sql: string) =>
    apiFetch<QueryResult>('/query', {
      method: 'POST',
      body: JSON.stringify({ connectionId, sql }),
    }),
};
```

- [ ] **Step 4: Create schemas API**

```typescript
// src/api/schemas.ts
import { apiFetch } from './client';

export interface TableInfo {
  schema_name?: string;
  table_name: string;
  table_type: string;
}

export interface ColumnInfo {
  name: string;
  type: string;
  nullable: string;
  default: string;
  constraint?: string;
}

export const schemasApi = {
  listTables: (connectionId: number) =>
    apiFetch<TableInfo[]>(`/connections/${connectionId}/schemas`),
};
```

- [ ] **Step 5: Commit**

```bash
git add src/api/
git commit -m "feat(frontend): add API client layer"
```

---

## Task 3: Wire ConnectionManagerView

**Files:**
- Modify: `src/views/ConnectionManagerView.vue`

**Changes:**
1. Replace all hardcoded `value` attributes in inputs with `v-model` bound to a reactive config object
2. Add a `dbType` selector that's actually reactive (currently uses visual buttons without state)
3. Wire "Test Connection" button to `connectionsApi.test()`
4. Wire "Connect" button to `connectionsApi.create()`
5. Show saved connections list below the form
6. Add a "select connection" flow for the sidebar

This is the most complex view to rewire. Full rewrite of the `<script>` section and partial rewrite of `<template>` to bind data.

- [ ] **Step 1: Rewrite script section**

```typescript
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { connectionsApi, type ConnectionConfig } from '@/api/connections'

const form = ref<ConnectionConfig>({
  name: '',
  dbType: 'POSTGRESQL',
  host: '',
  port: 5432,
  database: '',
  username: '',
  password: '',
})

const connections = ref<ConnectionConfig[]>([])
const testing = ref(false)
const saving = ref(false)
const error = ref('')
const success = ref('')

onMounted(() => connectionsApi.list().then(data => connections.value = data).catch(() => {}))

async function testConnection() {
  testing.value = true
  error.value = ''
  success.value = ''
  try {
    await connectionsApi.test(form.value)
    success.value = 'Connection successful!'
  } catch (e: any) {
    error.value = e.message || 'Connection failed'
  } finally {
    testing.value = false
  }
}

async function saveConnection() {
  saving.value = true
  error.value = ''
  success.value = ''
  try {
    const saved = await connectionsApi.create(form.value)
    connections.value.push(saved)
    success.value = 'Connection saved!'
  } catch (e: any) {
    error.value = e.message || 'Failed to save'
  } finally {
    saving.value = false
  }
}

function selectDbType(type: ConnectionConfig['dbType']) {
  form.value.dbType = type
  if (type === 'POSTGRESQL') form.value.port = 5432
  else if (type === 'MYSQL') form.value.port = 3306
  else form.value.port = 0
}
</script>
```

- [ ] **Step 2: Update template to bind v-model**

The hardcoded `value="Production Core DB"` becomes `v-model="form.name"`, etc. The DB type buttons call `selectDbType('POSTGRESQL')` etc. Add error/success messages.

Add saved connections list after the form.

- [ ] **Step 3: Build and verify**

Run: `npm run lint`
Expected: No type errors

- [ ] **Step 4: Commit**

```bash
git add src/views/ConnectionManagerView.vue
git commit -m "feat(frontend): wire ConnectionManagerView to backend API"
```

---

## Task 4: Wire SqlEditorView

**Files:**
- Modify: `src/views/SqlEditorView.vue`

**Changes:**
1. Replace `mockResults` with a `results` ref that comes from `queryApi.execute()`
2. Add a connection selector (dropdown to pick which saved connection to query against)
3. Fix: the `contenteditable` SQL editor div won't work well — replace with a `<textarea>` or capture its content on "Run". Simplest: use `<textarea>` for SQL input.
4. Wire the "Run" button to execute the query and display results in the table
5. Show elapsed time and row count from the real response

- [ ] **Step 1: Rewrite script section**

```typescript
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { queryApi, type QueryResult } from '@/api/query'
import { connectionsApi, type ConnectionConfig } from '@/api/connections'

const connections = ref<ConnectionConfig[]>([])
const selectedConnectionId = ref<number | null>(null)
const sql = ref(`SELECT\n  u.id,\n  u.username,\n  u.email\nFROM users u\nLIMIT 100;`)
const results = ref<QueryResult | null>(null)
const running = ref(false)

onMounted(() => connectionsApi.list().then(data => connections.value = data).catch(() => {}))

async function runQuery() {
  if (!selectedConnectionId.value || !sql.value.trim()) return
  running.value = true
  results.value = null
  try {
    results.value = await queryApi.execute(selectedConnectionId.value, sql.value)
  } catch (e: any) {
    results.value = { error: e.message, elapsedMs: 0 }
  } finally {
    running.value = false
  }
}
</script>
```

- [ ] **Step 2: Update template**

Replace the hardcoded SQL content with the `sql` ref, replace `mockResults` with `results.rows`, add a `<select>` for connection picker, show `results.error` if present.

- [ ] **Step 3: Build and verify**

Run: `npm run lint`
Expected: No type errors

- [ ] **Step 4: Commit**

```bash
git add src/views/SqlEditorView.vue
git commit -m "feat(frontend): wire SqlEditorView to query API"
```

---

## Task 5: Wire TableExplorerView

**Files:**
- Modify: `src/views/TableExplorerView.vue`
- Modify: `backend/.../schema/SchemaController.java`

**Changes (backend):**
Add endpoints for table detail + data preview.

**Changes (frontend):**
1. Replace hardcoded `columns` array with data from backend `GET /api/connections/{id}/tables/{tableName}/columns`
2. Replace hardcoded data preview rows with live data
3. Replace hardcoded `activities` with real data or empty state
4. Replace hardcoded stats with data from backend
5. Add connection + table selector

- [ ] **Step 1: Add backend table endpoints**

Add these methods to `SchemaController.java`:

**Table columns** — uses `DatabaseMetaData.getColumns()`:

```java
@GetMapping("/tables/{tableName}/columns")
public List<Map<String, Object>> tableColumns(@PathVariable Long id, @PathVariable String tableName) throws SQLException {
    ConnectionConfig config = connectionRepo.findById(id).orElseThrow();
    DataSource ds = dataSourceManager.getOrCreate(config);
    List<Map<String, Object>> cols = new ArrayList<>();
    try (Connection conn = ds.getConnection()) {
        var meta = conn.getMetaData();
        String schema = config.getDbType().equals("POSTGRESQL") ? "public" : null;
        try (var rs = meta.getColumns(null, schema, tableName, null)) {
            while (rs.next()) {
                Map<String, Object> col = new LinkedHashMap<>();
                col.put("name", rs.getString("COLUMN_NAME"));
                col.put("type", rs.getString("TYPE_NAME"));
                col.put("nullable", rs.getString("IS_NULLABLE"));
                col.put("default", rs.getString("COLUMN_DEF"));
                col.put("size", rs.getInt("COLUMN_SIZE"));
                cols.add(col);
            }
        }
    }
    return cols;
}
```

**Table data preview:**

```java
@GetMapping("/tables/{tableName}/data")
public List<Map<String, Object>> tableData(@PathVariable Long id, @PathVariable String tableName,
    @RequestParam(defaultValue = "100") int limit) {
    ConnectionConfig config = connectionRepo.findById(id).orElseThrow();
    DataSource ds = dataSourceManager.getOrCreate(config);
    JdbcTemplate jdbc = new JdbcTemplate(ds);
    String sql = "SELECT * FROM " + tableName + " LIMIT " + limit;
    return jdbc.query(sql, (ResultSet rs) -> {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        List<Map<String, Object>> rows = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= colCount; i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    });
}
```

**Table stats:**

```java
@GetMapping("/tables/{tableName}/stats")
public Map<String, Object> tableStats(@PathVariable Long id, @PathVariable String tableName) {
    ConnectionConfig config = connectionRepo.findById(id).orElseThrow();
    DataSource ds = dataSourceManager.getOrCreate(config);
    JdbcTemplate jdbc = new JdbcTemplate(ds);
    String countSql = switch (config.getDbType()) {
        case "POSTGRESQL" -> "SELECT reltuples::bigint AS row_count, pg_size_pretty(pg_total_relation_size(?'?)) AS total_size FROM pg_class WHERE relname = ?";
        case "MYSQL" -> "SELECT TABLE_ROWS AS row_count, ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 1) || ' MB' AS total_size FROM information_schema.tables WHERE TABLE_NAME = ?";
        case "SQLITE" -> "SELECT COUNT(*) AS row_count, 'N/A' AS total_size FROM " + tableName;
        default -> throw new IllegalArgumentException();
    };
    return jdbc.queryForMap(countSql, tableName);
}
```

- [ ] **Step 2: Build backend**

Run: `cd backend && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Create frontend schema detail API**

Add to `src/api/schemas.ts`:

```typescript
export const schemasApi = {
  // ... existing listTables ...

  tableColumns: (connectionId: number, tableName: string) =>
    apiFetch<ColumnInfo[]>(`/connections/${connectionId}/schemas/tables/${tableName}/columns`),

  tableData: (connectionId: number, tableName: string, limit = 100) =>
    apiFetch<Record<string, unknown>[]>(`/connections/${connectionId}/schemas/tables/${tableName}/data?limit=${limit}`),

  tableStats: (connectionId: number, tableName: string) =>
    apiFetch<Record<string, unknown>>(`/connections/${connectionId}/schemas/tables/${tableName}/stats`),
};
```

- [ ] **Step 4: Rewrite TableExplorerView script**

Replace hardcoded `columns`, `activities`, preview rows with API calls. Add connection + table selectors.

- [ ] **Step 5: Build and verify**

Run: `npm run lint`
Expected: No type errors

- [ ] **Step 6: Commit**

```bash
git add src/views/TableExplorerView.vue src/api/schemas.ts
git commit -m "feat(frontend): wire TableExplorerView to schema API"
```

---

## Task 6: Wire AppSidebar (Dynamic Table List)

**Files:**
- Modify: `src/components/AppSidebar.vue`

**Changes:**
1. Replace hardcoded table sub-list with data from API
2. Add a connection selector at the top (currently hardcoded "Localhost")
3. Fetch table list when a connection is selected

- [ ] **Step 1: Rewrite script**

```typescript
<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { activeView } from '@/store/app'
import { connectionsApi, type ConnectionConfig } from '@/api/connections'
import { schemasApi, type TableInfo } from '@/api/schemas'

const connections = ref<ConnectionConfig[]>([])
const selectedConnectionId = ref<number | null>(null)
const tables = ref<TableInfo[]>([])

onMounted(() => connectionsApi.list().then(data => {
  connections.value = data
  if (data.length > 0) selectConnection(data[0].id!)
}).catch(() => {}))

function selectConnection(id: number) {
  selectedConnectionId.value = id
  schemasApi.listTables(id).then(data => tables.value = data).catch(() => {})
}
</script>
```

- [ ] **Step 2: Update template**

Replace hardcoded "Localhost / PostgreSQL 15.2" with selected connection info. Replace hardcoded table list with `v-for` over `tables`.

- [ ] **Step 3: Build and verify**

Run: `npm run lint`
Expected: No type errors

- [ ] **Step 4: Commit**

```bash
git add src/components/AppSidebar.vue
git commit -m "feat(frontend): wire sidebar table list to API"
```

---

## Task 7: Wire DashboardView

**Files:**
- Modify: `src/views/DashboardView.vue`

**Changes:**
1. The dashboard currently displays hardcoded telemetry (CPU 12.4%, RAM 4.2/16GB, 142 connections, 840 IOPS)
2. Either create a backend metrics endpoint, or keep dashboard as an empty state placeholder
3. Simplest: replace hardcoded numbers with reactive refs, fetch from backend if endpoint exists, otherwise show "No data"

- [ ] **Step 1: Create backend metrics endpoint (optional)**

```java
@GetMapping("/api/connections/{id}/metrics")
public Map<String, Object> metrics(@PathVariable Long id) { ... }
```

Queries the database for: connection count, database size, active queries, etc. using information_schema.

- [ ] **Step 2: Wire DashboardView**

Replace hardcoded values with API data or empty state placeholders.

- [ ] **Step 3: Build and verify**

Run: `npm run lint`
Expected: No type errors

- [ ] **Step 4: Commit**

```bash
git add src/views/DashboardView.vue
git commit -m "feat(frontend): wire DashboardView to API or set empty state"
```

---

## Task 8: Tauri Dev Test

**Files:**
- No changes

- [ ] **Step 1: Start backend**

Run: `cd backend && ./gradlew bootRun`
Expected: Starts on port 8080

- [ ] **Step 2: Start Tauri dev**

Run: `cd .. && npm run tauri dev`
Expected: Tauri window opens, app loads on `http://localhost:3000`, API calls succeed.

- [ ] **Step 3: Create a test connection via UI**

Fill in connection form, click "Test Connection" (should fail gracefully if no DB running), then save.

- [ ] **Step 4: Commit final**

```bash
git add -A
git commit -m "chore: full frontend-backend integration"
```
