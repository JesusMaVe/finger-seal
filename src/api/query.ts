import { apiFetch } from './client';

const BASE_URL = 'http://localhost:8080/api';

export interface QueryResult {
  columns?: string[];
  rows?: Record<string, unknown>[];
  affectedRows?: number;
  elapsedMs: number;
  error?: string;
}

export interface InlineEditRequest {
  connectionId: number;
  table: string;
  primaryKey: Record<string, unknown>;
  column: string;
  value: unknown;
}

export interface QueryHistoryEntry {
  id: number;
  connectionId: number;
  sql: string;
  status: 'SUCCESS' | 'ERROR';
  elapsedMs: number;
  rowsCount?: number;
  errorMsg?: string;
  createdAt: string;
}

export type StreamEvent =
  | { type: 'header'; columns: string[] }
  | { type: 'row'; values: unknown[] }
  | { type: 'complete' }
  | { type: 'error'; message: string };

export type StreamCallback = (event: StreamEvent) => void;

export function streamQuery(
  connectionId: number,
  sql: string,
  onEvent: StreamCallback,
  onError?: (err: Error) => void
): () => void {
  const controller = new AbortController();

  fetch(`${BASE_URL}/query/stream`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ connectionId, sql }),
    signal: controller.signal,
  })
    .then(async (response) => {
      if (!response.ok) throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      const reader = response.body!.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() ?? '';

        for (const line of lines) {
          if (!line.startsWith('data: ')) continue;
          const json = line.slice(6).trim();
          if (!json) continue;
          try {
            const event: StreamEvent = JSON.parse(json);
            onEvent(event);
          } catch {
            // skip malformed JSON chunks
          }
        }
      }
    })
    .catch((err) => {
      if (err.name === 'AbortError') return;
      onError?.(err);
    });

  return () => controller.abort();
}

export const queryApi = {
  execute: (connectionId: number, sql: string) =>
    apiFetch<QueryResult>('/query', {
      method: 'POST',
      body: JSON.stringify({ connectionId, sql }),
    }),

  history: (connectionId: number) =>
    apiFetch<QueryHistoryEntry[]>(`/connections/${connectionId}/history`),

  clearHistory: (connectionId: number) =>
    apiFetch<void>(`/connections/${connectionId}/history`, { method: 'DELETE' }),

  inlineEdit: (req: InlineEditRequest) =>
    apiFetch<QueryResult>('/query/inline-edit', {
      method: 'POST',
      body: JSON.stringify(req),
    }),
};
