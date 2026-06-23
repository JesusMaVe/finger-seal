import { ref } from 'vue'
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
export const selectedConnectionId = ref<number | null>(null)
export const selectedTable = ref<string>('')
export const connections = ref<ConnectionConfig[]>([])

export function loadConnections() {
  connectionsApi.list().then(data => {
    connections.value = data
    if (data.length > 0 && !selectedConnectionId.value) {
      selectedConnectionId.value = data[0].id!
    }
  }).catch(() => {})
}
