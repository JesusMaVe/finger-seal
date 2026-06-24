import { apiFetch } from './client';

export interface LintIssue {
  line: number;
  column: number;
  message: string;
  severity: 'error' | 'warning' | 'info';
}

export interface LintResult {
  issues: LintIssue[];
  elapsedMs: number;
}

export interface FormatResult {
  sql: string | null;
  error: string | null;
  elapsedMs: number;
}

export interface SchemaSuggestion {
  name: string;
  type: 'table' | 'column' | 'keyword';
  parent: string | null;
  schema: string | null;
}

export interface SuggestResult {
  suggestions: SchemaSuggestion[];
  elapsedMs: number;
}

export const editorApi = {
  lint: (sql: string, dialect?: string) =>
    apiFetch<LintResult>('/editor/lint', {
      method: 'POST',
      body: JSON.stringify({ sql, dialect }),
    }),

  format: (sql: string) =>
    apiFetch<FormatResult>('/editor/format', {
      method: 'POST',
      body: JSON.stringify({ sql }),
    }),

  suggest: (connectionId: number, partial: string) =>
    apiFetch<SuggestResult>('/editor/suggest', {
      method: 'POST',
      body: JSON.stringify({ connectionId, partial }),
    }),
};
