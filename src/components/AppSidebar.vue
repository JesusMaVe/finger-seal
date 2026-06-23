<template>
  <aside class="h-full w-64 flex flex-col bg-surface-container-low border-r border-outline-variant z-30 shrink-0">

    <!-- Connection Header -->
    <div class="p-3 border-b border-outline-variant/50">
      <div class="flex items-center gap-2.5 mb-2">
        <div class="w-7 h-7 rounded bg-primary flex items-center justify-center shrink-0">
          <span class="material-symbols-outlined text-on-primary text-[16px]" style="font-variation-settings:'FILL' 1">database</span>
        </div>
        <div class="min-w-0 flex-1">
          <select v-model="selectedConnectionId"
            class="w-full bg-transparent border-0 p-0 text-sm font-semibold text-on-surface cursor-pointer outline-none appearance-none truncate">
            <option :value="null" disabled>Select connection…</option>
            <option v-for="conn in connections" :key="conn.id" :value="conn.id">{{ conn.name }}</option>
          </select>
          <p class="text-[11px] text-on-surface-variant font-medium truncate" v-if="currentConn">{{ currentConn.dbType }} · {{ currentConn.host }}:{{ currentConn.port }}</p>
          <p class="text-[11px] text-outline italic" v-else>Add a connection to start</p>
        </div>
      </div>
      <button @click="activeView = 'connections'"
        class="w-full flex items-center justify-center gap-1.5 text-xs font-medium text-on-surface-variant hover:text-on-surface bg-surface hover:bg-surface-variant border border-outline-variant/50 rounded py-1.5 transition-all">
        <span class="material-symbols-outlined text-[14px]">add</span>
        New Connection
      </button>
    </div>

    <!-- Table Browser -->
    <div class="flex-1 flex flex-col overflow-y-auto custom-scrollbar p-2">
      <p class="text-[10px] font-semibold text-outline uppercase tracking-wider px-2 py-1.5">Explorer</p>

      <a href="#" @click.prevent="activeView = 'connections'"
        class="flex items-center gap-2 px-2 py-1.5 text-sm rounded transition-all"
        :class="activeView === 'connections' ? 'bg-secondary-container text-on-secondary-container font-semibold' : 'text-on-surface-variant hover:bg-surface-variant hover:text-on-surface'">
        <span class="material-symbols-outlined text-[18px]">hub</span>
        Connections
      </a>

      <!-- Connection tables tree -->
      <div v-if="selectedConnectionId" class="mt-1">
        <a href="#" @click.prevent="activeView = 'tables'"
          class="flex items-center gap-2 px-2 py-1.5 text-sm rounded transition-all"
          :class="activeView === 'tables' ? 'bg-secondary-container text-on-secondary-container font-semibold' : 'text-on-surface-variant hover:bg-surface-variant hover:text-on-surface'">
          <span class="material-symbols-outlined text-[18px]" :style="{ 'font-variation-settings': activeView === 'tables' ? '\'FILL\' 1' : '' }">table_chart</span>
          Tables
          <span class="ml-auto text-[10px] text-outline font-code-sm">{{ tables.length }}</span>
        </a>

        <!-- Table list with indent -->
        <div v-if="activeView === 'tables' || tables.length > 0" class="ml-5 border-l border-outline-variant/30 pl-2 mt-0.5 space-y-0.5">
          <button v-for="t in tables" :key="t.table_name" @click="goToTable(t.table_name)"
            class="w-full text-left px-2 py-1 text-xs rounded transition-all truncate flex items-center gap-1.5"
            :class="[
              t.table_name === selectedTable
                ? 'bg-primary-container/30 text-primary font-semibold'
                : 'text-on-surface-variant hover:bg-surface-variant hover:text-on-surface',
              t.table_type === 'VIEW' ? 'opacity-60' : ''
            ]">
            <span class="material-symbols-outlined text-[14px] shrink-0" :style="{ 'font-variation-settings': t.table_name === selectedTable ? '\'FILL\' 1' : '' }">
              {{ t.table_type === 'VIEW' ? 'visibility' : 'table_rows' }}
            </span>
            <span class="truncate">{{ t.table_name }}</span>
          </button>
          <div v-if="tables.length === 0" class="px-2 py-2 text-[11px] text-outline italic text-center">No tables found</div>
        </div>
      </div>

      <div class="mt-3 pt-3 border-t border-outline-variant/30">
        <a href="#" @click.prevent="activeView = 'dashboard'"
          class="flex items-center gap-2 px-2 py-1.5 text-sm rounded transition-all"
          :class="activeView === 'dashboard' ? 'bg-secondary-container text-on-secondary-container font-semibold' : 'text-on-surface-variant hover:bg-surface-variant hover:text-on-surface'">
          <span class="material-symbols-outlined text-[18px]">analytics</span>
          Dashboard
        </a>

        <a href="#" @click.prevent="activeView = 'queries'"
          class="flex items-center gap-2 px-2 py-1.5 text-sm rounded transition-all"
          :class="activeView === 'queries' ? 'bg-secondary-container text-on-secondary-container font-semibold' : 'text-on-surface-variant hover:bg-surface-variant hover:text-on-surface'">
          <span class="material-symbols-outlined text-[18px]">terminal</span>
          Queries
        </a>
      </div>
    </div>

    <!-- Bottom links -->
    <div class="border-t border-outline-variant/30 p-2 flex gap-2">
      <button class="flex-1 flex items-center justify-center gap-1 px-2 py-1.5 text-xs text-on-surface-variant hover:text-on-surface hover:bg-surface-variant rounded transition-all">
        <span class="material-symbols-outlined text-[14px]">settings</span>
      </button>
      <button class="flex-1 flex items-center justify-center gap-1 px-2 py-1.5 text-xs text-on-surface-variant hover:text-on-surface hover:bg-surface-variant rounded transition-all">
        <span class="material-symbols-outlined text-[14px]">help</span>
      </button>
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
