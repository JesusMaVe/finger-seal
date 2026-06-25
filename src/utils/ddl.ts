const DDL_RE = /^\s*(CREATE|ALTER|DROP|TRUNCATE|RENAME)\b/i

export function isDdl(sql: string): boolean {
  return DDL_RE.test(sql.trim())
}
