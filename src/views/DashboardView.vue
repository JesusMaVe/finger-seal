<template>
  <div class="flex-1 flex flex-col h-full bg-surface-container-lowest overflow-x-hidden overflow-y-auto">
    <div class="p-lg flex flex-col gap-md max-w-7xl mx-auto w-full">
      <!-- Dashboard Header -->
      <div class="flex justify-between items-end mb-sm">
        <div>
          <h1 class="font-headline-lg text-headline-lg text-on-surface">Cluster Performance</h1>
          <p class="font-body-md text-body-md text-on-surface-variant mt-xs">Real-time telemetry from production-01-us-east</p>
        </div>
        <div class="flex gap-sm">
          <div class="flex items-center gap-xs bg-surface-container-low px-sm py-xs rounded border border-outline-variant shadow-sm shadow-black/5">
            <span class="w-2 h-2 rounded-full bg-primary animate-pulse"></span>
            <span class="font-code-sm text-code-sm text-primary uppercase font-bold tracking-widest">Cluster Healthy</span>
          </div>
        </div>
      </div>

      <!-- Bento Grid Status Cards -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-md">
        <!-- CPU Health -->
        <div class="md:col-span-1 bg-surface-container-low p-md border border-outline-variant rounded-lg flex flex-col justify-between h-32 hover:border-outline transition-colors cursor-default">
          <div class="flex justify-between items-start">
            <span class="font-label-caps text-label-caps text-on-surface-variant uppercase">CPU Usage</span>
            <span class="material-symbols-outlined text-primary text-[20px]">memory</span>
          </div>
          <div>
            <div class="font-headline-lg text-[28px] text-on-surface">—</div>
            <div class="w-full bg-surface-variant h-1 rounded-full mt-2 overflow-hidden flex">
            </div>
          </div>
        </div>
        <!-- RAM Health -->
        <div class="md:col-span-1 bg-surface-container-low p-md border border-outline-variant rounded-lg flex flex-col justify-between h-32 hover:border-outline transition-colors cursor-default">
          <div class="flex justify-between items-start">
            <span class="font-label-caps text-label-caps text-on-surface-variant uppercase">Memory usage</span>
            <span class="material-symbols-outlined text-primary text-[20px]">memory_alt</span>
          </div>
          <div>
            <div class="font-headline-lg text-[28px] text-on-surface">— <span class="text-body-sm text-on-surface-variant">/ —</span></div>
            <div class="w-full bg-surface-variant h-1 rounded-full mt-2 overflow-hidden">
            </div>
          </div>
        </div>
        <!-- Connections -->
        <div class="md:col-span-1 bg-surface-container-low p-md border border-outline-variant rounded-lg flex flex-col justify-between h-32 hover:border-outline transition-colors cursor-default">
          <div class="flex justify-between items-start">
            <span class="font-label-caps text-label-caps text-on-surface-variant uppercase">Active Conns</span>
            <span class="material-symbols-outlined text-primary text-[20px]">hub</span>
          </div>
          <div>
            <div class="font-headline-lg text-[28px] text-primary">{{ connections.length }}</div>
            <div class="font-code-sm text-code-sm text-outline">saved connections</div>
          </div>
        </div>
        <!-- Storage -->
        <div class="md:col-span-1 bg-surface-container-high p-md border border-outline-variant rounded-lg flex flex-col justify-between h-32 hover:border-outline transition-colors cursor-default relative overflow-hidden">
          <div class="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-outline-variant to-transparent opacity-50"></div>
          <div class="flex justify-between items-start">
            <span class="font-label-caps text-label-caps text-on-surface-variant uppercase">Disk IOPS</span>
            <span class="material-symbols-outlined text-primary text-[20px]">storage</span>
          </div>
          <div>
            <div class="font-headline-lg text-[28px] text-primary">—</div>
            <div class="font-body-sm text-body-sm text-on-surface-variant italic">Connect to a database</div>
          </div>
        </div>

        <!-- Large Storage Usage Chart Card -->
        <div class="md:col-span-3 bg-surface-container-low border border-outline-variant rounded-xl overflow-hidden flex flex-col">
          <div class="p-md border-b border-outline-variant flex justify-between items-center bg-surface-container/30">
            <span class="font-headline-md text-headline-md text-on-surface">Database Storage Distribution</span>
            <div class="flex gap-xs">
               <span class="px-2 py-1 rounded-sm bg-surface-variant border border-outline-variant/50 text-code-sm font-bold text-on-surface">Data</span>
               <span class="px-2 py-1 rounded-sm bg-surface text-code-sm border border-outline-variant/30 text-on-surface-variant">Index</span>
               <span class="px-2 py-1 rounded-sm bg-surface-container-highest border border-outline-variant/30 text-code-sm text-on-surface-variant">Free</span>
            </div>
          </div>
          <div class="flex-1 min-h-[220px] p-md flex items-end gap-sm relative group bg-surface">
             <!-- Chart bars: data from backend -->
             <div class="flex items-center justify-center w-full h-full text-on-surface-variant font-body-sm">
                No storage data available
             </div>
          </div>
        </div>

        <!-- Activity Heatmap Mockup -->
        <div class="md:col-span-1 bg-surface-container-low p-md border border-outline-variant rounded-xl flex flex-col">
          <span class="font-label-caps text-label-caps text-on-surface-variant uppercase mb-md">Query Intensity</span>
          <div class="grid grid-cols-7 gap-[2px] flex-1">
             <!-- Heatmap cells: data from backend -->
             <div class="flex items-center justify-center w-full h-full text-on-surface-variant font-body-sm">
                No query data available
              </div>
          </div>
          <div class="mt-md pt-xs flex justify-between items-center text-code-sm font-code-sm text-outline border-t border-outline-variant/50">
            <span>Low</span>
            <span>Critical</span>
          </div>
        </div>
      </div>

      <!-- Recent Activity List -->
      <section class="bg-surface-container-low border border-outline-variant rounded-xl overflow-hidden mt-sm mb-xl drop-shadow-sm">
        <div class="px-md py-sm border-b border-outline-variant flex justify-between items-center bg-surface-container/50">
          <h2 class="font-headline-md text-[16px] text-on-surface">Recent Query Logs</h2>
          <button class="text-primary font-body-sm font-bold text-body-sm hover:underline active:opacity-80 transition-all">View All Logs</button>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead class="bg-surface-container">
              <tr class="border-b border-outline-variant/60">
                <th class="px-md py-xs font-label-caps text-label-caps text-on-surface-variant uppercase tracking-wider">Time</th>
                <th class="px-md py-xs font-label-caps text-label-caps text-on-surface-variant uppercase tracking-wider">User</th>
                <th class="px-md py-xs font-label-caps text-label-caps text-on-surface-variant uppercase tracking-wider w-1/2">Statement</th>
                <th class="px-md py-xs font-label-caps text-label-caps text-on-surface-variant uppercase tracking-wider text-right">Execution</th>
                <th class="px-md py-xs font-label-caps text-label-caps text-on-surface-variant uppercase tracking-wider text-right">Rows</th>
              </tr>
            </thead>
            <tbody class="font-body-sm">
              <tr v-if="connected" class="border-b border-outline-variant/30 bg-surface/50">
                <td colspan="5" class="px-md py-2 text-center">
                  <span class="inline-flex items-center gap-2 text-code-sm text-primary">
                    <span class="w-1.5 h-1.5 rounded-full bg-primary animate-pulse"></span>
                    Connected — watching for queries
                  </span>
                </td>
              </tr>
              <tr class="border-b border-outline-variant/30 hover:bg-surface-container-highest/40 transition-colors cursor-pointer group" v-for="(log, i) in logs" :key="i">
                <td class="px-md py-2 font-code-sm text-code-sm text-outline group-hover:text-on-surface-variant transition-colors">{{ new Date(log.timestamp).toLocaleTimeString() }}</td>
                <td class="px-md py-2 font-body-sm text-on-surface">conn-{{ log.connectionId }}</td>
                <td class="px-md py-2 font-code-sm text-code-sm text-primary/80 group-hover:text-primary transition-colors truncate max-w-[200px]">{{ log.sql }}</td>
                <td class="px-md py-2 font-code-sm text-code-sm text-right text-on-surface-variant">{{ log.elapsedMs }}ms</td>
                <td class="px-md py-2 font-code-sm text-code-sm text-right font-bold" :class="log.status === 'ERROR' ? 'text-error' : 'text-on-surface'">{{ log.rows ?? '-' }}</td>
              </tr>
              <tr v-if="!logs.length && !connected">
                <td colspan="5" class="px-md py-4 text-center text-on-surface-variant font-body-sm italic">Not connected — start the backend</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { connections } from '@/store/app'
import { useWebSocket, type QueryEvent } from '@/composables/useWebSocket'

const logs = ref<QueryEvent[]>([])

// ponytail: dev uses Vite proxy, prod/Tauri connects to backend port directly
const wsUrl = window.location.port === '3000'
  ? `ws://${window.location.host}/ws/events`
  : `ws://localhost:8080/ws/events`
const { connected, lastEvent } = useWebSocket(wsUrl)

watch(lastEvent, (ev) => {
  if (ev && ev.type === 'query') {
    logs.value.unshift(ev)
    if (logs.value.length > 50) logs.value.pop()
  }
})
</script>
