const BASE_URL = 'http://localhost:8080/api';

type FetchOptions = RequestInit & { responseType?: 'json' | 'text' | 'blob' };

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

export async function apiFetch<T>(path: string, options?: FetchOptions): Promise<T> {
  const { responseType, ...init } = options ?? {};
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json', ...init.headers },
    ...init,
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new ApiError(res.status, text || res.statusText);
  }
  if (responseType === 'text') return res.text() as T;
  if (responseType === 'blob') return res.blob() as T;
  return res.json();
}

export const exportApi = {
  json: (connectionId: number, sql: string) =>
    apiFetch<string>('/export/json', { method: 'POST', body: JSON.stringify({ connectionId, sql }), responseType: 'text' }),

  csv: (connectionId: number, sql: string) =>
    apiFetch<string>('/export/csv', { method: 'POST', body: JSON.stringify({ connectionId, sql }), responseType: 'text' }),

  xlsx: (connectionId: number, sql: string) =>
    apiFetch<Blob>('/export/xlsx', { method: 'POST', body: JSON.stringify({ connectionId, sql }), responseType: 'blob' }),

  sql: (connectionId: number, sql: string, tableName?: string) =>
    apiFetch<string>('/export/sql', { method: 'POST', body: JSON.stringify({ connectionId, sql, tableName }), responseType: 'text' }),
};
