<template>
  <div class="flex-1 flex flex-col min-w-0 bg-surface">
    <!-- Editor Tabs -->
    <div class="flex items-center bg-surface-container-low border-b border-outline-variant shrink-0">
      <div class="flex items-center gap-sm px-md py-1 shrink-0 border-r border-outline-variant">
        <span v-if="selectedConnectionId" class="text-body-sm font-code-sm text-on-surface">{{ currentConn?.name }} <span class="text-outline">({{ currentConn?.dbType }})</span></span>
        <span v-else class="text-body-sm text-outline italic">No connection selected</span>
      </div>
      <div class="flex-1"></div>
      <div class="px-md flex gap-sm shrink-0 border-l border-outline-variant py-1">
        <button @click="runQuery" :disabled="running" class="flex items-center gap-xs px-md py-xs bg-primary text-on-primary rounded-sm font-body-md text-body-md font-bold btn-transition hover:opacity-90 active:opacity-80 disabled:opacity-50">
          <span class="material-symbols-outlined text-[18px]">play_arrow</span>
          {{ running ? 'Running...' : 'Run' }}
        </button>
        <button @click="saveSql" class="flex items-center gap-xs px-md py-xs bg-secondary-container text-on-secondary-container rounded-sm font-body-md text-body-md btn-transition hover:bg-surface-container-high">
          <span class="material-symbols-outlined text-[18px]">save</span>
          Save
        </button>
        <button @click="formatSql" class="flex items-center gap-xs px-md py-xs text-on-surface-variant hover:text-on-surface transition-all rounded-sm">
          <span class="material-symbols-outlined text-[18px]">code</span>
          <span class="text-body-sm font-body-sm">Format</span>
        </button>
        <button @click="showHistory = !showHistory" class="flex items-center gap-xs px-md py-xs text-on-surface-variant hover:text-on-surface transition-all rounded-sm">
          <span class="material-symbols-outlined text-[18px]">history</span>
          <span class="text-body-sm font-body-sm">History</span>
        </button>
      </div>
    </div>

    <!-- SQL Code Editor -->
    <div class="flex-1 overflow-hidden relative flex flex-col bg-surface">
      <div ref="editorContainer" class="flex-1 overflow-hidden"></div>
    </div>

    <!-- Results Panel -->
    <div class="h-64 border-t border-outline-variant flex flex-col bg-surface-container-lowest shrink-0 relative z-20">
      <!-- Toolbar -->
      <div class="flex items-center justify-between px-md py-xs border-b border-outline-variant bg-surface-container">
        <div class="flex gap-md items-center">
          <span class="font-label-caps text-label-caps text-on-surface-variant uppercase">Data</span>
          <label class="flex items-center gap-xs cursor-pointer">
            <input type="checkbox" v-model="streamingEnabled" class="accent-primary" />
            <span class="text-body-sm text-on-surface-variant">Stream</span>
          </label>
        </div>
        <div class="flex items-center gap-md">
          <span class="font-code-sm text-code-sm text-on-surface-variant" v-if="results">
          {{ results.rows ? results.rows.length + ' rows' : results.affectedRows + ' rows affected' }} in {{ results.elapsedMs }}ms
        </span>
        <span class="font-code-sm text-code-sm text-on-surface-variant" v-else>Ready</span>
          <div class="h-4 w-[1px] bg-outline-variant"></div>
          <div class="flex gap-xs relative">
            <button @click="showExportMenu = !showExportMenu" class="material-symbols-outlined text-[18px] text-on-surface-variant hover:text-primary transition-all" title="Export">download</button>
            <div v-if="showExportMenu" class="absolute bottom-full right-0 mb-1 bg-surface-bright border border-outline-variant rounded-lg shadow-xl z-50 py-1 min-w-[160px]">
              <button @click="doExport('csv')" class="w-full text-left px-md py-1.5 text-body-sm hover:bg-surface-container-low flex items-center gap-xs">
                <span class="material-symbols-outlined text-[16px]">table</span> CSV
              </button>
              <button @click="doExport('json')" class="w-full text-left px-md py-1.5 text-body-sm hover:bg-surface-container-low flex items-center gap-xs">
                <span class="material-symbols-outlined text-[16px]">data_object</span> JSON
              </button>
              <button @click="doExport('xlsx')" class="w-full text-left px-md py-1.5 text-body-sm hover:bg-surface-container-low flex items-center gap-xs">
                <span class="material-symbols-outlined text-[16px]">grid_on</span> Excel
              </button>
              <button @click="doExport('sql')" class="w-full text-left px-md py-1.5 text-body-sm hover:bg-surface-container-low flex items-center gap-xs">
                <span class="material-symbols-outlined text-[16px]">code</span> SQL INSERT
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Data Table -->
      <div class="flex-1 overflow-auto custom-scrollbar bg-surface-container-lowest relative">
        <template v-if="results && results.columns && results.rows">
          <table class="w-full text-left font-body-sm text-body-sm border-collapse min-w-[600px]">
            <thead class="bg-surface-container-high sticky top-0 z-10 shadow-sm">
              <tr>
                <th class="px-sm py-2 border-r border-b border-outline-variant font-bold text-on-surface">#</th>
                <th v-for="col in results.columns" :key="col" class="px-sm py-2 border-r border-b border-outline-variant font-bold text-on-surface">{{ col }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, idx) in results.rows" :key="idx" class="hover:bg-surface-variant transition-colors group cursor-pointer">
                <td class="px-sm py-1 border-r border-b border-outline-variant text-outline group-hover:text-on-surface-variant font-code-sm">{{ idx + 1 }}</td>
                <td v-for="col in results.columns" :key="col" class="px-sm py-1 border-r border-b border-outline-variant font-code-sm text-on-surface">{{ row[col] ?? '' }}</td>
              </tr>
            </tbody>
          </table>
        </template>
        <template v-else-if="results && results.error">
          <div class="p-md text-error font-body-sm">{{ results.error }}</div>
        </template>
        <template v-else-if="results && !results.columns?.length && !results.error">
          <div class="flex items-center justify-center h-full gap-sm text-primary font-body-sm">
            <span class="material-symbols-outlined text-[18px]">check_circle</span>
            Statement executed successfully in {{ results.elapsedMs }}ms
          </div>
        </template>
        <template v-else>
          <div class="flex items-center justify-center h-full text-on-surface-variant font-body-sm italic">Run a query to see results</div>
        </template>
      </div>
    </div>

    <!-- History Panel -->
    <div v-if="showHistory" class="border-t border-outline-variant bg-surface-container-low shrink-0 max-h-48 overflow-y-auto custom-scrollbar">
      <div class="px-md py-1 border-b border-outline-variant flex justify-between items-center sticky top-0 bg-surface-container-low z-10">
        <span class="font-label-caps text-label-caps text-on-surface-variant uppercase text-[11px]">Query History</span>
        <button @click="clearHistory" class="text-code-sm text-primary hover:underline font-medium">Clear</button>
      </div>
      <div class="divide-y divide-outline-variant/20">
        <div v-for="entry in queryHistory" :key="entry.id" @click="loadHistorySql(entry.sql)" class="px-md py-1 hover:bg-surface-variant transition-colors cursor-pointer flex items-center justify-between">
          <div class="flex-1 min-w-0 mr-sm">
            <code class="font-code-sm text-code-sm text-on-surface truncate block">{{ entry.sql }}</code>
            <div class="flex gap-sm mt-0.5">
              <span class="text-code-xs font-medium" :class="entry.status === 'SUCCESS' ? 'text-primary' : 'text-error'">{{ entry.status }}</span>
              <span class="text-code-xs text-outline-variant">{{ entry.elapsedMs }}ms</span>
              <span v-if="entry.rowsCount != null" class="text-code-xs text-outline-variant">{{ entry.rowsCount }} rows</span>
              <span class="text-code-xs text-outline-variant">{{ new Date(entry.createdAt).toLocaleTimeString() }}</span>
            </div>
          </div>
          <span v-if="entry.errorMsg" class="text-code-xs text-error truncate max-w-[150px]" :title="entry.errorMsg">{{ entry.errorMsg }}</span>
        </div>
        <div v-if="queryHistory.length === 0" class="px-md py-2 text-center text-outline text-body-sm italic">No queries executed yet</div>
      </div>
    </div>

    <!-- Footer -->
    <footer class="h-8 bg-surface-container border-t border-subtle flex justify-between items-center px-md py-xs z-50 shrink-0">
      <div class="flex items-center gap-md">
        <span class="font-code-sm text-code-sm text-on-surface-variant">Finger Seal v0.1</span>
        <div class="h-3 w-[1px] bg-outline-variant"></div>
        <span class="font-code-sm text-code-sm text-on-surface-variant" v-if="results">{{ results.rows ? results.rows.length + ' rows' : results.affectedRows + ' rows affected' }} in {{ results.elapsedMs }}ms</span>
      </div>
    </footer>
  </div>

  <!-- Toast -->
  <div v-if="toastMsg" class="fixed bottom-6 right-6 bg-on-surface text-surface px-4 py-2.5 rounded-lg shadow-xl text-body-sm font-medium flex items-center gap-2 z-50 animate-toast">
    <span class="material-symbols-outlined text-[18px]">check_circle</span>
    {{ toastMsg }}
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount, shallowRef, nextTick } from 'vue'
import { EditorView, keymap, lineNumbers, highlightActiveLine, highlightSpecialChars, ViewPlugin } from '@codemirror/view'
import { EditorState } from '@codemirror/state'
import { defaultKeymap, history, historyKeymap, indentWithTab } from '@codemirror/commands'
import { bracketMatching, foldGutter, indentOnInput, syntaxHighlighting, defaultHighlightStyle } from '@codemirror/language'
import { closeBrackets, closeBracketsKeymap, autocompletion, completionKeymap, type CompletionContext, type CompletionResult } from '@codemirror/autocomplete'
import { linter, type Diagnostic } from '@codemirror/lint'
import { sql, PostgreSQL, MySQL, SQLite, StandardSQL, type SQLDialect } from '@codemirror/lang-sql'
import { queryApi, type QueryResult, type QueryHistoryEntry } from '@/api/query'
import { editorApi, type LintIssue, type SchemaSuggestion } from '@/api/editor'
import { storeToRefs } from 'pinia'
import { useAppStore } from '@/store/app'
import { exportApi } from '@/api/export'
import { streamQuery, type StreamEvent } from '@/api/stream'
const appStore = useAppStore()
const { connections, selectedConnectionId, pendingQuery, currentSql } = storeToRefs(appStore)
const { loadConnections, bumpSchema } = appStore

const currentConn = computed(() => connections.value.find(c => c.id === selectedConnectionId.value))
const results = ref<QueryResult | null>(null)
const running = ref(false)
const streamingEnabled = ref(false)
const streamingActive = ref(false)
const streamingProgress = ref(0)
let abortStream: (() => void) | null = null
const showHistory = ref(false)
const showExportMenu = ref(false)
const queryHistory = ref<QueryHistoryEntry[]>([])
const toastMsg = ref('')
const lintIssues = ref<LintIssue[]>([])

const editorContainer = ref<HTMLElement>()
const editorView = shallowRef<EditorView>()

// Map connection dbType to CodeMirror SQL dialect
const dialectMap: Record<string, SQLDialect> = {
  POSTGRESQL: PostgreSQL,
  MYSQL: MySQL,
  SQLITE: SQLite,
  ORACLE: StandardSQL, // PLSQL not available in v6 lang-sql, use StandardSQL
}

const currentDialect = computed<SQLDialect>(() => {
  const dbType = currentConn.value?.dbType
  return dialectMap[dbType ?? ''] ?? StandardSQL
})

// Sync store → editor when pendingQuery changes or dialect changes
watch([pendingQuery, currentDialect], ([q]) => {
  if (q) {
    currentSql.value = q
    pendingQuery.value = ''
  }
  if (editorView.value) {
    const current = editorView.value.state.doc.toString()
    if (current !== currentSql.value) {
      editorView.value.dispatch({
        changes: { from: 0, to: current.length, insert: currentSql.value }
      })
    }
  }
})

// Create the theme that matches the design system
const fsTheme = EditorView.theme({
  '&': {
    backgroundColor: 'var(--color-surface)',
    color: 'var(--color-on-surface)',
    fontFamily: '"JetBrains Mono", monospace',
    fontSize: '0.8125rem',
    lineHeight: '1.5',
    height: '100%',
  },
  '.cm-content': {
    padding: '16px 0',
    caretColor: 'var(--color-primary)',
  },
  '.cm-cursor, .cm-dropCursor': {
    borderLeftColor: 'var(--color-primary)',
    borderLeftWidth: '2px',
  },
  '&.cm-focused .cm-cursor': {
    borderLeftColor: 'var(--color-primary)',
  },
  '.cm-activeLine': {
    backgroundColor: 'var(--color-surface-container-low)',
  },
  '.cm-activeLineGutter': {
    backgroundColor: 'var(--color-surface-container-low)',
  },
  '.cm-selectionMatch': {
    backgroundColor: 'var(--color-primary-container)',
  },
  '&.cm-focused .cm-selectionBackground, ::selection': {
    backgroundColor: 'var(--color-primary-container)',
  },
  '.cm-gutters': {
    backgroundColor: 'var(--color-surface)',
    color: 'var(--color-outline)',
    border: 'none',
    paddingRight: '4px',
  },
  '.cm-gutter .cm-gutterElement': {
    padding: '0 4px',
  },
  '.cm-lineNumbers .cm-gutterElement': {
    fontSize: '0.75rem',
  },
  '.cm-foldGutter .cm-gutterElement': {
    color: 'var(--color-outline)',
  },
  '.cm-matchingBracket': {
    backgroundColor: 'var(--color-primary-container)',
    outline: '1px solid var(--color-primary)',
  },
  '.cm-searchMatch': {
    backgroundColor: 'var(--color-primary-container)',
    outline: '1px solid var(--color-primary)',
  },
  '.cm-searchMatch.cm-searchMatch-selected': {
    backgroundColor: 'var(--color-tertiary-container)',
  },
  '.cm-tooltip': {
    backgroundColor: 'var(--color-surface-bright)',
    border: '1px solid var(--color-outline-variant)',
    borderRadius: '8px',
    overflow: 'hidden',
    fontFamily: 'var(--font-body-sm)',
    fontSize: '0.8125rem',
    color: 'var(--color-on-surface)',
  },
  '.cm-tooltip-autocomplete': {
    '& > ul': {
      fontFamily: 'var(--font-code-sm)',
      fontSize: '0.8125rem',
      lineHeight: '1.5',
      padding: '4px 0',
    },
    '& > ul > li': {
      padding: '6px 12px',
      display: 'flex',
      alignItems: 'center',
      gap: '8px',
    },
    '& > ul > li[aria-selected]': {
      backgroundColor: 'var(--color-secondary-container)',
      color: 'var(--color-on-secondary-container)',
    },
    '& > ul > li > div': {
      display: 'flex',
      alignItems: 'center',
      gap: '6px',
    },
  },
  '.cm-completionIcon': {
    display: 'none',
  },
  '.cm-completionLabel': {
    fontFamily: 'var(--font-code-sm)',
  },
  '.cm-completionDetail': {
    fontFamily: 'var(--font-body-sm)',
    fontStyle: 'normal',
    fontSize: '0.75rem',
    color: 'var(--color-outline)',
    marginLeft: 'auto',
  },
  '.cm-panels': {
    backgroundColor: 'var(--color-surface-container-low)',
    color: 'var(--color-on-surface)',
  },
  '.cm-panels.cm-panels-top': {
    borderBottom: '1px solid var(--color-outline-variant)',
  },
  '.cm-panels.cm-panels-bottom': {
    borderTop: '1px solid var(--color-outline-variant)',
  },
  // Lint tooltips
  '.cm-tooltip.cm-tooltip-lint': {
    backgroundColor: 'var(--color-surface-bright)',
    color: 'var(--color-on-surface)',
    padding: '12px 16px',
    maxWidth: '440px',
    fontFamily: 'var(--font-body-sm)',
    fontSize: '0.8125rem',
    lineHeight: '1.6',
    border: '1px solid var(--color-outline-variant)',
    borderRadius: '8px',
    overflow: 'hidden',
    boxShadow: '0 8px 30px rgba(0,0,0,0.12), 0 2px 8px rgba(0,0,0,0.06)',
  },
  '.cm-lintRange': {
    backgroundImage: 'none !important',
    outline: 'none !important',
  },
  '.cm-lintRange-error': {
    borderBottom: '2px wavy var(--color-error)',
  },
  '.cm-lintRange-warning': {
    borderBottom: '2px wavy var(--color-warning)',
  },
  '.cm-lintRange-info': {
    borderBottom: '2px dotted var(--color-info)',
  },
  '.cm-lint-marker-error': {
    content: 'none',
  },
  '.cm-lint-marker-warning': {
    content: 'none',
  },
  '.cm-lint-marker-info': {
    content: 'none',
  },
  '.cm-panel.cm-lintPanel': {
    backgroundColor: 'var(--color-surface-container)',
    borderTop: '1px solid var(--color-outline-variant)',
    color: 'var(--color-on-surface)',
    padding: '0',
    fontFamily: 'var(--font-body-sm)',
    fontSize: '0.8125rem',
  },
  '.cm-panel.cm-lintPanel .cm-lintMessage': {
    padding: '8px 12px',
    borderBottom: '1px solid var(--color-outline-variant)',
    fontFamily: 'var(--font-body-sm)',
    fontSize: '0.8125rem',
    lineHeight: '1.5',
  },
  '.cm-panel.cm-lintPanel .cm-lintMessage-error': {
    backgroundColor: 'color-mix(in srgb, var(--color-error) 10%, transparent)',
  },
  '.cm-panel.cm-lintPanel .cm-lintMessage-warning': {
    backgroundColor: 'color-mix(in srgb, var(--color-warning) 10%, transparent)',
  },
  '.cm-panel.cm-lintPanel .cm-lintMessage-info': {
    backgroundColor: 'color-mix(in srgb, var(--color-info) 10%, transparent)',
  },
  '.cm-panel.cm-lintPanel .cm-lintPoint': {
    display: 'none',
  },
}, { dark: false })

// Debounced lint timer
let lintTimer: ReturnType<typeof setTimeout> | null = null

function sqlLinter() {
  return linter(async (view) => {
    const sql = view.state.doc.toString()
    if (!sql.trim()) return []
    try {
      const result = await editorApi.lint(sql)
      lintIssues.value = result.issues
      return result.issues.map((issue) => {
        const line = Math.min(issue.line, view.state.doc.lines)
        const lineObj = view.state.doc.line(line)
        const from = lineObj.from + Math.min(issue.column - 1, lineObj.length)
        const to = lineObj.to
        const severity = issue.severity === 'error' ? 'error' as const
          : issue.severity === 'warning' ? 'warning' as const
          : 'info' as const
        return { from, to, severity, message: issue.message } as Diagnostic
      })
    } catch {
      lintIssues.value = []
      return []
    }
  }, { delay: 300 })
}

// ponytail: fix tooltip position once when created, no loop
const lintPortal = ViewPlugin.fromClass(
  class {
    observer: MutationObserver
    constructor(view: EditorView) {
      this.observer = new MutationObserver((mutations) => {
        for (const m of mutations) {
          for (const node of m.addedNodes) {
            if (node instanceof HTMLElement) {
              if (node.matches('.cm-tooltip-lint')) this.fix(node)
              node.querySelectorAll('.cm-tooltip-lint').forEach((el: HTMLElement) => this.fix(el))
            }
          }
        }
      })
      this.observer.observe(view.dom, { childList: true, subtree: true })
      // Re-fix on scroll so tooltip follows the code
      view.scrollDOM.addEventListener('scroll', () => this.refixAll(), { passive: true })
    }
    refixAll() {
      document.querySelectorAll<HTMLElement>('.cm-tooltip-lint').forEach(el => {
        el.style.position = ''  // Reset to let CM's absolute positioning take effect
        // Double rAF to wait for CM's layout after scroll
        requestAnimationFrame(() => {
          requestAnimationFrame(() => {
            const rect = el.getBoundingClientRect()
            if (!rect.width || !rect.height) return
            el.style.position = 'fixed'
            el.style.top = (rect.top + 10) + 'px'
            el.style.left = (rect.left + 34) + 'px'
            el.style.zIndex = '99999'
          })
        })
      })
    }
    fix(el: HTMLElement) {
      // Double rAF: first frame is CodeMirror's layout, second is ours
      requestAnimationFrame(() => {
        requestAnimationFrame(() => {
          if (el.style.position === 'fixed') return
          const rect = el.getBoundingClientRect()
          if (!rect.width || !rect.height) return
          el.style.position = 'fixed'
          el.style.top = (rect.top + 10) + 'px'
          el.style.left = (rect.left + 34) + 'px'
          el.style.zIndex = '99999'
        })
      })
    }
    destroy() { this.observer.disconnect() }
  }
)


function toCompletionOptions(suggestions: SchemaSuggestion[]) {
  return suggestions.map((s) => ({
    label: s.name,
    type: s.type === 'table' ? 'class' : s.type === 'column' ? 'property' : 'keyword',
    detail: s.type === 'table' ? 'TABLE' : s.type === 'column' ? `in ${s.parent}` : undefined,
    boost: s.type === 'table' ? 1 : 0,
  }))
}

// Schema-aware autocomplete with cache
const suggestCache = new Map<string, SchemaSuggestion[]>()
let suggestTimer: ReturnType<typeof setTimeout> | null = null

function schemaCompletionSource(context: CompletionContext): CompletionResult | Promise<CompletionResult | null> | null {
  const word = context.matchBefore(/\w*/)
  if (!word || (word.from === word.to && !context.explicit)) return null

  const partial = word.text
  const connId = selectedConnectionId.value
  if (!connId) return null

  const cacheKey = `${connId}:${partial}`
  if (suggestCache.has(cacheKey)) {
    return {
      from: word.from,
      options: toCompletionOptions(suggestCache.get(cacheKey)!),
      validFor: /^\w*$/,
    }
  }

  return new Promise((resolve) => {
    if (suggestTimer) clearTimeout(suggestTimer)
    suggestTimer = setTimeout(async () => {
      try {
        const { suggestions } = await editorApi.suggest(connId, partial)
        if (suggestCache.size > 200) suggestCache.clear()
        suggestCache.set(cacheKey, suggestions)
        resolve({
          from: word.from,
          options: toCompletionOptions(suggestions),
          validFor: /^\w*$/,
        })
      } catch {
        resolve(null)
      }
    }, 150)
  })
}

// Build extensions
function buildExtensions(dialect: SQLDialect) {
  return [
    lineNumbers(),
    highlightActiveLine(),
    highlightSpecialChars(),
    history(),
    foldGutter(),
    indentOnInput(),
    bracketMatching(),
    closeBrackets(),
    autocompletion({ override: [schemaCompletionSource] }),
    sql({ dialect, upperCaseKeywords: true }),
    syntaxHighlighting(defaultHighlightStyle),
    sqlLinter(),
    lintPortal,
    fsTheme,
    keymap.of([
      ...closeBracketsKeymap,
      ...defaultKeymap,
      ...historyKeymap,
      ...completionKeymap,
      indentWithTab,
      // Cmd/Ctrl+Enter to run query
      { key: 'Mod-Enter', run: () => { runQuery(); return true } },
      { key: 'Shift-Alt-f', run: () => { formatSql(); return true } },
    ]),
    EditorView.updateListener.of((update) => {
      if (update.docChanged) {
        currentSql.value = update.state.doc.toString()
      }
    }),
    EditorView.lineWrapping,
  ]
}

// Initialize editor on mount
onMounted(() => {
  if (!editorContainer.value) return
  const state = EditorState.create({
    doc: currentSql.value,
    extensions: buildExtensions(currentDialect.value),
  })
  editorView.value = new EditorView({ state, parent: editorContainer.value })
})

// Rebuild editor when dialect changes
watch(currentDialect, (dialect) => {
  if (!editorView.value) return
  const doc = editorView.value.state.doc.toString()
  const newState = EditorState.create({
    doc,
    extensions: buildExtensions(dialect),
  })
  editorView.value.setState(newState)
})

// Cleanup
onBeforeUnmount(() => {
  editorView.value?.destroy()
})

onMounted(loadConnections)

watch(selectedConnectionId, async (id) => {
  suggestCache.clear() // Clear autocomplete cache on connection change
  if (!id) return
  try {
    queryHistory.value = await queryApi.history(id)
  } catch { queryHistory.value = [] }
}, { immediate: true })

const DDL_RE = /^\s*(CREATE|ALTER|DROP|TRUNCATE|RENAME)\b/i

async function runQuery() {
  if (!selectedConnectionId.value || !currentSql.value.trim()) return
  running.value = true
  results.value = null

  if (streamingEnabled.value) {
    streamingActive.value = true
    streamingProgress.value = 0
    const startTime = Date.now()
    results.value = { columns: [], rows: [], affectedRows: 0, elapsedMs: 0 }
    abortStream = streamQuery(
      selectedConnectionId.value,
      currentSql.value,
      (event: StreamEvent) => {
        if (event.type === 'header') {
          results.value = { columns: event.columns, rows: [], affectedRows: 0, elapsedMs: 0 }
        } else if (event.type === 'row') {
          const row: Record<string, unknown> = {}
          if (results.value.columns) {
            event.values.forEach((v, i) => { row[results.value.columns![i]] = v })
          }
          results.value.rows = [...(results.value.rows ?? []), row]
          streamingProgress.value++
        } else if (event.type === 'complete') {
          streamingActive.value = false
          running.value = false
          results.value.elapsedMs = Date.now() - startTime
        } else if (event.type === 'error') {
          streamingActive.value = false
          running.value = false
          results.value.error = event.message
        }
      },
      (err) => {
        streamingActive.value = false
        running.value = false
        results.value!.error = err.message
      }
    )
    return
  }

  try {
    results.value = await queryApi.execute(selectedConnectionId.value, currentSql.value)
    if (DDL_RE.test(currentSql.value)) bumpSchema()
  } catch (e: any) {
    results.value = { error: e.message, elapsedMs: 0 }
  } finally {
    running.value = false
  }
}

async function formatSql() {
  if (!currentSql.value.trim()) return
  try {
    const result = await editorApi.format(currentSql.value)
    if (result.error) {
      toastMsg.value = result.error
      setTimeout(() => { toastMsg.value = '' }, 3000)
      return
    }
    if (result.sql && editorView.value) {
      const current = editorView.value.state.doc.toString()
      editorView.value.dispatch({
        changes: { from: 0, to: current.length, insert: result.sql }
      })
      currentSql.value = result.sql
    }
  } catch (e: any) {
    toastMsg.value = 'Format failed: ' + (e.message || 'unknown error')
    setTimeout(() => { toastMsg.value = '' }, 3000)
  }
}

async function saveSql() {
  const content = currentSql.value
  if ('showSaveFilePicker' in window) {
    const handle = await (window as any).showSaveFilePicker({
      suggestedName: 'query.sql',
      types: [{ description: 'SQL File', accept: { 'text/plain': ['.sql'] } }]
    })
    const writable = await handle.createWritable()
    await writable.write(content)
    await writable.close()
  } else {
    const blob = new Blob([content], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'query.sql'
    a.click()
    URL.revokeObjectURL(url)
  }
  toastMsg.value = 'query.sql saved'
  setTimeout(() => { toastMsg.value = '' }, 3000)
}

async function exportBackendCsv() {
  if (!selectedConnectionId.value || !currentSql.value.trim()) return
  try {
    const csv = await exportApi.csv(selectedConnectionId.value, currentSql.value)
    const blob = new Blob([csv], { type: 'text/csv' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'export_' + new Date().toISOString().slice(0, 10) + '.csv'
    a.click()
    URL.revokeObjectURL(url)
    toastMsg.value = 'CSV downloaded'
    setTimeout(() => { toastMsg.value = '' }, 3000)
  } catch (e: any) {
    toastMsg.value = 'Export failed: ' + (e.message || 'unknown error')
    setTimeout(() => { toastMsg.value = '' }, 3000)
  }
}

function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

function dateStr() {
  return new Date().toISOString().slice(0, 10)
}

async function doExport(format: 'csv' | 'json' | 'xlsx' | 'sql') {
  showExportMenu.value = false
  if (!selectedConnectionId.value || !currentSql.value.trim()) return
  try {
    if (format === 'xlsx') {
      const blob = await exportApi.xlsx(selectedConnectionId.value, currentSql.value)
      downloadBlob(blob, `export_${dateStr()}.xlsx`)
    } else if (format === 'sql') {
      const sql = await exportApi.sql(selectedConnectionId.value, currentSql.value)
      downloadBlob(new Blob([sql], { type: 'text/plain' }), `export_${dateStr()}.sql`)
    } else if (format === 'csv') {
      const csv = await exportApi.csv(selectedConnectionId.value, currentSql.value)
      downloadBlob(new Blob([csv], { type: 'text/csv' }), `export_${dateStr()}.csv`)
    } else {
      const json = await exportApi.json(selectedConnectionId.value, currentSql.value)
      downloadBlob(new Blob([json], { type: 'application/json' }), `export_${dateStr()}.json`)
    }
    toastMsg.value = `${format.toUpperCase()} downloaded`
  } catch (e: any) {
    toastMsg.value = `Export failed: ${e.message}`
  }
  setTimeout(() => { toastMsg.value = '' }, 3000)
}

async function exportBackendJson() {
  if (!selectedConnectionId.value || !currentSql.value.trim()) return
  try {
    const json = await exportApi.json(selectedConnectionId.value, currentSql.value)
    const blob = new Blob([json], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'export_' + new Date().toISOString().slice(0, 10) + '.json'
    a.click()
    URL.revokeObjectURL(url)
    toastMsg.value = 'JSON downloaded'
    setTimeout(() => { toastMsg.value = '' }, 3000)
  } catch (e: any) {
    toastMsg.value = 'Export failed: ' + (e.message || 'unknown error')
    setTimeout(() => { toastMsg.value = '' }, 3000)
  }
}

async function exportCsv() {
  if (!results.value?.columns || !results.value?.rows) return
  const cols = results.value.columns
  const rows = results.value.rows.map((r: any) => cols.map(c => {
    const v = r[c]
    if (v == null) return ''
    const s = String(v)
    return s.includes(',') || s.includes('"') || s.includes('\n') ? '"' + s.replace(/"/g, '""') + '"' : s
  }).join(','))
  const csv = [cols.join(','), ...rows].join('\n')
  if ('showSaveFilePicker' in window) {
    const handle = await (window as any).showSaveFilePicker({
      suggestedName: 'query_results_' + new Date().toISOString().slice(0, 10) + '.csv',
      types: [{ description: 'CSV File', accept: { 'text/csv': ['.csv'] } }]
    })
    const writable = await handle.createWritable()
    await writable.write(csv)
    await writable.close()
  } else {
    const blob = new Blob([csv], { type: 'text/csv' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'query_results_' + new Date().toISOString().slice(0, 10) + '.csv'
    a.click()
    URL.revokeObjectURL(url)
  }
  toastMsg.value = 'CSV downloaded'
  setTimeout(() => { toastMsg.value = '' }, 3000)
}

async function clearHistory() {
  if (!selectedConnectionId.value) return
  await queryApi.clearHistory(selectedConnectionId.value)
  queryHistory.value = []
}

function loadHistorySql(entrySql: string) {
  currentSql.value = entrySql
  if (editorView.value) {
    const current = editorView.value.state.doc.toString()
    editorView.value.dispatch({
      changes: { from: 0, to: current.length, insert: entrySql }
    })
  }
}
</script>

<style>
/* CodeMirror overrides — keep minimal, theme handles the rest */
.cm-editor {
  height: 100%;
}
.cm-editor .cm-scroller {
  overflow: auto;
  font-family: "JetBrains Mono", monospace;
}
.cm-editor .cm-scroller::-webkit-scrollbar { width: 4px; height: 4px; }
.cm-editor .cm-scroller::-webkit-scrollbar-track { background: transparent; }
.cm-editor .cm-scroller::-webkit-scrollbar-thumb { background: var(--color-outline-variant); border-radius: 2px; }
.cm-editor .cm-scroller::-webkit-scrollbar-thumb:hover { background: var(--color-outline); }

/* Lint markers */
.cm-diagnostic-error {
  border-bottom: 2px solid var(--color-error);
  cursor: pointer;
}
.cm-diagnostic-warning {
  border-bottom: 2px wavy var(--color-tertiary);
  cursor: pointer;
}
.cm-diagnostic-info {
  border-bottom: 2px dotted var(--color-outline);
  cursor: pointer;
}
.cm-lintPoint-error::after,
.cm-lintPoint-warning::after,
.cm-lintPoint-info::after {
  content: '';
  position: absolute;
  left: -2px;
  bottom: -2px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
.cm-lintPoint-error::after { background: var(--color-error); }
.cm-lintPoint-warning::after { background: var(--color-tertiary); }
.cm-lintPoint-info::after { background: var(--color-outline); }

.cm-panel.cm-lintPanel {
  background: var(--color-surface-bright);
  border-top: 1px solid var(--color-outline-variant);
  font-family: "Plus Jakarta Sans", sans-serif;
  font-size: 0.8125rem;
}
.cm-lintPanel-error { color: var(--color-error); }
.cm-lintPanel-warning { color: var(--color-tertiary); }
.cm-lintPanel-info { color: var(--color-outline); }
</style>
