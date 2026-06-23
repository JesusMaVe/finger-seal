<template>
  <div class="flex-1 flex flex-col min-w-0 bg-surface">
    <!-- Editor Tabs -->
    <div class="flex items-center bg-surface-container-low border-b border-outline-variant shrink-0">
      <div class="flex items-center gap-sm px-md py-1 shrink-0 border-r border-outline-variant">
        <span v-if="selectedConnectionId" class="text-body-sm font-code-sm text-on-surface">{{ currentConn?.name }} <span class="text-outline">({{ currentConn?.dbType }})</span></span>
        <span v-else class="text-body-sm text-outline italic">No connection selected</span>
      </div>
      <div class="flex-1 flex overflow-x-auto custom-scrollbar">
        <button class="px-md py-sm font-body-md text-body-md text-primary border-b-2 border-primary bg-surface-container-lowest flex items-center gap-sm shrink-0">
          <span>fetch_analytics.sql</span>
          <span class="material-symbols-outlined text-[14px]">close</span>
        </button>
        <button class="px-md py-sm font-body-md text-body-md text-on-surface-variant hover:bg-surface-container-highest transition-colors flex items-center gap-sm shrink-0">
          <span>user_report_v2.sql</span>
          <span class="material-symbols-outlined text-[14px]">close</span>
        </button>
        <button class="px-md py-sm text-on-surface-variant hover:bg-surface-container-highest shrink-0">
          <span class="material-symbols-outlined text-[18px]">add</span>
        </button>
      </div>
      <div class="px-md flex gap-sm shrink-0 border-l border-outline-variant py-1">
        <button @click="runQuery" :disabled="running" class="flex items-center gap-xs px-md py-xs bg-primary text-on-primary rounded-sm font-body-md text-body-md font-bold hover:opacity-90 active:opacity-80 transition-all">
          <span class="material-symbols-outlined text-[18px]">play_arrow</span>
          {{ running ? 'Running...' : 'Run' }}
        </button>
        <button class="flex items-center gap-xs px-md py-xs bg-secondary-container text-on-secondary-container rounded-sm font-body-md text-body-md hover:bg-surface-variant transition-all">
          <span class="material-symbols-outlined text-[18px]">save</span>
          Save
        </button>
        <button @click="showHistory = !showHistory" class="flex items-center gap-xs px-md py-xs text-on-surface-variant hover:text-on-surface transition-all rounded-sm">
          <span class="material-symbols-outlined text-[18px]">history</span>
          <span class="text-body-sm font-body-sm">History</span>
        </button>
      </div>
    </div>

    <!-- SQL Code Editor -->
    <div class="flex-1 overflow-hidden relative flex bg-surface">
      <!-- Editor Body -->
      <textarea v-model="sql" class="flex-1 p-md font-code-md text-code-md bg-surface overflow-auto custom-scrollbar focus:outline-none text-on-surface resize-none border-0" spellcheck="false" placeholder="Enter SQL query..."></textarea>
    </div>

    <!-- Results Panel -->
    <div class="h-64 border-t border-outline-variant flex flex-col bg-surface-container-lowest shrink-0 drop-shadow-[0_-4px_10px_rgba(0,0,0,0.02)] relative z-20">
      <!-- Toolbar -->
      <div class="flex items-center justify-between px-md py-xs border-b border-outline-variant bg-surface-container">
        <div class="flex gap-md items-center">
          <div class="flex">
            <button class="px-md py-1 font-body-sm text-body-sm font-bold text-on-surface border-b-2 border-primary bg-surface-container-lowest transition-all">Data</button>
            <button class="px-md py-1 font-body-sm text-body-sm text-on-surface-variant hover:text-on-surface transition-all">Console</button>
            <button class="px-md py-1 font-body-sm text-body-sm text-on-surface-variant hover:text-on-surface transition-all">Execution Plan</button>
          </div>
        </div>
        <div class="flex items-center gap-md">
          <span class="font-code-sm text-code-sm text-on-surface-variant" v-if="results">
          {{ results.rows ? results.rows.length + ' rows' : results.affectedRows + ' rows affected' }} in {{ results.elapsedMs }}ms
        </span>
        <span class="font-code-sm text-code-sm text-on-surface-variant" v-else>Ready</span>
          <div class="h-4 w-[1px] bg-outline-variant"></div>
          <div class="flex gap-xs">
            <button class="material-symbols-outlined text-[18px] text-on-surface-variant hover:text-primary transition-all">filter_list</button>
            <button class="material-symbols-outlined text-[18px] text-on-surface-variant hover:text-primary transition-all">download</button>
            <button class="material-symbols-outlined text-[18px] text-on-surface-variant hover:text-primary transition-all">share</button>
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
        <div v-for="entry in history" :key="entry.id" @click="loadHistorySql(entry.sql)" class="px-md py-1 hover:bg-surface-variant transition-colors cursor-pointer flex items-center justify-between">
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
        <div v-if="history.length === 0" class="px-md py-2 text-center text-outline text-body-sm italic">No queries executed yet</div>
      </div>
    </div>

    <!-- Footer -->
    <footer class="h-8 bg-surface-container border-t border-outline-variant flex justify-between items-center px-md py-xs z-50 shrink-0">
      <div class="flex items-center gap-md">
        <span class="font-code-sm text-code-sm text-on-surface-variant">Finger Seal v0.1</span>
        <div class="h-3 w-[1px] bg-outline-variant"></div>
        <span class="font-code-sm text-code-sm text-on-surface-variant" v-if="results">{{ results.rows ? results.rows.length + ' rows' : results.affectedRows + ' rows affected' }} in {{ results.elapsedMs }}ms</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { queryApi, type QueryResult, type QueryHistoryEntry } from '@/api/query'
import { connectionsApi, type ConnectionConfig } from '@/api/connections'
import { selectedConnectionId } from '@/store/app'

const connections = ref<ConnectionConfig[]>([])
const currentConn = computed(() => connections.value.find(c => c.id === selectedConnectionId.value))
const sql = ref(`SELECT
  u.id,
  u.username,
  u.email
FROM users u
LIMIT 100;`)
const results = ref<QueryResult | null>(null)
const running = ref(false)
const showHistory = ref(false)
const history = ref<QueryHistoryEntry[]>([])

onMounted(() => {
  connectionsApi.list().then(data => connections.value = data).catch(() => {})
})

watch(selectedConnectionId, async (id) => {
  if (!id) return
  try {
    history.value = await queryApi.history(id)
  } catch { history.value = [] }
}, { immediate: true })

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

async function clearHistory() {
  if (!selectedConnectionId.value) return
  await queryApi.clearHistory(selectedConnectionId.value)
  history.value = []
}

function loadHistorySql(entrySql: string) {
  sql.value = entrySql
}
</script>
