import { apiFetch } from './client';

export interface TableInfo {
  schema_name?: string;
  table_name: string;
  table_type: string;
}

export interface ColumnInfo {
  name: string;
  type: string;
  nullable: string;
  default: string;
  size: number;
}

export const schemasApi = {
  listTables: (connectionId: number) =>
    apiFetch<TableInfo[]>(`/connections/${connectionId}/schemas`),

  tableColumns: (connectionId: number, tableName: string) =>
    apiFetch<ColumnInfo[]>(`/connections/${connectionId}/schemas/tables/${encodeURIComponent(tableName)}/columns`),

  tableData: (connectionId: number, tableName: string, limit = 100) =>
    apiFetch<Record<string, unknown>[]>(`/connections/${connectionId}/schemas/tables/${encodeURIComponent(tableName)}/data?limit=${limit}`),

  tableStats: (connectionId: number, tableName: string) =>
    apiFetch<Record<string, unknown>>(`/connections/${connectionId}/schemas/tables/${encodeURIComponent(tableName)}/stats`),

  tableForeignKeys: (connectionId: number, tableName: string) =>
    apiFetch<Record<string, unknown>[]>(`/connections/${connectionId}/schemas/tables/${encodeURIComponent(tableName)}/foreign-keys`),
};
