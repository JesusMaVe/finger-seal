<template>
  <div class="flex-1 flex flex-col overflow-hidden bg-surface-container-lowest h-full">
    <!-- Header Section -->
     <div class="flex flex-col gap-md p-md bg-surface border-b border-outline-variant shrink-0 relative z-10">
      <div class="flex justify-between items-end">
        <div>
           <div class="flex items-center gap-xs text-outline mb-1 font-body-sm text-[12px]">
             <span>public</span>
             <span class="material-symbols-outlined text-[14px]">chevron_right</span>
             <span>tables</span>
           </div>
           <h1 class="font-headline-lg text-[28px] font-bold text-on-surface flex items-center gap-sm">
             <span class="material-symbols-outlined text-primary text-[32px]" style="font-variation-settings: 'FILL' 1;">table_chart</span>
             users
           </h1>
        </div>
        <div class="flex gap-sm">
           <button class="bg-surface-container border border-outline-variant px-md py-1.5 rounded flex items-center gap-xs text-body-sm font-medium hover:bg-surface-container-high transition-colors text-on-surface">
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
       <div class="grid grid-cols-12 gap-md h-fit max-w-[1400px] mx-auto">
         
         <!-- Columns/Schema Definition Panel -->
         <div class="col-span-12 lg:col-span-8 bg-surface-container-low border border-outline-variant rounded-xl overflow-hidden flex flex-col min-h-[400px]">
           <div class="p-md border-b border-outline-variant flex justify-between items-center bg-surface flex-wrap gap-2">
              <h3 class="font-headline-md text-[18px] font-bold text-on-surface">Columns</h3>
              <div class="flex items-center gap-sm">
                <span class="text-code-sm text-outline">12 Columns total</span>
                <div class="w-[1px] h-4 bg-outline-variant mx-1"></div>
                <button class="text-primary hover:underline text-body-sm font-bold flex items-center gap-xs">
                  <span class="material-symbols-outlined text-[16px]">add</span>
                  Add Column
                </button>
              </div>
           </div>
           
           <div class="overflow-x-auto flex-1 bg-surface-container-lowest">
              <table class="w-full text-left border-collapse">
                <thead class="bg-surface-container-low text-label-caps text-outline uppercase border-b border-outline-variant">
                  <tr>
                    <th class="px-md py-sm font-bold">Name</th>
                    <th class="px-md py-sm font-bold">Type</th>
                    <th class="px-md py-sm font-bold">Constraint</th>
                    <th class="px-md py-sm font-bold">Nullable</th>
                    <th class="px-md py-sm font-bold">Default</th>
                    <th class="px-md py-sm font-bold text-center">Actions</th>
                  </tr>
                </thead>
                <tbody class="font-code-md divide-y divide-outline-variant/40">
                  <tr v-for="(col, i) in columns" :key="i" class="hover:bg-surface-container-low transition-colors group">
                     <td class="px-md py-sm text-on-surface">{{ col.name }}</td>
                     <td class="px-md py-sm">
                        <span class="px-2 py-0.5 rounded-sm bg-surface text-[11px] border border-outline-variant/30 text-on-surface-variant font-code-sm">{{ col.type }}</span>
                     </td>
                     <td class="px-md py-sm flex items-center gap-1 font-body-sm text-[12px] h-full pt-3">
                        <span v-if="col.constraintIcon" class="material-symbols-outlined text-[16px]" :class="col.color" style="font-variation-settings: 'FILL' 1;">{{ col.constraintIcon }}</span>
                        <span :class="col.constraint ? 'text-on-surface' : 'text-outline'">{{ col.constraint || '-' }}</span>
                     </td>
                     <td class="px-md py-sm font-body-md text-[13px]" :class="col.nullable === 'NO' ? 'text-error font-bold' : 'text-on-surface-variant'">{{ col.nullable }}</td>
                     <td class="px-md py-sm text-outline-variant italic group-hover:text-outline transition-colors">{{ col.defaultVal }}</td>
                     <td class="px-md py-sm text-center">
                        <button class="material-symbols-outlined text-outline-variant hover:text-primary transition-colors cursor-pointer text-[20px]">more_vert</button>
                     </td>
                  </tr>
                </tbody>
              </table>
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
             <div class="grid grid-cols-2 gap-sm">
                <div class="bg-surface p-sm rounded-lg border border-outline-variant/60">
                   <p class="text-label-caps text-outline uppercase text-[10px] tracking-wide mb-1">Total Rows</p>
                   <p class="font-headline-md text-[20px] text-primary">124,802</p>
                </div>
                <div class="bg-surface p-sm rounded-lg border border-outline-variant/60">
                   <p class="text-label-caps text-outline uppercase text-[10px] tracking-wide mb-1">Table Size</p>
                   <p class="font-headline-md text-[20px] text-primary">42.5 MB</p>
                </div>
                <div class="bg-surface p-sm rounded-lg border border-outline-variant/60">
                   <p class="text-label-caps text-outline uppercase text-[10px] tracking-wide mb-1">Index Size</p>
                   <p class="font-headline-md text-[20px] text-primary">18.2 MB</p>
                </div>
                <div class="bg-surface p-sm rounded-lg border border-outline-variant/60">
                   <p class="text-label-caps text-outline uppercase text-[10px] tracking-wide mb-1">Avg Row Width</p>
                   <p class="font-headline-md text-[20px] text-primary">248B</p>
                </div>
             </div>
           </div>

           <!-- Activity Log -->
           <div class="bg-surface-container-low border border-outline-variant rounded-xl p-md flex-1 overflow-hidden flex flex-col">
              <h3 class="font-headline-md text-[16px] font-bold text-on-surface mb-md flex items-center gap-xs shrink-0">
                 <span class="material-symbols-outlined text-outline text-[20px]">history</span>
                 Activity Log
              </h3>
              <div class="flex flex-col gap-md overflow-y-auto custom-scrollbar flex-1 relative">
                 <div class="absolute left-1 top-2 bottom-2 w-[2px] bg-outline-variant/30"></div>
                 <div v-for="(log, i) in activities" :key="i" class="flex gap-md relative z-10">
                   <div class="w-2.5 h-2.5 rounded-full mt-1 border-2 border-surface-container-low shrink-0" :class="log.color"></div>
                   <div>
                     <p class="text-body-sm font-bold text-on-surface">{{ log.title }}</p>
                     <p class="text-[11px] text-outline mt-0.5 leading-snug" v-html="log.desc"></p>
                     <p class="text-[10px] text-outline-variant mt-1 font-body-sm italic">{{ log.time }}</p>
                   </div>
                 </div>
              </div>
           </div>
         </div>

         <!-- Data Preview Section Layout -->
         <div class="col-span-12 bg-surface-container-low border border-outline-variant rounded-xl overflow-hidden mt-sm mb-xl">
            <div class="p-md border-b border-outline-variant flex justify-between items-center bg-surface flex-wrap gap-2">
               <h3 class="font-headline-md text-[18px] font-bold text-on-surface flex items-center gap-xs">
                  <span class="material-symbols-outlined text-outline">table_rows</span>
                  Data Preview <span class="text-outline font-body-md font-normal ml-xs">(Top 100)</span>
               </h3>
               <div class="flex items-center gap-sm">
                 <div class="flex rounded-sm border border-outline-variant overflow-hidden bg-surface-container-low">
                   <button class="px-3 py-1 bg-surface-variant border-r border-outline-variant text-on-surface text-body-sm font-bold hover:bg-surface-container-high transition-colors">Table View</button>
                   <button class="px-3 py-1 bg-transparent text-on-surface-variant text-body-sm font-medium hover:bg-surface transition-colors">JSON View</button>
                 </div>
               </div>
            </div>
            
            <div class="overflow-x-auto custom-scrollbar bg-surface-container-lowest">
               <table class="w-full text-left border-collapse">
                 <thead class="bg-surface-container text-label-caps text-outline uppercase border-b border-outline-variant">
                   <tr>
                     <th class="px-md py-xs font-bold border-r border-outline-variant/30">id</th>
                     <th class="px-md py-xs font-bold border-r border-outline-variant/30">email</th>
                     <th class="px-md py-xs font-bold border-r border-outline-variant/30">full_name</th>
                     <th class="px-md py-xs font-bold border-r border-outline-variant/30">account_id</th>
                     <th class="px-md py-xs font-bold border-r border-outline-variant/30">created_at</th>
                     <th class="px-md py-xs font-bold">status</th>
                   </tr>
                 </thead>
                 <tbody class="font-code-sm text-on-surface-variant divide-y divide-outline-variant/20">
                    <tr v-for="i in 5" :key="i" class="hover:bg-surface-variant transition-colors group cursor-pointer">
                      <td class="px-md py-1 border-r border-outline-variant/20 group-hover:text-on-surface transition-colors">f47ac10b...</td>
                      <td class="px-md py-1 border-r border-outline-variant/20 text-on-surface">j{{i}}.smith@example.com</td>
                      <td class="px-md py-1 border-r border-outline-variant/20 font-body-sm text-[13px]">John Smith</td>
                      <td class="px-md py-1 border-r border-outline-variant/20 text-on-surface text-right">100{{24+i}}</td>
                      <td class="px-md py-1 border-r border-outline-variant/20 text-outline group-hover:text-on-surface-variant">2023-10-25 10:45</td>
                      <td class="px-md py-1 flex items-center gap-2">
                         <div class="w-2 h-2 rounded-full" :class="i===3?'bg-orange-400':'bg-emerald-500'"></div>
                         <span class="font-body-sm text-[12px] text-on-surface">{{i===3?'Suspended':'Active'}}</span>
                      </td>
                    </tr>
                 </tbody>
               </table>
            </div>
         </div>
         
       </div>
     </div>
  </div>
</template>

<script setup lang="ts">
const columns = [
  { name: 'id', type: 'UUID', constraint: 'PK', constraintIcon: 'key', color: 'text-primary', nullable: 'NO', defaultVal: 'gen_random_uuid()' },
  { name: 'email', type: 'VARCHAR(255)', constraint: 'UNIQUE', constraintIcon: '', color: '', nullable: 'NO', defaultVal: 'NULL' },
  { name: 'full_name', type: 'TEXT', constraint: '', constraintIcon: '', color: '', nullable: 'YES', defaultVal: 'NULL' },
  { name: 'account_id', type: 'INTEGER', constraint: 'FK', constraintIcon: 'link', color: 'text-outline', nullable: 'NO', defaultVal: '-' },
  { name: 'created_at', type: 'TIMESTAMP', constraint: '', constraintIcon: '', color: '', nullable: 'NO', defaultVal: 'now()' },
  { name: 'last_login', type: 'TIMESTAMP', constraint: '', constraintIcon: '', color: '', nullable: 'YES', defaultVal: 'NULL' },
]

const activities = [
  { title: 'Schema updated', desc: 'Modified <span class="text-on-surface font-code-sm border border-outline-variant/30 rounded px-1 bg-surface">last_login</span> col by admin', time: '2 hours ago', color: 'bg-primary' },
  { title: 'Bulk insert execution', desc: '5,000 rows added via API', time: 'Yesterday, 14:22', color: 'bg-outline-variant' },
  { title: 'Index Rebuild', desc: 'Primary key re-indexed automatically', time: 'Oct 24, 09:10', color: 'bg-outline-variant' },
]
</script>
