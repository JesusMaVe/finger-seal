# AGENTS.md — DataForge Pro

## What this is

Vue 3 + Vite + Tailwind CSS v4 database management UI (SQL editor, connection manager, table explorer, dashboard). Generated from Google AI Studio. Entry: `src/main.ts`.

## Commands

- `npm run dev` — Vite dev server on port 3000
- `npm run build` — Production build to `dist/`
- `npm run lint` — Runs `tsc --noEmit` only (no ESLint)
- `npm run clean` — Removes `dist/` and `server.js`

## Gotchas

- **`package.json` says "react-example" and lists React deps, but this is a Vue 3 app.** The React dependencies are unused leftovers. Do not import React.
- **No `.env.local` exists.** Create it from `.env.example` with a real `GEMINI_API_KEY` if you need the AI features to work.
- **No test runner.** There are no tests, no test config, and no test script.
- **No ESLint/Prettier.** The only type checking is `tsc --noEmit`.
- **Tailwind CSS v4** uses `@theme` blocks in `src/index.css` for custom tokens (colors, spacing, fonts). This is not the v3 config format.

## Conventions

- **Path alias:** `@/` maps to `src/` (configured in both `vite.config.ts` and `tsconfig.json`).
- **Icons:** Google Material Symbols Outlined font. Use `<span class="material-symbols-outlined">icon_name</span>`. Fill via `style="font-variation-settings: 'FILL' 1;"`.
- **Fonts:** Inter (body/headlines), JetBrains Mono (code). Loaded via Google Fonts in `index.html`.
- **State:** Simple `ref()` exports from `src/store/app.ts`. No Pinia/Vuex.
- **View routing:** Manual `switch` on `activeView` ref in `App.vue`. No Vue Router.
- **Styling:** Tailwind utility classes with custom theme tokens (`text-primary`, `bg-surface-container-low`, etc.). Token names follow Material Design naming.

## Structure

```
src/
  main.ts              # Vue app entry
  App.vue              # Root — view switcher
  index.css            # Tailwind v4 @theme tokens + global styles
  store/app.ts         # Minimal state (activeView, sidebar)
  components/          # AppHeader, AppSidebar
  views/               # DashboardView, ConnectionManagerView, TableExplorerView, SqlEditorView
```
