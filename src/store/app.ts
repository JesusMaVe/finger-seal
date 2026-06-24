import { defineStore, storeToRefs } from 'pinia'
import { ref, watch } from 'vue'
import type { ConnectionConfig } from '@/api/connections'
import { connectionsApi } from '@/api/connections'

export type ViewName = 'dashboard' | 'connections' | 'tables' | 'queries'
export type Theme = 'system' | 'light' | 'dark'

export interface QueryEvent {
  type: 'query'
  connectionId: number
  sql: string
  status: string
  elapsedMs: number
  rows: number | null
  error: string | null
  timestamp: number
}

export const useAppStore = defineStore('app', () => {
  const activeView = ref<ViewName>('queries')
  const theme = ref<Theme>((localStorage.getItem('fs-theme') as Theme) || 'system')

  function applyTheme(t: Theme) {
    const html = document.documentElement
    html.removeAttribute('data-theme')
    if (t !== 'system') html.setAttribute('data-theme', t)
    localStorage.setItem('fs-theme', t)
  }

  const isSidebarCollapsed = ref(false)

  // Persist selected connection across reloads
  const savedConnId = localStorage.getItem('fs-connection-id')
  const selectedConnectionId = ref<number | null>(savedConnId ? Number(savedConnId) : null)
  watch(selectedConnectionId, (id) => {
    if (id) localStorage.setItem('fs-connection-id', String(id))
    else localStorage.removeItem('fs-connection-id')
  })

  const selectedTable = ref<string>('')
  const pendingQuery = ref('')
  const currentSql = ref('')

  // Increment after DDL to trigger sidebar table list refresh
  const schemaVersion = ref(0)
  function bumpSchema() { schemaVersion.value++ }

  const connections = ref<ConnectionConfig[]>([])

  async function loadConnections() {
    try {
      const data = await connectionsApi.list()
      connections.value = data
      if (data.length > 0 && !selectedConnectionId.value) {
        selectedConnectionId.value = data[0].id!
      }
    } catch { /* noop */ }
  }

  /* ── WebSocket event bus (persists across view switches) ── */
  const wsConnected = ref(false)
  const wsLogs = ref<QueryEvent[]>([])

  const wsUrl = window.location.port === '3000'
    ? `ws://${window.location.host}/ws/events`
    : `ws://localhost:8080/ws/events`
  let ws: WebSocket | null = null

  function connectWs() {
    try {
      ws = new WebSocket(wsUrl)
      ws.onopen = () => { wsConnected.value = true }
      ws.onclose = () => { wsConnected.value = false }
      ws.onerror = () => { wsConnected.value = false }
      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data) as QueryEvent
          if (data.type === 'query') {
            wsLogs.value.unshift(data)
            if (wsLogs.value.length > 50) wsLogs.value.pop()
          }
        } catch { /* noop */ }
      }
    } catch { /* noop */ }
  }

  connectWs()

  return {
    activeView,
    theme,
    applyTheme,
    isSidebarCollapsed,
    selectedConnectionId,
    selectedTable,
    pendingQuery,
    currentSql,
    schemaVersion,
    bumpSchema,
    connections,
    loadConnections,
    wsConnected,
    wsLogs,
  }
})
