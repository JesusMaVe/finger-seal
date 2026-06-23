<template>
  <div class="flex-1 flex flex-col min-w-0 bg-surface">
    <!-- Editor Tabs -->
    <div class="flex items-center bg-surface-container-low border-b border-outline-variant shrink-0">
      <div class="flex-1 flex overflow-x-auto custom-scrollbar">
        <button class="px-md py-sm font-body-md text-body-md text-primary border-b-2 border-primary bg-surface-container-lowest flex items-center gap-sm shrink-0">
          <span>fetch_analytics.sql</span>
          <span class="material-symbols-outlined text-[14px]">close</span>
        </button>
        <button class="px-md py-sm font-body-md text-body-md text-on-surface-variant hover:bg-surface-container-highest transition-colors flex items-center gap-sm shrink-0">
          <span>user_report_v2.sql</span>
          <span class="material-symbols-outlined text-[14px]">close</span>
        </button>
        <button class="px-md py-sm text-on-surface-variant hover:bg-surface-container-highest shrink-0">
          <span class="material-symbols-outlined text-[18px]">add</span>
        </button>
      </div>
      <div class="px-md flex gap-sm shrink-0 border-l border-outline-variant py-1">
        <button class="flex items-center gap-xs px-md py-xs bg-primary text-on-primary rounded-sm font-body-md text-body-md font-bold hover:opacity-90 active:opacity-80 transition-all">
          <span class="material-symbols-outlined text-[18px]">play_arrow</span>
          Run
        </button>
        <button class="flex items-center gap-xs px-md py-xs bg-secondary-container text-on-secondary-container rounded-sm font-body-md text-body-md hover:bg-surface-variant transition-all">
          <span class="material-symbols-outlined text-[18px]">save</span>
          Save
        </button>
      </div>
    </div>

    <!-- SQL Code Editor -->
    <div class="flex-1 overflow-hidden relative flex bg-surface">
      <!-- Line Numbers -->
      <div class="w-12 bg-surface-container-low border-r border-outline-variant text-right pr-sm py-md font-code-sm text-code-sm text-outline select-none shrink-0 border-l border-outline-variant/30">
        1<br/>2<br/>3<br/>4<br/>5<br/>6<br/>7<br/>8<br/>9<br/>10<br/>11<br/>12
      </div>
      <!-- Editor Body -->
      <div class="flex-1 p-md font-code-md text-code-md bg-surface overflow-auto custom-scrollbar focus:outline-none text-on-surface" contenteditable="true" spellcheck="false">
        <span class="sql-keyword">SELECT</span><br/>
        &nbsp;&nbsp;u.id, <br/>
        &nbsp;&nbsp;u.username, <br/>
        &nbsp;&nbsp;u.email, <br/>
        &nbsp;&nbsp;<span class="sql-function">COUNT</span>(o.order_id) <span class="sql-keyword">AS</span> total_orders,<br/>
        &nbsp;&nbsp;<span class="sql-function">SUM</span>(o.amount) <span class="sql-keyword">AS</span> revenue<br/>
        <span class="sql-keyword">FROM</span> users u<br/>
        <span class="sql-keyword">JOIN</span> orders o <span class="sql-keyword">ON</span> u.id = o.user_id<br/>
        <span class="sql-keyword">WHERE</span> o.created_at > <span class="sql-string">'2023-01-01'</span><br/>
        <span class="sql-keyword">GROUP BY</span> u.id, u.username, u.email<br/>
        <span class="sql-keyword">ORDER BY</span> revenue <span class="sql-keyword">DESC</span><br/>
        <span class="sql-keyword">LIMIT</span> 500;
        <br/><br/>
        <span class="sql-comment">-- Fetching top performing accounts for the quarterly audit</span>
      </div>
    </div>

    <!-- Results Panel -->
    <div class="h-64 border-t border-outline-variant flex flex-col bg-surface-container-lowest shrink-0 drop-shadow-[0_-4px_10px_rgba(0,0,0,0.02)] relative z-20">
      <!-- Toolbar -->
      <div class="flex items-center justify-between px-md py-xs border-b border-outline-variant bg-surface-container">
        <div class="flex gap-md items-center">
          <div class="flex">
            <button class="px-md py-1 font-body-sm text-body-sm font-bold text-on-surface border-b-2 border-primary bg-surface-container-lowest transition-all">Data</button>
            <button class="px-md py-1 font-body-sm text-body-sm text-on-surface-variant hover:text-on-surface transition-all">Console</button>
            <button class="px-md py-1 font-body-sm text-body-sm text-on-surface-variant hover:text-on-surface transition-all">Execution Plan</button>
          </div>
        </div>
        <div class="flex items-center gap-md">
          <span class="font-code-sm text-code-sm text-on-surface-variant">Fetched 500 rows in 12ms</span>
          <div class="h-4 w-[1px] bg-outline-variant"></div>
          <div class="flex gap-xs">
            <button class="material-symbols-outlined text-[18px] text-on-surface-variant hover:text-primary transition-all">filter_list</button>
            <button class="material-symbols-outlined text-[18px] text-on-surface-variant hover:text-primary transition-all">download</button>
            <button class="material-symbols-outlined text-[18px] text-on-surface-variant hover:text-primary transition-all">share</button>
          </div>
        </div>
      </div>

      <!-- Data Table -->
      <div class="flex-1 overflow-auto custom-scrollbar bg-surface-container-lowest relative">
        <table class="w-full text-left font-body-sm text-body-sm border-collapse min-w-[600px]">
          <thead class="bg-surface-container-high sticky top-0 z-10 shadow-sm">
            <tr>
              <th class="px-sm py-2 border-r border-b border-outline-variant font-bold text-on-surface">#</th>
              <th class="px-sm py-2 border-r border-b border-outline-variant font-bold text-on-surface">id</th>
              <th class="px-sm py-2 border-r border-b border-outline-variant font-bold text-on-surface">username</th>
              <th class="px-sm py-2 border-r border-b border-outline-variant font-bold text-on-surface">email</th>
              <th class="px-sm py-2 border-r border-b border-outline-variant font-bold text-on-surface">total_orders</th>
              <th class="px-sm py-2 border-b border-outline-variant font-bold text-on-surface">revenue</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in mockResults" :key="user.id" class="hover:bg-surface-variant transition-colors group cursor-pointer">
              <td class="px-sm py-1 border-r border-b border-outline-variant text-outline group-hover:text-on-surface-variant">{{ user.index }}</td>
              <td class="px-sm py-1 border-r border-b border-outline-variant font-code-sm text-on-surface">{{ user.id }}</td>
              <td class="px-sm py-1 border-r border-b border-outline-variant">{{ user.username }}</td>
              <td class="px-sm py-1 border-r border-b border-outline-variant">{{ user.email }}</td>
              <td class="px-sm py-1 border-r border-b border-outline-variant">{{ user.orders }}</td>
              <td class="px-sm py-1 border-b border-outline-variant">{{ user.revenue }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
    <!-- Footer local to view -->
    <footer class="h-8 bg-surface-container border-t border-outline-variant flex justify-between items-center px-md py-xs z-50 shrink-0">
      <div class="flex items-center gap-md">
        <span class="font-label-caps text-label-caps text-tertiary">Environment: PRODUCTION</span>
        <div class="h-3 w-[1px] bg-outline-variant"></div>
        <span class="font-code-sm text-code-sm text-on-surface-variant">Executed in 12ms | 500 rows</span>
      </div>
      <div class="flex gap-md">
        <a class="font-code-sm text-code-sm text-on-surface-variant hover:text-primary transition-colors" href="#">Documentation</a>
        <a class="font-code-sm text-code-sm text-on-surface-variant hover:text-primary transition-colors" href="#">Status</a>
        <a class="font-code-sm text-code-sm text-on-surface-variant hover:text-primary transition-colors" href="#">Release Notes</a>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const mockResults = ref([
  { index: 1, id: 'USR_8829', username: 'alpha_coder', email: 'alpha@proton.me', orders: 142, revenue: '$12,450.00' },
  { index: 2, id: 'USR_4410', username: 'dev_ops_ninja', email: 'ninja@gmail.com', orders: 98, revenue: '$8,210.50' },
  { index: 3, id: 'USR_1022', username: 'skyline_blue', email: 'skyline@outlook.com', orders: 76, revenue: '$6,440.00' },
  { index: 4, id: 'USR_9901', username: 'pixel_pusha', email: 'ppusha@fastmail.com', orders: 54, revenue: '$5,900.22' },
  { index: 5, id: 'USR_2311', username: 'data_wizard', email: 'wizard@icloud.com', orders: 45, revenue: '$4,332.10' },
])
</script>
