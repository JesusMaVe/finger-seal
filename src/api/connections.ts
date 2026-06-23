import { apiFetch } from './client';

export interface ConnectionConfig {
  id?: number;
  name: string;
  dbType: 'POSTGRESQL' | 'MYSQL' | 'SQLITE' | 'ORACLE';
  host: string;
  port: number;
  database: string;
  username: string;
  password: string;
}

export const connectionsApi = {
  list: () => apiFetch<ConnectionConfig[]>('/connections'),
  get: (id: number) => apiFetch<ConnectionConfig>(`/connections/${id}`),
  create: (cfg: ConnectionConfig) =>
    apiFetch<ConnectionConfig>('/connections', { method: 'POST', body: JSON.stringify(cfg) }),
  delete: (id: number) =>
    apiFetch<void>(`/connections/${id}`, { method: 'DELETE' }),
  test: (cfg: ConnectionConfig) =>
    apiFetch<void>('/connections/test', { method: 'POST', body: JSON.stringify(cfg) }),
  testExisting: (id: number) =>
    apiFetch<void>(`/connections/${id}/test`, { method: 'POST' }),
};
