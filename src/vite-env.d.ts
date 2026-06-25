/// <reference types="vite/client" />
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// Tauri v2 runtime bridge — available only inside a Tauri webview
declare global {
  interface Window {
    __TAURI_INTERNALS__?: Record<string, unknown>
  }
}
