<template>
  <div class="flex-1 flex flex-col overflow-hidden bg-surface-container-lowest h-full">
    <!-- Connection info bar -->
    <div class="flex items-center gap-md px-md py-sm bg-surface border-b border-outline-variant shrink-0">
      <span v-if="selectedConnectionId" class="text-body-sm font-code-sm text-on-surface">{{ currentConn?.name }} <span class="text-outline">({{ currentConn?.dbType }})</span></span>
      <span v-else class="text-body-sm text-outline italic">No connection selected</span>
      <span class="text-code-sm text-on-surface-variant mx-2">/</span>
      <span v-if="selectedTable" class="font-code-sm text-on-surface font-bold">{{ selectedTable }}</span>
      <span v-else class="text-body-sm text-outline italic">Select a table from the sidebar</span>
      <span v-if="loading" class="text-code-sm text-outline animate-pulse ml-auto">Loading...</span>
    </div>

    <!-- Header Section -->
     <div class="flex flex-col gap-md p-md bg-surface border-b border-outline-variant shrink-0 relative z-10">
      <div class="flex justify-between items-end">
        <div>
           <div class="flex items-center gap-xs text-outline mb-1 font-body-sm text-[12px]">
             <span>{{ selectedConnectionId ? (connections.find(c => c.id === selectedConnectionId)?.name || 'DB') : 'No connection' }}</span>
             <span class="material-symbols-outlined text-[14px]">chevron_right</span>
             <span>tables</span>
           </div>
           <h1 class="font-headline-lg text-[28px] font-bold text-on-surface flex items-center gap-sm">
             <span class="material-symbols-outlined text-primary text-[32px]" style="font-variation-settings: 'FILL' 1;">table_chart</span>
             {{ selectedTable || 'Select a table' }}
           </h1>
        </div>
        <div class="flex gap-sm">
           <button @click="refresh" class="bg-surface-container border border-outline-variant px-md py-1.5 rounded flex items-center gap-xs text-body-sm font-medium hover:bg-surface-container-high transition-colors text-on-surface">
              <span class="material-symbols-outlined text-[18px]">refresh</span>
              Refresh
           </button>
           <button class="bg-surface-container border border-outline-variant px-md py-1.5 rounded flex items-center gap-xs text-body-sm font-medium hover:bg-surface-container-high transition-colors text-on-surface">
              <span class="material-symbols-outlined text-[18px]">download</span>
              Export
           </button>
           <button class="bg-primary text-on-primary px-md py-1.5 rounded flex items-center gap-xs text-body-sm font-bold shadow-md shadow-primary/20 hover:opacity-90 active:scale-[0.98] transition-all">
              <span class="material-symbols-outlined text-[18px]">play_arrow</span>
              Query Table
           </button>
        </div>
      </div>

       <!-- Tabs Area -->
       <div class="flex gap-lg border-b border-transparent mt-2">
         <button class="border-b-2 border-primary text-primary pb-sm font-bold text-body-md flex items-center gap-xs">
            <span class="material-symbols-outlined text-[18px]">schema</span>
            Schema
         </button>
         <button class="text-on-surface-variant pb-sm font-medium text-body-md flex items-center gap-xs hover:text-on-surface transition-colors border-b-2 border-transparent hover:border-outline-variant">
            <span class="material-symbols-outlined text-[18px]">database</span>
            Data Preview
         </button>
         <button class="text-on-surface-variant pb-sm font-medium text-body-md flex items-center gap-xs hover:text-on-surface transition-colors border-b-2 border-transparent hover:border-outline-variant">
            <span class="material-symbols-outlined text-[18px]">link</span>
            Relationships
         </button>
         <button class="text-on-surface-variant pb-sm font-medium text-body-md flex items-center gap-xs hover:text-on-surface transition-colors border-b-2 border-transparent hover:border-outline-variant">
            <span class="material-symbols-outlined text-[18px]">history</span>
            Change Log
         </button>
       </div>
       <div class="absolute bottom-0 left-0 w-full h-[1px] bg-outline-variant/60"></div>
     </div>

     <!-- Bento Grid Content -->
     <div class="flex-1 overflow-y-auto custom-scrollbar p-md bg-surface-container-lowest">
       <div v-if="!selectedConnectionId" class="flex items-center justify-center h-64 text-on-surface-variant font-body-md">
         Select a connection and table to view schema.
       </div>
       <div v-else class="grid grid-cols-12 gap-md h-fit max-w-[1400px] mx-auto">

         <!-- Columns/Schema Definition Panel -->
         <div class="col-span-12 lg:col-span-8 bg-surface-container-low border border-outline-variant rounded-xl overflow-hidden flex flex-col min-h-[400px]">
           <div class="p-md border-b border-outline-variant flex justify-between items-center bg-surface flex-wrap gap-2">
              <h3 class="font-headline-md text-[18px] font-bold text-on-surface">Columns</h3>
              <div class="flex items-center gap-sm">
                <span class="text-code-sm text-outline">{{ columns.length }} Columns total</span>
              </div>
           </div>

           <div class="overflow-x-auto flex-1 bg-surface-container-lowest">
              <table v-if="columns.length" class="w-full text-left border-collapse">
                <thead class="bg-surface-container-low text-label-caps text-outline uppercase border-b border-outline-variant">
                  <tr>
                    <th class="px-md py-sm font-bold">Name</th>
                    <th class="px-md py-sm font-bold">Type</th>
                    <th class="px-md py-sm font-bold">Size</th>
                    <th class="px-md py-sm font-bold">Nullable</th>
                    <th class="px-md py-sm font-bold">Default</th>
                    <th class="px-md py-sm font-bold text-center">Actions</th>
                  </tr>
                </thead>
                <tbody class="font-code-md divide-y divide-outline-variant/40">
                  <tr v-for="(col, i) in columns" :key="i" class="hover:bg-surface-container-low transition-colors group">
                     <td class="px-md py-sm text-on-surface font-code-sm">{{ col.name }}</td>
                     <td class="px-md py-sm">
                        <span class="px-2 py-0.5 rounded-sm bg-surface text-[11px] border border-outline-variant/30 text-on-surface-variant font-code-sm">{{ col.type }}</span>
                     </td>
                     <td class="px-md py-sm text-on-surface-variant font-body-sm text-[12px]">{{ col.size || '-' }}</td>
                     <td class="px-md py-sm font-body-md text-[13px]" :class="col.nullable === 'NO' ? 'text-error font-bold' : 'text-on-surface-variant'">{{ col.nullable }}</td>
                     <td class="px-md py-sm text-outline-variant italic group-hover:text-outline transition-colors font-code-sm">{{ col.default || '-' }}</td>
                     <td class="px-md py-sm text-center">
                        <button class="material-symbols-outlined text-outline-variant hover:text-primary transition-colors cursor-pointer text-[20px]">more_vert</button>
                     </td>
                  </tr>
                </tbody>
              </table>
              <div v-else-if="!loading" class="flex items-center justify-center h-32 text-on-surface-variant font-body-sm italic">No columns found</div>
           </div>
         </div>

         <!-- Statistics & Health Panel -->
         <div class="col-span-12 lg:col-span-4 flex flex-col gap-md">

           <!-- Table Insights -->
           <div class="bg-surface-container-low border border-outline-variant rounded-xl p-md">
             <h3 class="font-headline-md text-[16px] font-bold text-on-surface mb-md flex items-center gap-xs">
                <span class="material-symbols-outlined text-outline text-[20px]">analytics</span>
                Table Insights
             </h3>
             <div v-if="selectedTable" class="grid grid-cols-2 gap-sm">
                <div class="bg-surface p-sm rounded-lg border border-outline-variant/60">
                   <p class="text-label-caps text-outline uppercase text-[10px] tracking-wide mb-1">Total Rows</p>
                   <p class="font-headline-md text-[20px] text-primary">{{ tableStats.row_count ?? '-' }}</p>
                </div>
                <div class="bg-surface p-sm rounded-lg border border-outline-variant/60">
                   <p class="text-label-caps text-outline uppercase text-[10px] tracking-wide mb-1">Table Size</p>
                   <p class="font-headline-md text-[20px] text-primary">{{ tableStats.total_size ?? '-' }}</p>
                </div>
                <div class="bg-surface p-sm rounded-lg border border-outline-variant/60">
                   <p class="text-label-caps text-outline uppercase text-[10px] tracking-wide mb-1">Avg Row Width</p>
                   <p class="font-headline-md text-[20px] text-primary">{{ tableStats.avg_row_width ?? '-' }}</p>
                </div>
                <div class="bg-surface p-sm rounded-lg border border-outline-variant/60">
                   <p class="text-label-caps text-outline uppercase text-[10px] tracking-wide mb-1">Index Size</p>
                   <p class="font-headline-md text-[20px] text-primary">{{ tableStats.index_size ?? '-' }}</p>
                </div>
             </div>
             <div v-else class="flex items-center justify-center h-24 text-on-surface-variant font-body-sm italic">
               Select a table to view insights
             </div>
           </div>

           <!-- Activity Log -->
           <div class="bg-surface-container-low border border-outline-variant rounded-xl p-md flex-1 overflow-hidden flex flex-col">
              <h3 class="font-headline-md text-[16px] font-bold text-on-surface mb-md flex items-center gap-xs shrink-0">
                 <span class="material-symbols-outlined text-outline text-[20px]">history</span>
                 Activity Log
              </h3>
              <div class="flex items-center justify-center h-full text-on-surface-variant font-body-sm italic">
                No recent activity
              </div>
           </div>
         </div>

         <!-- Data Preview Section Layout -->
         <div class="col-span-12 bg-surface-container-low border border-outline-variant rounded-xl overflow-hidden mt-sm mb-xl">
            <div class="p-md border-b border-outline-variant flex justify-between items-center bg-surface flex-wrap gap-2">
               <h3 class="font-headline-md text-[18px] font-bold text-on-surface flex items-center gap-xs">
                  <span class="material-symbols-outlined text-outline">table_rows</span>
                  Data Preview <span v-if="selectedTable" class="text-outline font-body-md font-normal ml-xs">(Top {{ previewData.length }})</span>
               </h3>
               <div class="flex items-center gap-sm">
                 <div class="flex rounded-sm border border-outline-variant overflow-hidden bg-surface-container-low">
                   <button class="px-3 py-1 bg-surface-variant border-r border-outline-variant text-on-surface text-body-sm font-bold hover:bg-surface-container-high transition-colors">Table View</button>
                   <button class="px-3 py-1 bg-transparent text-on-surface-variant text-body-sm font-medium hover:bg-surface transition-colors">JSON View</button>
                 </div>
               </div>
            </div>

            <div v-if="previewData.length" class="overflow-x-auto custom-scrollbar bg-surface-container-lowest">
               <table class="w-full text-left border-collapse">
                 <thead class="bg-surface-container text-label-caps text-outline uppercase border-b border-outline-variant">
                   <tr>
                     <th class="px-md py-xs font-bold border-r border-outline-variant/30">#</th>
                     <th v-for="col in previewColumns" :key="col" class="px-md py-xs font-bold border-r border-outline-variant/30">{{ col }}</th>
                   </tr>
                 </thead>
                 <tbody class="font-code-sm text-on-surface-variant divide-y divide-outline-variant/20">
                    <tr v-for="(row, idx) in previewData" :key="idx" class="hover:bg-surface-variant transition-colors group cursor-pointer">
                      <td class="px-md py-1 border-r border-outline-variant/20 text-outline group-hover:text-on-surface-variant">{{ idx + 1 }}</td>
                      <td v-for="col in previewColumns" :key="col" class="px-md py-1 border-r border-outline-variant/20 group-hover:text-on-surface transition-colors">{{ row[col] ?? '' }}</td>
                    </tr>
                 </tbody>
               </table>
            </div>
            <div v-else-if="selectedTable && !loading" class="flex items-center justify-center h-32 text-on-surface-variant font-body-sm italic">
              No data available
            </div>
         </div>

       </div>
     </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { schemasApi, type ColumnInfo } from '@/api/schemas'
import { connectionsApi, type ConnectionConfig } from '@/api/connections'
import { selectedConnectionId, selectedTable } from '@/store/app'

const connections = ref<ConnectionConfig[]>([])
const columns = ref<ColumnInfo[]>([])
const previewData = ref<Record<string, unknown>[]>([])
const tableStats = ref<Record<string, unknown>>({})
const loading = ref(false)

const currentConn = computed(() => connections.value.find(c => c.id === selectedConnectionId.value))

const previewColumns = computed(() =>
  previewData.value.length > 0 ? Object.keys(previewData.value[0]) : []
)

onMounted(() => {
  connectionsApi.list().then(data => connections.value = data).catch(() => {})
})

watch(selectedConnectionId, () => {
  columns.value = []
  previewData.value = []
  tableStats.value = {}
})

watch([selectedConnectionId, selectedTable], async ([connId, table]) => {
  if (!connId || !table) {
    columns.value = []
    previewData.value = []
    tableStats.value = {}
    return
  }
  loading.value = true
  try {
    const [cols, data, stats] = await Promise.all([
      schemasApi.tableColumns(connId, table),
      schemasApi.tableData(connId, table, 100),
      schemasApi.tableStats(connId, table),
    ])
    columns.value = cols
    previewData.value = data
    tableStats.value = stats
  } catch {
    columns.value = []
    previewData.value = []
    tableStats.value = {}
  } finally {
    loading.value = false
  }
})

async function refresh() {
  if (selectedTable.value && selectedConnectionId.value) {
    loading.value = true
    try {
      const [cols, data, stats] = await Promise.all([
        schemasApi.tableColumns(selectedConnectionId.value, selectedTable.value),
        schemasApi.tableData(selectedConnectionId.value, selectedTable.value, 100),
        schemasApi.tableStats(selectedConnectionId.value, selectedTable.value),
      ])
      columns.value = cols
      previewData.value = data
      tableStats.value = stats
    } catch {
      columns.value = []
      previewData.value = []
      tableStats.value = {}
    } finally {
      loading.value = false
    }
  }
}
</script>
