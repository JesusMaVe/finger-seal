# Finger Seal

Cliente de bases de datos moderno — editor SQL, gestor de conexiones, explorador de tablas y dashboard de métricas.

## Tech Stack

| Layer | Stack |
|-------|-------|
| **Frontend** | Vue 3 + Vite + TypeScript + Tailwind CSS v4 |
| **Desktop** | [Tauri v2](https://v2.tauri.app/) (Rust native + keychain OS) |
| **Backend** | Java 21 + Spring Boot 3.4.4 (API REST + WebSocket) |
| **State** | [Pinia](https://pinia.vuejs.org/) |
| **DB soportadas** | PostgreSQL, MySQL, SQLite, Oracle |

## Project Structure

```
.
├── src/                          # Vue 3 frontend
│   ├── main.ts
│   ├── App.vue
│   ├── index.css                 # Tailwind v4 @theme tokens (Material Design 3)
│   ├── store/app.ts              # Pinia store (useAppStore)
│   ├── api/                      # API client (connections, query, schemas, editor, export)
│   ├── tauri/                    # Tauri native helpers (credentials.ts)
│   ├── components/               # AppHeader, AppSidebar, SshTunnelForm
│   └── views/                    # Dashboard, ConnectionManager, TableExplorer, SqlEditor
├── src-tauri/                    # Tauri Rust backend
│   ├── src/
│   │   ├── lib.rs                # Tauri builder + command registration
│   │   ├── commands.rs           # Rust commands (save/get/delete credential via keyring)
│   │   └── main.rs
│   ├── Cargo.toml
│   └── tauri.conf.json
├── backend/                      # Java Spring Boot
│   ├── src/main/java/com/dataforge/
│   │   ├── config/               # CorsConfig, EncryptionService, ObservabilityConfig
│   │   ├── connection/           # ConnectionController/Service/Config + SshTunnelService
│   │   ├── dashboard/            # DashboardController (métricas + heatmap)
│   │   ├── editor/               # SqlEditorController (lint, format, autocomplete)
│   │   ├── export/               # ExportService + ExportController (JSON, CSV)
│   │   ├── query/                # QueryController/Service + DataSourceManager + History
│   │   ├── schema/               # SchemaController (tablas, columnas, FKs, stats)
│   │   └── ws/                   # WebSocket event bus (query logs en tiempo real)
│   └── build.gradle
├── docs/superpowers/plans/       # Implementation plans
├── docker-compose.yml            # Full stack (app + postgres)
├── docker-compose.dev.yml        # Dev only (postgres standalone)
├── Dockerfile.backend
├── ANALISIS-BLUEPRINT.md         # Gap analysis vs architecture blueprint
└── .github/workflows/ci.yml      # CI pipeline
```

## Features

| Feature | Status |
|---------|--------|
| Editor SQL (CodeMirror 6) con syntax highlight | ✅ |
| Linter SQL (JSqlParser) | ✅ |
| Formateador SQL (JSqlParser) | ✅ |
| Autocompletado schema-aware | ✅ |
| Historial de consultas | ✅ |
| Explorador de esquemas (tablas, columnas, FKs, stats) | ✅ |
| Edición inline de datos (doble click → UPDATE) | ✅ |
| Dashboard con métricas + heatmap de queries | ✅ |
| WebSocket event bus en tiempo real | ✅ |
| Exportación CSV + JSON (server-side) | ✅ |
| Gestor de conexiones (CRUD + test) | ✅ |
| SSH tunneling para conexiones remotas | ✅ |
| Cifrado de credenciales en reposo (AES-GCM) | ✅ |
| OS Keychain para contraseñas (Tauri keyring) | ✅ |
| Micrometer + Prometheus endpoint | ✅ |
| Docker Compose (dev + full stack) | ✅ |
| CI/CD (GitHub Actions) | ✅ |
| Multi-DB: PostgreSQL, MySQL, SQLite, Oracle | ✅ |

## Security

- **SQL Injection:** nombres de tabla sanitizados con regex `SAFE_IDENTIFIER`
- **Credenciales en reposo:** cifradas con AES-256/GCM, prefijo `ENC:`
- **Keychain del SO:** contraseñas guardadas en keyring nativo vía Tauri
- **Query timeout:** 30s para queries, 10s para edición inline
- **SSH tunneling:** conexiones remotas seguras vía JSch
- **CORS:** configurado para desarrollo local

## Observability

Metrics endpoint: `http://localhost:8080/actuator/prometheus`

Métricas disponibles:
- `query.execution` — histograma de tiempos de ejecución de queries
- Métricas JVM, heap, threads

## Development

```bash
# Install dependencies
npm install

# Start dev databases (PostgreSQL)
docker compose -f docker-compose.dev.yml up -d

# Set encryption key (generate with: openssl rand -base64 32)
export APP_ENCRYPTION_KEY="your-32-byte-base64-key"

# Start backend
cd backend && ./gradlew bootRun

# Dev server (browser)
npm run dev

# Desktop app (Tauri window)
npm run tauri dev

# Type check
npm run lint

# Production build (frontend only)
npm run build

# Desktop build (native binary)
npm run tauri build
```

## Test Databases

| DB | Port | User | Password | Database |
|----|------|------|----------|----------|
| PostgreSQL | 5432 | `fingerseal` | `fingerseal` | `fingerseal` |

Start with: `docker compose -f docker-compose.dev.yml up -d`

Connection config for the app:
- **PostgreSQL:** host=`localhost`, port=`5432`, db=`fingerseal`, user=`fingerseal`, pass=`fingerseal`
- **SQLite:** db=`/path/to/your/file.db` (no host/port/user/pass needed)

## Environment

Copy `.env.example` to `.env` and add your `GEMINI_API_KEY` if AI features are needed.

## Architecture Notes

- **Path alias**: `@/` → `src/`
- **State**: Pinia via `useAppStore()` from `@/store/app`
- **Routing**: Manual `switch` on `activeView` — no Vue Router
- **Icons**: Google Material Symbols Outlined
- **Fonts**: Plus Jakarta Sans (UI), JetBrains Mono (code)
- **Tauri**: Solo shell nativo — la UI es web app Vue 3
