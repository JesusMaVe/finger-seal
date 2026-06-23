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
            <div class="font-headline-lg text-[28px] text-on-surface">12.4%</div>
            <div class="w-full bg-surface-variant h-1 rounded-full mt-2 overflow-hidden flex">
              <div class="bg-primary h-full transition-all duration-1000" style="width: 12.4%"></div>
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
            <div class="font-headline-lg text-[28px] text-on-surface">4.2 <span class="text-body-sm text-on-surface-variant">GB / 16GB</span></div>
            <div class="w-full bg-surface-variant h-1 rounded-full mt-2 overflow-hidden">
              <div class="bg-outline h-full transition-all duration-1000" style="width: 26%"></div>
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
            <div class="font-headline-lg text-[28px] text-primary">142</div>
            <div class="font-code-sm text-code-sm text-outline">+12 in last 5m</div>
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
            <div class="font-headline-lg text-[28px] text-primary">840</div>
            <div class="font-body-sm text-body-sm text-on-surface-variant italic">Peak load detected</div>
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
             <!-- Chart bars (mockup using CSS) -->
             <div v-for="(h, i) in [80, 65, 90, 40, 55, 70, 85, 30, 45, 60]" :key="i" class="flex-1 bg-primary/90 hover:bg-primary transition-colors rounded-t-sm border-t border-outline-variant relative" :style="`height: ${h}%;`">
                <div class="opacity-0 group-hover:opacity-100 absolute -top-8 left-1/2 -translate-x-1/2 bg-surface-container-highest text-on-surface text-code-sm px-2 py-1 rounded border border-outline-variant pointer-events-none transition-opacity whitespace-nowrap z-10 shadow-sm">{{h}}GB</div>
             </div>
          </div>
        </div>

        <!-- Activity Heatmap Mockup -->
        <div class="md:col-span-1 bg-surface-container-low p-md border border-outline-variant rounded-xl flex flex-col">
          <span class="font-label-caps text-label-caps text-on-surface-variant uppercase mb-md">Query Intensity</span>
          <div class="grid grid-cols-7 gap-[2px] flex-1">
             <!-- Generative Heatmap cells -->
             <div class="w-full aspect-square rounded-sm bg-primary" v-for="i in 28" :key="i" :style="`opacity: ${(Math.random() * 0.8) + 0.1}`" :title="`Load: ${Math.floor(Math.random() * 100)}%`"></div>
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
              <tr class="border-b border-outline-variant/30 hover:bg-surface-container-highest/40 transition-colors cursor-pointer group" v-for="(log, i) in logs" :key="i">
                <td class="px-md py-2 font-code-sm text-code-sm text-outline group-hover:text-on-surface-variant transition-colors">{{log.time}}</td>
                <td class="px-md py-2 font-body-sm text-on-surface">{{log.user}}</td>
                <td class="px-md py-2 font-code-sm text-code-sm text-primary/80 group-hover:text-primary transition-colors truncate max-w-[200px]">{{log.stmt}}</td>
                <td class="px-md py-2 font-code-sm text-code-sm text-right text-on-surface-variant">{{log.exec}}</td>
                <td class="px-md py-2 font-code-sm text-code-sm text-right font-bold text-on-surface">{{log.rows}}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
const logs = [
  { time: '14:22:01', user: 'admin_svc', stmt: "SELECT * FROM orders WHERE status = 'PENDING' LIMIT 100;", exec: '4ms', rows: '100' },
  { time: '14:21:55', user: 'j_doe', stmt: "UPDATE inventory SET stock = stock - 1 WHERE id = 4920;", exec: '12ms', rows: '1' },
  { time: '14:21:30', user: 'analyst_01', stmt: "EXPLAIN ANALYZE SELECT COUNT(*) FROM transactions GROUP BY region;", exec: '412ms', rows: '12' },
  { time: '14:20:12', user: 'system', stmt: "VACUUM ANALYZE public.session_store;", exec: '1.2s', rows: '0' },
  { time: '14:19:48', user: 'admin_svc', stmt: "INSERT INTO audit_log (user_id, action) VALUES (10, 'LOGIN');", exec: '2ms', rows: '1' },
]
</script>
