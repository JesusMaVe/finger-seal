<template>
  <div class="flex-1 flex flex-col h-full bg-surface-container-lowest overflow-x-hidden overflow-y-auto">
    <div class="p-lg flex flex-col gap-md max-w-7xl mx-auto w-full">
      <!-- Dashboard Header -->
      <div class="flex justify-between items-end mb-sm">
        <div>
          <h1 class="font-headline-lg text-headline-lg text-on-surface">{{ currentConn ? currentConn.database + ' · ' + currentConn.dbType : 'Performance Overview' }}</h1>
          <p class="font-body-md text-body-md text-on-surface-variant mt-xs">{{ currentConn ? currentConn.host + ':' + currentConn.port + '/' + currentConn.database : 'No connection selected' }}</p>
        </div>
        <div class="flex gap-sm">
          <div class="flex items-center gap-xs bg-surface-container-low px-sm py-xs rounded border border-subtle">
            <span class="w-2 h-2 rounded-full" :class="wsConnected ? 'bg-primary animate-pulse' : 'bg-error'"></span>
            <span class="font-code-sm text-code-sm uppercase font-bold tracking-widest" :class="wsConnected ? 'text-primary' : 'text-error'">{{ wsConnected ? 'Connected' : 'Disconnected' }}</span>
          </div>
        </div>
      </div>

      <!-- Bento Grid Status Cards -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-md">
        <!-- Active Queries -->
        <div class="md:col-span-1 bg-surface-container-low p-md border border-subtle rounded-lg flex flex-col justify-between h-32 hover-lift cursor-default">
          <div class="flex justify-between items-start">
            <span class="font-label-caps text-label-caps text-on-surface-variant uppercase">Active Queries</span>
            <span class="material-symbols-outlined text-on-surface-variant text-[20px]">memory</span>
          </div>
          <div>
            <div class="font-headline-lg text-headline-lg text-on-surface">{{ metrics.activeQueries ?? '—' }}</div>
            <div class="w-full bg-surface-variant h-1 rounded-full mt-2 overflow-hidden flex">
              <div class="h-full bg-primary rounded-full transition-all duration-500" :style="{ width: Math.min((metrics.activeQueries ?? 0) / 10 * 100, 100) + '%' }"></div>
            </div>
          </div>
        </div>
        <!-- Transactions -->
        <div class="md:col-span-1 bg-surface-container-low p-md border border-subtle rounded-lg flex flex-col justify-between h-32 hover-lift cursor-default">
          <div class="flex justify-between items-start">
            <span class="font-label-caps text-label-caps text-on-surface-variant uppercase">Transactions</span>
            <span class="material-symbols-outlined text-on-surface-variant text-[20px]">sync_alt</span>
          </div>
          <div>
            <div class="font-headline-lg text-headline-lg text-on-surface">{{ metrics.transactions != null ? metrics.transactions.toLocaleString() : '—' }}</div>
            <div class="font-body-sm text-body-sm text-on-surface-variant italic" v-if="selectedConnectionId">Total since DB start</div>
            <div class="font-body-sm text-body-sm text-on-surface-variant italic" v-else>Connect to a database</div>
          </div>
        </div>
        <!-- Connections -->
        <div class="md:col-span-1 bg-surface-container-low p-md border border-subtle rounded-lg flex flex-col justify-between h-32 hover-lift cursor-default">
          <div class="flex justify-between items-start">
            <span class="font-label-caps text-label-caps text-on-surface-variant uppercase">Active Conns</span>
            <span class="material-symbols-outlined text-on-surface-variant text-[20px]">hub</span>
          </div>
          <div>
            <div class="font-headline-lg text-headline-lg text-on-surface">{{ connections.length }}</div>
            <div class="font-code-sm text-code-sm text-outline">{{ metrics.tableCount ?? connections.length }} tables</div>
          </div>
        </div>
        <!-- Storage -->
        <div class="md:col-span-1 bg-surface-container-high p-md border border-subtle rounded-lg flex flex-col justify-between h-32 hover-lift cursor-default relative overflow-hidden">
          <div class="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-outline-variant to-transparent opacity-50"></div>
          <div class="flex justify-between items-start">
            <span class="font-label-caps text-label-caps text-on-surface-variant uppercase">Storage Used</span>
            <span class="material-symbols-outlined text-on-surface-variant text-[20px]">storage</span>
          </div>
          <div>
            <div class="font-headline-lg text-headline-lg text-on-surface">{{ formatBytes(metrics.storageBytes) }}</div>
            <div class="font-body-sm text-body-sm text-on-surface-variant italic" v-if="metrics.tableCount != null">{{ metrics.tableCount }} tables</div>
            <div class="font-body-sm text-body-sm text-on-surface-variant italic" v-else>Connect to a database</div>
          </div>
        </div>

        <!-- Large Storage Usage Chart Card -->
        <div class="md:col-span-3 bg-surface-container-low border border-subtle rounded-xl overflow-hidden flex flex-col">
          <div class="p-md border-b border-subtle flex justify-between items-center bg-surface-container/30">
            <span class="font-headline-md text-headline-md text-on-surface">Storage by Table</span>
          </div>
          <div class="flex-1 min-h-[220px] p-md flex items-end gap-sm relative group bg-surface">
             <!-- Chart bars: data from backend -->
             <div v-if="metrics.storageBreakdown?.length" class="flex items-end gap-1 w-full h-full">
               <div v-for="(item, i) in metrics.storageBreakdown" :key="i"
                 class="flex-1 bg-primary/70 rounded-t-sm hover:bg-primary hover:opacity-80 transition-all cursor-pointer relative group/item min-w-[12px]"
                 :style="{ height: Math.max((item.bytes / (metrics.storageBreakdown?.[0]?.bytes * 1.1 || 1)) * 100, 4) + '%' }"
                 :title="item.name + ': ' + formatBytes(item.bytes)">
                 <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-on-surface text-surface text-[10px] px-2 py-0.5 rounded opacity-0 group-hover/item:opacity-100 transition-opacity whitespace-nowrap z-10">
                   {{ item.name }}
                 </div>
               </div>
             </div>
             <div v-else class="flex items-center justify-center w-full h-full text-on-surface-variant font-body-sm">
                No storage data available
             </div>
          </div>
        </div>

        <!-- Query Intensity Heatmap -->
        <div class="md:col-span-1 bg-surface-container-low p-md border border-subtle rounded-xl flex flex-col">
          <span class="font-label-caps text-label-caps text-on-surface-variant uppercase mb-sm">Query Intensity</span>
          <div class="flex flex-col gap-[2px] flex-1" v-if="heatmapWeeks.length">
            <!-- Column headers -->
            <div class="grid grid-cols-7 gap-[2px]">
              <div v-for="label in ['Mon','Tue','Wed','Thu','Fri','Sat','Sun']" :key="label"
                class="text-center font-code-sm text-[9px] text-outline uppercase tracking-wider py-1">{{ label }}</div>
            </div>
            <!-- 4 week rows -->
            <div v-for="(week, wi) in heatmapWeeks" :key="wi" class="grid grid-cols-7 gap-[2px]">
              <div v-for="(item, di) in week" :key="di"
                class="aspect-square rounded-sm border transition-all duration-300 hover:scale-110 hover:ring-1 hover:ring-primary/50 cursor-help"
                :class="{ 'border-outline-variant/20': !item.isFuture, 'border-transparent opacity-30': item.isFuture }"
                :style="{
                  background: item.isFuture ? 'transparent' : 'var(--color-primary)',
                  opacity: item.isFuture ? 0.3 : (item.count > 0 ? Math.max(item.count / heatmapMax, 0.15) : 0.06)
                }"
                :title="item.isFuture ? '' : (item.day + ' | ' + item.count + ' queries')">
              </div>
            </div>
          </div>
          <div class="flex items-center justify-center w-full min-h-[80px] text-on-surface-variant font-body-sm" v-else>
            No query data available
          </div>
          <div class="mt-sm pt-xs flex justify-between items-center text-code-sm font-code-sm text-outline border-t border-outline-variant/50">
            <span>Low</span>
            <span>Critical</span>
          </div>
        </div>
      </div>

      <!-- Recent Activity List -->
      <section class="bg-surface-container-low border border-subtle rounded-xl overflow-hidden mt-sm mb-xl">
        <div class="px-md py-sm border-b border-outline-variant flex justify-between items-center bg-surface-container/50">
          <h2 class="font-headline-md text-[16px] text-on-surface">Recent Query Logs</h2>
          <button @click="showAllLogs = !showAllLogs" class="text-primary font-body-sm font-bold text-body-sm hover:underline active:opacity-80 transition-all">
  {{ showAllLogs ? 'Show Less' : 'View All (' + wsLogs.length + ')' }}
</button>
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
              <template v-if="wsLogs.length">
                <tr class="border-b border-outline-variant/30 hover:bg-surface-container-highest/40 transition-colors cursor-pointer group" v-for="(log, i) in displayedLogs" :key="i">
                  <td class="px-md py-2 font-code-sm text-code-sm text-outline group-hover:text-on-surface-variant transition-colors">{{ new Date(log.timestamp).toLocaleTimeString() }}</td>
                  <td class="px-md py-2 font-body-sm text-on-surface">conn-{{ log.connectionId }}</td>
                  <td class="px-md py-2 font-code-sm text-code-sm text-primary/80 group-hover:text-primary transition-colors truncate max-w-[200px]">{{ log.sql }}</td>
                  <td class="px-md py-2 font-code-sm text-code-sm text-right text-on-surface-variant">{{ log.elapsedMs }}ms</td>
                  <td class="px-md py-2 font-code-sm text-code-sm text-right font-bold" :class="log.status === 'ERROR' ? 'text-error' : 'text-on-surface'">{{ log.rows ?? '-' }}</td>
                </tr>
              </template>
              <tr v-else-if="wsConnected" class="border-b border-outline-variant/30 bg-surface/50">
                <td colspan="5" class="px-md py-2 text-center">
                  <span class="inline-flex items-center gap-2 text-code-sm text-primary">
                    <span class="w-1.5 h-1.5 rounded-full bg-primary animate-pulse"></span>
                    Connected — watching for queries
                  </span>
                </td>
              </tr>
              <tr v-else>
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
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useAppStore } from '@/store/app'
import { useQueryEvents } from '@/composables/useQueryEvents'
const appStore = useAppStore()
const { connections, selectedConnectionId } = storeToRefs(appStore)
const { wsConnected, wsLogs } = useQueryEvents()
import { schemasApi } from '@/api/schemas'

const metrics = ref<Record<string, any>>({})
const historicalCounts = ref<Record<string, number>>({})
let pollTimer: ReturnType<typeof setInterval> | null = null

const currentConn = computed(() => connections.value.find(c => c.id === selectedConnectionId.value))

async function loadMetrics() {
  if (!selectedConnectionId.value) return
  try {
    const data = await schemasApi.dashboardMetrics(selectedConnectionId.value)
    metrics.value = data
    const hc: Record<string, number> = {}
    if (data.queryIntensity) {
      for (const item of data.queryIntensity) {
        hc[item.day] = item.count
      }
    }
    historicalCounts.value = hc
  } catch {}
}

watch(selectedConnectionId, () => {
  metrics.value = {}
  historicalCounts.value = {}
  if (selectedConnectionId.value) loadMetrics()
})

onMounted(() => {
  if (selectedConnectionId.value) loadMetrics()
  pollTimer = setInterval(loadMetrics, 10000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})

function formatBytes(bytes: number | null): string {
  if (bytes == null) return '—'
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + units[i]
}

const queryMaxCount = computed(() => {
  const items = metrics.value.queryIntensity
  if (!items?.length) return 1
  return Math.max(...items.map((i: any) => i.count), 1)
})

// Heatmap: combines persisted history (from backend) + real-time wsLogs
const heatmapWeeks = computed(() => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  // Count queries per day from wsLogs (session real-time)
  const sessionCounts: Record<string, number> = {}
  for (const log of wsLogs.value) {
    const d = new Date(log.timestamp)
    const key = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
    sessionCounts[key] = (sessionCounts[key] || 0) + 1
  }
  // Start from Monday of current week, go back 3 weeks (total 4 rows)
  const weekStart = new Date(today)
  const dow = weekStart.getDay() // 0=Sun
  weekStart.setDate(weekStart.getDate() + (dow === 0 ? -6 : 1 - dow)) // back to Monday of this week
  weekStart.setDate(weekStart.getDate() - 21) // 3 weeks back
  // Build 4 weeks of 7 days
  const weeks: { day: string; count: number; isFuture: boolean }[][] = []
  for (let w = 0; w < 4; w++) {
    const week: { day: string; count: number; isFuture: boolean }[] = []
    for (let d = 0; d < 7; d++) {
      const date = new Date(weekStart)
      date.setDate(weekStart.getDate() + w * 7 + d)
      const key = date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0') + '-' + String(date.getDate()).padStart(2, '0')
      week.push({ day: key, count: Math.max(historicalCounts.value[key] || 0, sessionCounts[key] || 0), isFuture: date > today })
    }
    weeks.push(week)
  }
  return weeks
})

const heatmapMax = computed(() => {
  let max = 0
  for (const week of heatmapWeeks.value) {
    for (const item of week) {
      if (item.count > max) max = item.count
    }
  }
  return max || 1
})

const displayedLogs = computed(() =>
  showAllLogs.value ? wsLogs.value : wsLogs.value.slice(0, 5)
)

const showAllLogs = ref(false)
</script>
