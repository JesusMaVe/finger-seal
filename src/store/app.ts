import { ref } from 'vue'
import type { ConnectionConfig } from '@/api/connections'
import { connectionsApi } from '@/api/connections'

export type ViewName = 'dashboard' | 'connections' | 'tables' | 'queries'

export const activeView = ref<ViewName>('queries')
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
