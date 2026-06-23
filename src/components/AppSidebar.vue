<template>
  <aside class="h-full w-64 flex flex-col bg-surface-container-low border-r border-outline-variant py-md px-sm z-30 shrink-0">
    <div class="mb-lg px-xs">
      <div class="flex items-center gap-sm mb-md">
        <div class="w-8 h-8 rounded-sm bg-primary-container flex items-center justify-center">
          <span class="material-symbols-outlined text-on-primary-container" style="font-variation-settings: 'FILL' 1;">database</span>
        </div>
        <div v-if="selectedConnectionId && connections.length > 0">
          <h2 class="font-headline-md text-[14px] text-on-surface leading-tight">{{ currentConn?.name || 'Database' }}</h2>
          <p class="font-body-sm text-[11px] text-on-surface-variant">{{ currentConn?.dbType || '' }}</p>
        </div>
        <div v-else>
          <h2 class="font-headline-md text-[14px] text-on-surface leading-tight">No connection</h2>
          <p class="font-body-sm text-[11px] text-on-surface-variant">Add one to get started</p>
        </div>
      </div>
      <button @click="activeView = 'connections'" class="w-full bg-primary text-on-primary font-body-md text-body-md py-sm rounded-sm hover:opacity-90 transition-all flex items-center justify-center gap-xs">
         <span class="material-symbols-outlined text-[18px]">add</span>
         New Connection
      </button>
      <!-- Connection selector (only show when >1 connection) -->
      <select v-if="connections.length > 1" v-model="selectedConnectionId" class="w-full bg-surface border border-outline-variant rounded px-2 py-1 text-body-sm font-code-sm text-on-surface outline-none mb-md">
        <option :value="null" disabled>Select connection...</option>
        <option v-for="conn in connections" :key="conn.id" :value="conn.id">{{ conn.name }}</option>
      </select>
    </div>

    <!-- Scrollable Navigation -->
    <nav class="flex-1 flex flex-col gap-xs overflow-y-auto custom-scrollbar">
      <p class="text-label-caps text-outline uppercase mb-2 px-xs pt-2">Browser</p>
      
      <a href="#" @click.prevent="activeView = 'connections'"
        class="flex items-center gap-sm px-sm py-xs font-body-md text-body-md rounded-sm transition-all duration-150"
        :class="activeView === 'connections' ? 'bg-secondary-container text-on-secondary-container' : 'text-on-surface-variant hover:bg-surface-variant hover:text-on-surface'">
        <span class="material-symbols-outlined">hub</span>
        Connections
      </a>
      
      <a href="#" @click.prevent="activeView = 'tables'"
        class="flex items-center gap-sm px-sm py-xs font-body-md text-body-md rounded-sm transition-all duration-150"
        :class="activeView === 'tables' ? 'bg-secondary-container text-on-secondary-container' : 'text-on-surface-variant hover:bg-surface-variant hover:text-on-surface'">
         <span class="material-symbols-outlined" :style="{ 'font-variation-settings': activeView === 'tables' ? '\'FILL\' 1' : '' }">table_chart</span>
         Tables
      </a>
      
      <!-- Sub-list of tables -->
      <div v-if="selectedConnectionId" class="ml-8 flex flex-col gap-1 border-l border-outline-variant/30 pl-xs mt-1 mb-2">
        <div v-for="t in tables" :key="t.table_name" @click="goToTable(t.table_name)" class="p-xs text-body-sm rounded hover:bg-surface-variant cursor-pointer font-code-md" :class="[t.table_type === 'VIEW' ? 'text-outline' : 'text-on-surface-variant', t.table_name === selectedTable ? 'bg-secondary-container text-on-secondary-container font-bold' : '']">
          {{ t.table_name }}
        </div>
        <div v-if="tables.length === 0" class="p-xs text-body-sm text-outline italic">No tables found</div>
      </div>

      <a href="#" @click.prevent="activeView = 'dashboard'"
        class="flex items-center gap-sm px-sm py-xs font-body-md text-body-md rounded-sm transition-all duration-150"
        :class="activeView === 'dashboard' ? 'bg-secondary-container text-on-secondary-container' : 'text-on-surface-variant hover:bg-surface-variant hover:text-on-surface'">
         <span class="material-symbols-outlined">analytics</span>
         Dashboard
      </a>
      
      <a href="#" @click.prevent="activeView = 'queries'"
        class="flex items-center gap-sm px-sm py-xs font-body-md text-body-md rounded-sm transition-all duration-150"
        :class="activeView === 'queries' ? 'bg-secondary-container text-on-secondary-container' : 'text-on-surface-variant hover:bg-surface-variant hover:text-on-surface'">
         <span class="material-symbols-outlined">terminal</span>
         Queries
      </a>
    </nav>
    
    <div class="mt-auto flex flex-col gap-xs border-t border-outline-variant pt-md">
      <a class="flex items-center gap-sm px-sm py-xs text-on-surface-variant font-body-md text-body-md hover:bg-surface-variant hover:text-on-surface transition-all" href="#">
         <span class="material-symbols-outlined">settings</span>
         Settings
      </a>
      <a class="flex items-center gap-sm px-sm py-xs text-on-surface-variant font-body-md text-body-md hover:bg-surface-variant hover:text-on-surface transition-all" href="#">
         <span class="material-symbols-outlined">help</span>
         Support
      </a>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { activeView, selectedConnectionId, selectedTable } from '@/store/app'
import { connectionsApi, type ConnectionConfig } from '@/api/connections'
import { schemasApi, type TableInfo } from '@/api/schemas'

const connections = ref<ConnectionConfig[]>([])
const tables = ref<TableInfo[]>([])

const currentConn = computed(() =>
  connections.value.find(c => c.id === selectedConnectionId.value)
)

onMounted(() => {
  connectionsApi.list().then(data => {
    connections.value = data
    // Auto-select first connection if none selected
    if (data.length > 0 && !selectedConnectionId.value) {
      selectedConnectionId.value = data[0].id!
    }
  }).catch(() => {})
})

watch(selectedConnectionId, (id) => {
  tables.value = []
  selectedTable.value = ''
  if (!id) return
  schemasApi.listTables(id).then(data => tables.value = data).catch(() => {})
})

function goToTable(tableName: string) {
  selectedTable.value = tableName
  activeView.value = 'tables'
}
</script>
