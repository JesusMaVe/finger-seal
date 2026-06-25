import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import type { ConnectionConfig } from '@/api/connections'
import { connectionsApi } from '@/api/connections'

export type ViewName = 'dashboard' | 'connections' | 'tables' | 'queries'
export type Theme = 'system' | 'light' | 'dark'

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
  }
})
