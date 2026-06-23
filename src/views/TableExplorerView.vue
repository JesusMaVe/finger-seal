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
           <button @click="refresh" :disabled="loading" class="bg-surface-container border border-outline-variant px-md py-1.5 rounded flex items-center gap-xs text-body-sm font-medium hover:bg-surface-container-high transition-colors text-on-surface disabled:opacity-50">
              <span class="material-symbols-outlined text-[18px]" :class="{ 'animate-spin': loading }">refresh</span>
              <span>{{ loading ? 'Loading...' : 'Refresh' }}</span>
           </button>
           <button @click="exportCSV" :disabled="!previewData.length" class="bg-surface-container border border-outline-variant px-md py-1.5 rounded flex items-center gap-xs text-body-sm font-medium hover:bg-surface-container-high transition-colors text-on-surface disabled:opacity-50">
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
         <button
           v-for="tab in tabs" :key="tab.id"
           @click="currentTab = tab.id"
           class="pb-sm font-medium text-body-md flex items-center gap-xs transition-colors border-b-2"
           :class="currentTab === tab.id
             ? 'border-primary text-primary font-bold'
             : 'text-on-surface-variant border-transparent hover:text-on-surface hover:border-outline-variant'"
         >
            <span class="material-symbols-outlined text-[18px]">{{ tab.icon }}</span>
            {{ tab.label }}
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
         <div v-if="currentTab === 'schema'" class="col-span-12 lg:col-span-8 bg-surface-container-low border border-outline-variant rounded-xl overflow-hidden flex flex-col min-h-[400px]">
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
                   <p class="font-headline-md text-[20px] text-primary">{{ tableStats.row_count ?? tableStats.TABLE_ROWS ?? tableStats.COUNT ?? '-' }}</p>
                </div>
                <div class="bg-surface p-sm rounded-lg border border-outline-variant/60">
                   <p class="text-label-caps text-outline uppercase text-[10px] tracking-wide mb-1">Table Size</p>
                   <p class="font-headline-md text-[20px] text-primary">{{ tableStats.total_size ?? tableStats.total_size_mb ?? '-' }}</p>
                </div>
                <div class="bg-surface p-sm rounded-lg border border-outline-variant/60">
                   <p class="text-label-caps text-outline uppercase text-[10px] tracking-wide mb-1">Avg Row Width</p>
                   <p class="font-headline-md text-[20px] text-primary">{{ tableStats.avg_row_width ?? tableStats.avg_row_len ?? '-' }}</p>
                </div>
                <div class="bg-surface p-sm rounded-lg border border-outline-variant/60">
                   <p class="text-label-caps text-outline uppercase text-[10px] tracking-wide mb-1">Index Size</p>
                   <p class="font-headline-md text-[20px] text-primary">{{ tableStats.index_size ?? tableStats.index_size_mb ?? '-' }}</p>
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
         <div v-if="currentTab === 'data'" class="col-span-12 lg:col-span-8 bg-surface-container-low border border-outline-variant rounded-xl overflow-hidden">
            <div class="p-md border-b border-outline-variant flex justify-between items-center bg-surface flex-wrap gap-2">
               <h3 class="font-headline-md text-[18px] font-bold text-on-surface flex items-center gap-xs">
                  <span class="material-symbols-outlined text-outline">table_rows</span>
                  Data Preview <span v-if="selectedTable" class="text-outline font-body-md font-normal ml-xs">(Top {{ previewData.length }})</span>
               </h3>
               <div class="flex items-center gap-sm">
                 <div class="flex rounded-sm border border-outline-variant overflow-hidden bg-surface-container-low">
                   <button @click="dataView = 'table'"
                     class="px-3 py-1 text-body-sm font-bold transition-colors"
                     :class="dataView === 'table' ? 'bg-surface-variant text-on-surface border-r border-outline-variant' : 'bg-transparent text-on-surface-variant hover:bg-surface'">
                     Table View
                   </button>
                   <button @click="dataView = 'json'"
                     class="px-3 py-1 text-body-sm font-bold transition-colors"
                     :class="dataView === 'json' ? 'bg-surface-variant text-on-surface border-l border-outline-variant' : 'bg-transparent text-on-surface-variant hover:bg-surface'">
                     JSON View
                   </button>
                 </div>
               </div>
            </div>

            <div v-if="previewData.length && dataView === 'table'" class="overflow-x-auto custom-scrollbar bg-surface-container-lowest">
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
            <div v-else-if="selectedTable && !loading && dataView === 'table'" class="flex items-center justify-center h-32 text-on-surface-variant font-body-sm italic">
              No data available
            </div>
            <!-- JSON View -->
            <div v-if="dataView === 'json' && previewData.length" class="bg-surface-container-lowest p-md">
              <pre class="font-code-sm text-on-surface-variant whitespace-pre-wrap overflow-x-auto max-h-96 custom-scrollbar">{{ JSON.stringify(previewData, null, 2) }}</pre>
            </div>
         </div>

         <!-- Relationships Tab -->
         <div v-if="currentTab === 'relationships'" class="col-span-12 lg:col-span-8 bg-surface-container-low border border-outline-variant rounded-xl overflow-hidden">
           <div class="p-md border-b border-outline-variant bg-surface">
             <h3 class="font-headline-md text-[18px] font-bold text-on-surface">Foreign Keys</h3>
           </div>
           <table v-if="foreignKeys.length" class="w-full text-left border-collapse bg-surface-container-lowest">
             <thead class="bg-surface-container text-label-caps text-outline uppercase border-b border-outline-variant">
               <tr>
                 <th class="px-md py-sm font-bold">FK Column</th>
                 <th class="px-md py-sm font-bold">References</th>
                 <th class="px-md py-sm font-bold">PK Column</th>
                 <th class="px-md py-sm font-bold">Constraint</th>
               </tr>
             </thead>
             <tbody class="divide-y divide-outline-variant/20 font-code-sm">
               <tr v-for="(fk, i) in foreignKeys" :key="i" class="hover:bg-surface-container-low transition-colors">
                 <td class="px-md py-sm text-on-surface">{{ fk.fk_column }}</td>
                 <td class="px-md py-sm text-primary">→ {{ fk.pk_table }}</td>
                 <td class="px-md py-sm text-on-surface-variant">{{ fk.pk_column }}</td>
                 <td class="px-md py-sm text-outline">{{ fk.fk_name || '-' }}</td>
               </tr>
             </tbody>
           </table>
           <div v-else class="flex items-center justify-center h-32 text-on-surface-variant font-body-sm italic">
             No foreign key relationships found for this table
           </div>
         </div>

         <!-- Change Log Tab -->
         <div v-if="currentTab === 'changelog'" class="col-span-12 lg:col-span-8 bg-surface-container-low border border-outline-variant rounded-xl overflow-hidden">
           <div class="p-md border-b border-outline-variant bg-surface">
             <h3 class="font-headline-md text-[18px] font-bold text-on-surface">Recent Queries on {{ selectedTable }}</h3>
           </div>
           <div v-if="changelog.length" class="divide-y divide-outline-variant/20 bg-surface-container-lowest">
             <div v-for="(entry, i) in changelog" :key="i" class="p-md hover:bg-surface-container-low transition-colors">
               <pre class="font-code-sm text-on-surface whitespace-pre-wrap">{{ entry.sql }}</pre>
               <p class="text-[11px] text-outline mt-1">{{ entry.createdAt }}</p>
             </div>
           </div>
           <div v-else class="flex items-center justify-center h-32 text-on-surface-variant font-body-sm italic">
             No query history for this table
           </div>
         </div>

       </div>
     </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { schemasApi, type ColumnInfo } from '@/api/schemas'
import { queryApi } from '@/api/query'
import { connections, loadConnections, selectedConnectionId, selectedTable } from '@/store/app'

const columns = ref<ColumnInfo[]>([])
const foreignKeys = ref<Record<string, unknown>[]>([])
const changelog = ref<{sql: string, createdAt: string}[]>([])
const previewData = ref<Record<string, unknown>[]>([])
const tableStats = ref<Record<string, unknown>>({})
const loading = ref(false)

const tabs = [
  { id: 'schema', label: 'Schema', icon: 'schema' },
  { id: 'data', label: 'Data Preview', icon: 'database' },
  { id: 'relationships', label: 'Relationships', icon: 'link' },
  { id: 'changelog', label: 'Change Log', icon: 'history' },
] as const
type TabId = (typeof tabs)[number]['id']
const currentTab = ref<TabId>('schema')
const dataView = ref<'table' | 'json'>('table')

const currentConn = computed(() => connections.value.find(c => c.id === selectedConnectionId.value))

const previewColumns = computed(() =>
  previewData.value.length > 0 ? Object.keys(previewData.value[0]) : []
)

onMounted(loadConnections)

watch(selectedConnectionId, () => {
  columns.value = []
  foreignKeys.value = []
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
    const [cols, data, stats, fks] = await Promise.all([
      schemasApi.tableColumns(connId, table),
      schemasApi.tableData(connId, table, 100),
      schemasApi.tableStats(connId, table),
      schemasApi.tableForeignKeys(connId, table),
    ])
    columns.value = cols
    previewData.value = data
    tableStats.value = stats
    foreignKeys.value = fks

    try {
      const history = await queryApi.history(connId)
      changelog.value = history.filter((h: any) => h.sql?.toLowerCase().includes((table as string).toLowerCase())).slice(0, 10)
    } catch {
      changelog.value = []
    }
  } catch {
    columns.value = []
    previewData.value = []
    tableStats.value = {}
    foreignKeys.value = []
    changelog.value = []
  } finally {
    loading.value = false
  }
})

async function refresh() {
  if (selectedTable.value && selectedConnectionId.value) {
    loading.value = true
    try {
      const [cols, data, stats, fks] = await Promise.all([
        schemasApi.tableColumns(selectedConnectionId.value, selectedTable.value),
        schemasApi.tableData(selectedConnectionId.value, selectedTable.value, 100),
        schemasApi.tableStats(selectedConnectionId.value, selectedTable.value),
        schemasApi.tableForeignKeys(selectedConnectionId.value, selectedTable.value),
      ])
      columns.value = cols
      previewData.value = data
      tableStats.value = stats
      foreignKeys.value = fks

      const history = await queryApi.history(selectedConnectionId.value)
      changelog.value = history.filter((h: any) => h.sql?.toLowerCase().includes((selectedTable.value as string).toLowerCase())).slice(0, 10)
    } catch {
      columns.value = []
      previewData.value = []
      tableStats.value = {}
      foreignKeys.value = []
      changelog.value = []
    } finally {
      loading.value = false
    }
  }
}

function exportCSV() {
  if (!previewData.value.length || !selectedTable.value) return
  const rows = previewData.value
  const cols = previewColumns.value
  if (!cols.length) return

  const csv = [
    cols.map(escapeCsv).join(','),
    ...rows.map(r => cols.map(c => escapeCsv(String(r[c] ?? ''))).join(',')),
  ].join('\n')

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${selectedTable.value}_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

function escapeCsv(val: string): string {
  if (val.includes(',') || val.includes('"') || val.includes('\n')) {
    return `"${val.replace(/"/g, '""')}"`
  }
  return val
}
</script>
