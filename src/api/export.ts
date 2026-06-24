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

export const exportApi = {
  json: (connectionId: number, sql: string) =>
    exportText('/export/json', { connectionId, sql }),

  csv: (connectionId: number, sql: string) =>
    exportText('/export/csv', { connectionId, sql }),
};
