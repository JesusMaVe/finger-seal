import { ref, onMounted, onUnmounted } from 'vue'

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

export function useWebSocket(url: string) {
  const connected = ref(false)
  const lastEvent = ref<QueryEvent | null>(null)
  let ws: WebSocket | null = null

  function connect() {
    try {
      ws = new WebSocket(url)
      ws.onopen = () => { connected.value = true }
      ws.onclose = () => { connected.value = false }
      ws.onerror = () => { connected.value = false }
      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data) as QueryEvent
          lastEvent.value = data
        } catch {}
      }
    } catch {}
  }

  function disconnect() {
    ws?.close()
    ws = null
    connected.value = false
  }

  onMounted(connect)
  onUnmounted(disconnect)

  return { connected, lastEvent }
}
