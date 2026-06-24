const BASE_URL = 'http://localhost:8080/api';

async function exportText(path: string, body: object): Promise<string> {
  const res = await fetch(`${BASE_URL}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(text || res.statusText);
  }
  return res.text();
}

async function exportBlob(path: string, body: object): Promise<Blob> {
  const res = await fetch(`${BASE_URL}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error(res.statusText);
  return res.blob();
}

export const exportApi = {
  json: (connectionId: number, sql: string) =>
    exportText('/export/json', { connectionId, sql }),

  csv: (connectionId: number, sql: string) =>
    exportText('/export/csv', { connectionId, sql }),

  xlsx: (connectionId: number, sql: string) =>
    exportBlob('/export/xlsx', { connectionId, sql }),

  sql: (connectionId: number, sql: string, tableName?: string) =>
    exportText('/export/sql', { connectionId, sql, tableName }),
};
