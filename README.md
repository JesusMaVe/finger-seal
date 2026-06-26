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
| Exportación CSV + JSON + XLSX + SQL INSERT | ✅ |
| SSE streaming de resultados de queries | ✅ |
| HashiCorp Vault integration (secrets management) | ✅ |
| OpenTelemetry tracing (manual spans + OTel agent) | ✅ |
| Grafana dashboard (latency, throughput, errors) | ✅ |
| Playwright E2E smoke tests | ✅ |
| Testcontainers integration tests (PostgreSQL) | ✅ |
| WebSocket event broadcast tests | ✅ |
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
- **HashiCorp Vault:** secrets management opcional, deshabilitado por defecto
- **Query timeout:** 30s para queries, 10s para edición inline
- **SSH tunneling:** conexiones remotas seguras vía JSch
- **CORS:** configurado para desarrollo local

### HashiCorp Vault

```bash
docker compose -f docker-compose.vault.yml up -d
scripts/vault-init.sh
```

Set `vault.enabled: true` in config to activate.
Docs: `docs/superpowers/vault-setup.md`

## Observability

Metrics endpoint: `http://localhost:8080/actuator/prometheus`

Métricas disponibles:
- `query.execution` — histograma de tiempos de ejecución de queries
- Métricas JVM, heap, threads

### Observability Stack

```bash
docker compose -f docker-compose.observability.yml up -d
```

| Service | URL |
|---------|-----|
| Grafana | http://localhost:3001 (admin/admin) |
| Prometheus | http://localhost:9090 |
| Loki | http://localhost:3100 |
| Tempo (OTLP) | :4317 gRPC / :4318 HTTP |

Pre-configured dashboard: **Finger Seal — Query Performance**
(latency P50/P95/P99, throughput, error rates)

Docs: `docs/superpowers/observability-setup.md`

## Prerequisites

- **Java 21+** (JDK) — el backend usa Gradle con toolchain automática
- **Node.js 20+** — para el frontend Vite + Vue
- **Docker** — para levantar la base de datos PostgreSQL de desarrollo
- **Bun** (opcional) — el proyecto tiene `bun.lock`, funciona con `bun` o `npm`

## Development

```bash
# 1. Install frontend dependencies
npm install
# o con bun: bun install

# 2. Start PostgreSQL de desarrollo (puerto 5432)
docker compose -f docker-compose.dev.yml up -d

# 3. (Opcional) Clave de encriptación personalizada
#    Por defecto usa la clave incluida en application.yml.
#    Solo si quieres una distinta:
# export APP_ENCRYPTION_KEY="$(openssl rand -base64 32)"

# 4. Iniciar backend (Spring Boot en :8080)
cd backend && ./gradlew bootRun

# 5. En otra terminal — dev server del frontend (Vite en :3000)
npm run dev
# Abre http://localhost:3000 en el navegador

# Type check
npm run lint

# Compilar frontend para producción
npm run build

# (Opcional) Desktop app con Tauri
npm run tauri dev       # desarrollo
npm run tauri build     # binario nativo

# (Opcional) E2E smoke tests (requiere dev server en :3000)
npm run test:e2e
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

No `.env` file es necesario para desarrollo básico. Si usas features de AI (Gemini), copia el archivo de ejemplo y agrega tu `GEMINI_API_KEY`:

```bash
cp .env.example .env
# editar .env con tu GEMINI_API_KEY
```

## Docs

| Document | Description |
|----------|-------------|
| `docs/superpowers/plans/2026-06-23-max-features-plan.md` | Full implementation plan (Vault, OTel, Testing, SSE, Export) |
| `docs/superpowers/vault-setup.md` | HashiCorp Vault setup guide |
| `docs/superpowers/observability-setup.md` | Grafana + Prometheus + Loki + Tempo setup |
| `ANALISIS-BLUEPRINT.md` | Gap analysis vs architecture blueprint |

## Testing

```bash
# Backend tests (22 integration + unit tests)
cd backend && ./gradlew test

# E2E smoke tests (requires dev server running on :3000)
npm run test:e2e
```

## Architecture Notes

- **Path alias**: `@/` → `src/`
- **State**: Pinia via `useAppStore()` from `@/store/app`
- **Routing**: Manual `switch` on `activeView` — no Vue Router
- **Icons**: Google Material Symbols Outlined
- **Fonts**: Plus Jakarta Sans (UI), JetBrains Mono (code)
- **Tauri**: Solo shell nativo — la UI es web app Vue 3
