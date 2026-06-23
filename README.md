# Finger seal

Database management UI — SQL editor, connection manager, table explorer, and dashboard.

## Tech Stack

| Layer | Stack |
|-------|-------|
| **Frontend** | Vue 3 + Vite + TypeScript + Tailwind CSS v4 |
| **Desktop** | [Tauri v2](https://v2.tauri.app/) (Rust native) |
| **Backend** | Java Spring Boot (API REST) |

## Project Structure

```
.
├── src/                  # Vue 3 frontend
│   ├── main.ts
│   ├── App.vue
│   ├── index.css         # Tailwind v4 @theme tokens
│   ├── store/app.ts      # Minimal state (refs)
│   ├── components/       # AppHeader, AppSidebar
│   └── views/            # Dashboard, ConnectionManager, TableExplorer, SqlEditor
├── src-tauri/            # Tauri Rust backend
│   ├── src/
│   │   ├── lib.rs
│   │   └── main.rs
│   ├── Cargo.toml
│   ├── tauri.conf.json
│   └── capabilities/
├── backend/              # Java Spring Boot (TODO)
├── index.html
├── vite.config.ts
├── tsconfig.json
├── package.json
└── .env.example
```

## Development

```bash
# Install dependencies
npm install

# Start test databases (PostgreSQL + MySQL)
docker compose up -d

# Load test data (choose one)
docker exec -i df-postgres psql -U testuser -d testdb < backend/src/main/resources/test-data.sql
docker exec -i df-mysql mysql -u testuser -ptestpass testdb < backend/src/main/resources/test-data-mysql.sql
# SQLite: sqlite3 /path/to/testdb.db < backend/src/main/resources/test-data-sqlite.sql

# Start backend
cd backend && ./gradlew bootRun

# Dev server (browser)
npm run dev

# Desktop app (Tauri window)
npm run tauri dev

# Type check
npm run lint

# Production build
npm run build

# Desktop build (native binary)
npm run tauri build
```

The Tauri dev server starts Vite on port 3000, then opens a native window pointing at it with hot reload.

## Test Databases

| DB | Port | User | Password | Database |
|----|------|------|----------|----------|
| PostgreSQL | 5432 | `testuser` | `testpass` | `testdb` |
| MySQL | 3306 | `testuser` | `testpass` | `testdb` |
| SQLite | — | — | — | file-based |

Start with: `docker compose up -d`

Connection configs for the app:
- **PostgreSQL:** host=`localhost`, port=`5432`, db=`testdb`, user=`testuser`, pass=`testpass`
- **MySQL:** host=`localhost`, port=`3306`, db=`testdb`, user=`testuser`, pass=`testpass`
- **SQLite:** db=`/path/to/your/file.db` (no host/port/user/pass needed)

## Environment

Copy `.env.example` to `.env` and add your `GEMINI_API_KEY` if AI features are needed.

## Architecture Notes

- **Path alias**: `@/` → `src/`
- **State**: Simple `ref()` exports — no Pinia/Vuex
- **Routing**: Manual `switch` on `activeView` — no Vue Router
- **Icons**: Google Material Symbols Outlined
- **Fonts**: Inter (UI), JetBrains Mono (code)
