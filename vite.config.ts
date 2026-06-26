import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import tailwindcss from '@tailwindcss/vite';
import path from 'path';

const host = process.env.TAURI_DEV_HOST;

export default defineConfig(() => {
  return {
    plugins: [vue(), tailwindcss()],
    clearScreen: false,
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    server: {
      port: 3000,
      strictPort: true,
      host: host || false,
      hmr: host ? { protocol: 'ws', host, port: 3001 } : undefined,
      proxy: {
        '/ws': {
          target: 'http://localhost:8080',
          ws: true,
        },
      },
      watch: {
        ignored: ['**/src-tauri/**'],
        followSymlinks: false,
      },
    },
  };
});
