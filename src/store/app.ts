import { ref, watch } from 'vue'
import type { ConnectionConfig } from '@/api/connections'
import { connectionsApi } from '@/api/connections'

export type ViewName = 'dashboard' | 'connections' | 'tables' | 'queries'

export type Theme = 'system' | 'light' | 'dark'

export const activeView = ref<ViewName>('queries')
export const theme = ref<Theme>((localStorage.getItem('fs-theme') as Theme) || 'system')

export function applyTheme(t: Theme) {
  const html = document.documentElement
  html.removeAttribute('data-theme')
  if (t !== 'system') html.setAttribute('data-theme', t)
  localStorage.setItem('fs-theme', t)
}
export const isSidebarCollapsed = ref(false)

// Persist selected connection across reloads
const savedConnId = localStorage.getItem('fs-connection-id')
export const selectedConnectionId = ref<number | null>(savedConnId ? Number(savedConnId) : null)
watch(selectedConnectionId, (id) => {
  if (id) localStorage.setItem('fs-connection-id', String(id))
  else localStorage.removeItem('fs-connection-id')
})
export const selectedTable = ref<string>('')
export const pendingQuery = ref('')
export const currentSql = ref('')

// Increment after DDL to trigger sidebar table list refresh
export const schemaVersion = ref(0)
export function bumpSchema() { schemaVersion.value++ }
export const connections = ref<ConnectionConfig[]>([])

export function loadConnections() {
  connectionsApi.list().then(data => {
    connections.value = data
    if (data.length > 0 && !selectedConnectionId.value) {
      selectedConnectionId.value = data[0].id!
    }
  }).catch(() => {})
}

/* ── WebSocket event bus (persists across view switches) ── */
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

export const wsConnected = ref(false)
export const wsLogs = ref<QueryEvent[]>([])

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
      } catch {}
    }
  } catch {}
}

connectWs()
