import { apiFetch } from './client';

export interface QueryResult {
  columns?: string[];
  rows?: Record<string, unknown>[];
  affectedRows?: number;
  elapsedMs: number;
  error?: string;
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
};
