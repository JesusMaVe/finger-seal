export type StreamEvent =
  | { type: 'header'; columns: string[] }
  | { type: 'row'; values: unknown[] }
  | { type: 'complete' }
  | { type: 'error'; message: string }

export type StreamCallback = (event: StreamEvent) => void

/**
 * Subscribes to a query SSE stream.
 * Returns an abort function to cancel mid-stream.
 *
 * ponytail: native EventSource would be ideal but we need POST body.
 * Using fetch + ReadableStream with line-by-line SSE parsing.
 */
export function streamQuery(
  connectionId: number,
  sql: string,
  onEvent: StreamCallback,
  onError?: (err: Error) => void
): () => void {
  const controller = new AbortController()

  fetch('http://localhost:8080/api/query/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ connectionId, sql }),
    signal: controller.signal,
  })
    .then(async (response) => {
      if (!response.ok) throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      const reader = response.body!.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() ?? ''

        for (const line of lines) {
          if (!line.startsWith('data: ')) continue
          const json = line.slice(6).trim()
          if (!json) continue
          try {
            const event: StreamEvent = JSON.parse(json)
            onEvent(event)
          } catch {
            // skip malformed JSON chunks
          }
        }
      }
    })
    .catch((err) => {
      if (err.name === 'AbortError') return
      onError?.(err)
    })

  return () => controller.abort()
}
