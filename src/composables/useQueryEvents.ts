import { ref } from 'vue'

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

const wsConnected = ref(false)
const wsLogs = ref<QueryEvent[]>([])
let started = false

function start() {
  if (started) return
  started = true

  const wsUrl =
    window.location.port === '3000'
      ? `ws://${window.location.host}/ws/events`
      : `ws://localhost:8080/ws/events`

  function connect() {
    try {
      const ws = new WebSocket(wsUrl)
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

  connect()
}

export function useQueryEvents() {
  start()
  return { wsConnected, wsLogs }
}
