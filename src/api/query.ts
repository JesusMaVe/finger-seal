import { apiFetch } from './client';

export interface QueryResult {
  columns?: string[];
  rows?: Record<string, unknown>[];
  affectedRows?: number;
  elapsedMs: number;
  error?: string;
}

export const queryApi = {
  execute: (connectionId: number, sql: string) =>
    apiFetch<QueryResult>('/query', {
      method: 'POST',
      body: JSON.stringify({ connectionId, sql }),
    }),
};
