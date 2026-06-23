<template>
  <div class="flex-1 flex flex-col h-full bg-surface-container-lowest overflow-y-auto px-edge-margin pt-edge-margin pb-24 items-center">
    <div class="w-full max-w-4xl flex flex-col gap-lg">
      <div class="flex justify-between items-end">
        <div>
          <h1 class="font-headline-lg text-headline-lg text-on-surface">Connection Manager</h1>
          <p class="font-body-md text-on-surface-variant text-body-md mt-xs">Configure access to your remote or local data sources.</p>
        </div>

      </div>

      <!-- Main Form Card -->
      <section class="w-full bg-surface-container-lowest border border-outline-variant rounded-xl shadow-lg shadow-black/5 overflow-hidden flex flex-col md:flex-row hover-lift">

        <!-- Left Sidebar Type List -->
        <div class="w-full md:w-1/3 bg-surface-container-low border-r border-outline-variant p-lg flex flex-col gap-md shrink-0">
          <h3 class="font-label-caps text-label-caps text-on-surface-variant">DATABASE TYPE</h3>

          <div class="space-y-sm">
            <button @click="selectDbType('POSTGRESQL')"
              class="w-full text-left flex items-start gap-md p-md rounded-lg transition-all group"
              :class="form.dbType === 'POSTGRESQL' ? 'border-2 border-primary bg-primary-container/[0.05]' : 'border border-outline-variant bg-surface hover:border-outline'">
              <div class="w-10 h-10 shrink-0 rounded bg-surface border border-outline-variant/30 flex items-center justify-center p-2 mt-0.5">
                <span class="material-symbols-outlined text-primary text-[22px]">database</span>
              </div>
              <div>
                <p class="font-bold text-on-surface">PostgreSQL</p>
                <p class="text-body-sm text-on-surface-variant">Recommended</p>
              </div>
            </button>
            <button @click="selectDbType('MYSQL')"
              class="w-full text-left flex items-start gap-md p-md rounded-lg transition-all group"
              :class="form.dbType === 'MYSQL' ? 'border-2 border-primary bg-primary-container/[0.05]' : 'border border-outline-variant bg-surface hover:border-outline'">
              <div class="w-10 h-10 shrink-0 rounded bg-surface-variant flex items-center justify-center p-2 mt-0.5 border border-transparent group-hover:border-outline-variant">
                <span class="material-symbols-outlined text-secondary text-[22px]">storage</span>
              </div>
              <div>
                <p class="font-bold text-on-surface">MySQL</p>
                <p class="text-body-sm text-on-surface-variant">Standard</p>
              </div>
            </button>
            <button @click="selectDbType('SQLITE')"
              class="w-full text-left flex items-start gap-md p-md rounded-lg transition-all group"
              :class="form.dbType === 'SQLITE' ? 'border-2 border-primary bg-primary-container/[0.05]' : 'border border-outline-variant bg-surface hover:border-outline'">
               <div class="w-10 h-10 shrink-0 rounded bg-surface-variant flex items-center justify-center p-2 mt-0.5 border border-transparent group-hover:border-outline-variant">
                <span class="material-symbols-outlined text-tertiary text-[22px]">dataset</span>
              </div>
              <div>
                <p class="font-bold text-on-surface">SQLite</p>
                <p class="text-body-sm text-on-surface-variant">Embedded</p>
              </div>
            </button>
            <button @click="selectDbType('ORACLE')"
              class="w-full text-left flex items-start gap-md p-md rounded-lg transition-all group"
              :class="form.dbType === 'ORACLE' ? 'border-2 border-primary bg-primary-container/[0.05]' : 'border border-outline-variant bg-surface hover:border-outline'">
               <div class="w-10 h-10 shrink-0 rounded bg-surface-variant flex items-center justify-center p-2 mt-0.5 border border-transparent group-hover:border-outline-variant">
                <span class="material-symbols-outlined text-outline text-[22px]">database</span>
              </div>
              <div>
                <p class="font-bold text-on-surface">Oracle DB</p>
                <p class="text-body-sm text-on-surface-variant">Enterprise</p>
              </div>
            </button>
          </div>

          <div class="mt-xl p-md rounded-lg border border-outline-variant bg-surface-container flex flex-col gap-xs mt-auto">
             <div class="flex items-center gap-xs">
                <span class="material-symbols-outlined text-[16px] text-on-surface-variant">info</span>
                <span class="text-label-caps text-on-surface-variant">SSH TUNNEL</span>
             </div>
             <p class="text-body-sm text-on-surface-variant leading-relaxed">Need to connect through a proxy? You can configure SSH tunneling in the Advanced Settings panel.</p>
          </div>
        </div>

        <!-- Right Side Form Fields -->
        <div class="w-full md:w-2/3 p-lg flex flex-col gap-md">
           <div class="col-span-2">
            <label class="block text-label-caps text-on-surface-variant mb-xs">CONNECTION NAME</label>
            <input class="w-full bg-surface border border-outline-variant rounded px-md py-sm focus:border-primary focus:ring-1 focus:ring-primary text-on-surface transition-all outline-none font-code-md" type="text" v-model="form.name"/>
          </div>

          <div class="grid grid-cols-2 gap-md">
            <div class="col-span-2 md:col-span-1 border-r border-transparent md:border-outline-variant/30 md:pr-4">
              <label class="block text-label-caps text-on-surface-variant mb-xs">HOST</label>
              <div class="relative">
                <input class="w-full bg-surface border border-outline-variant rounded px-md py-sm pl-10 focus:border-primary focus:ring-1 focus:ring-primary text-on-surface transition-all outline-none font-code-md" type="text" v-model="form.host"/>
                <span class="material-symbols-outlined absolute left-sm top-1/2 -translate-y-1/2 text-outline text-[18px]">dns</span>
              </div>
            </div>
            <div class="col-span-2 md:col-span-1">
              <label class="block text-label-caps text-on-surface-variant mb-xs">PORT</label>
              <input class="w-full bg-surface border border-outline-variant rounded px-md py-sm focus:border-primary focus:ring-1 focus:ring-primary text-on-surface transition-all outline-none font-code-md" type="number" v-model.number="form.port"/>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-md">
            <div class="col-span-2 md:col-span-1 border-r border-transparent md:border-outline-variant/30 md:pr-4">
              <label class="block text-label-caps text-on-surface-variant mb-xs">USER</label>
              <div class="relative">
                <input class="w-full bg-surface border border-outline-variant rounded px-md py-sm pl-10 focus:border-primary focus:ring-1 focus:ring-primary text-on-surface transition-all outline-none font-code-md" type="text" v-model="form.username"/>
                <span class="material-symbols-outlined absolute left-sm top-1/2 -translate-y-1/2 text-outline text-[18px]">person</span>
              </div>
            </div>
            <div class="col-span-2 md:col-span-1">
              <label class="block text-label-caps text-on-surface-variant mb-xs">PASSWORD</label>
              <div class="relative">
                <input :type="showPassword ? 'text' : 'password'" class="w-full bg-surface border border-outline-variant rounded px-md py-sm pl-10 pr-10 focus:border-primary focus:ring-1 focus:ring-primary text-on-surface transition-all outline-none font-code-md" v-model="form.password"/>
                <span class="material-symbols-outlined absolute left-sm top-1/2 -translate-y-1/2 text-outline text-[18px]">lock</span>
                <button class="material-symbols-outlined absolute right-sm top-1/2 -translate-y-1/2 text-outline hover:text-on-surface transition-colors text-[18px] cursor-pointer" @click="showPassword = !showPassword">{{ showPassword ? 'visibility_off' : 'visibility' }}</button>
              </div>
            </div>
          </div>

          <div class="col-span-2 mb-md hover:translate-y-0.5 transition-transform duration-300">
            <label class="block text-label-caps text-on-surface-variant mb-xs">DATABASE</label>
            <div class="relative">
              <input class="w-full bg-surface border border-outline-variant rounded px-md py-sm pl-10 focus:border-primary focus:ring-1 focus:ring-primary text-on-surface transition-all outline-none font-code-md" type="text" v-model="form.database"/>
              <span class="material-symbols-outlined absolute left-sm top-1/2 -translate-y-1/2 text-outline text-[18px]">folder_data</span>
            </div>
          </div>

          <!-- Status messages -->
          <div v-if="error" class="px-md py-sm bg-error-container text-on-error-container rounded-sm text-body-sm font-medium">{{ error }}</div>
          <div v-if="success" class="px-md py-sm bg-primary-container/20 text-primary rounded-sm text-body-sm font-medium">{{ success }}</div>

          <div class="mt-auto pt-lg border-t border-outline-variant flex items-center justify-between">
             <button @click="testConnection" :disabled="testing"
              class="flex items-center gap-sm px-md py-sm border border-outline-variant rounded font-medium text-on-surface font-body-md hover:bg-surface transition-all active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed">
              <span class="material-symbols-outlined text-outline">network_check</span>
              {{ testing ? 'Testing...' : 'Test Connection' }}
            </button>
            <div class="flex gap-md">
               <button class="text-on-surface-variant hover:text-on-surface font-bold text-body-md transition-colors px-md">Cancel</button>
               <button @click="saveConnection" :disabled="saving"
                class="bg-primary text-on-primary px-lg py-sm rounded border border-transparent font-bold text-body-md hover:opacity-90 active:scale-[0.98] transition-all shadow-sm disabled:opacity-50 disabled:cursor-not-allowed">
                {{ saving ? 'Saving...' : 'Connect' }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- Saved Connections List -->
      <section v-if="connections.length > 0" class="w-full bg-surface-container-lowest border border-outline-variant rounded-xl overflow-hidden">
        <div class="p-md border-b border-outline-variant">
          <h3 class="font-headline-md text-[16px] font-bold text-on-surface">Saved Connections</h3>
        </div>
        <div class="divide-y divide-outline-variant/40">
          <div v-for="conn in connections" :key="conn.id" class="flex items-center justify-between px-md py-sm hover:bg-surface-variant transition-colors">
            <div class="flex items-center gap-md">
              <span class="font-code-md text-on-surface">{{ conn.name }}</span>
              <span class="text-code-sm text-outline bg-surface-container-low px-2 py-0.5 rounded border border-outline-variant/30">{{ conn.dbType }}</span>
              <span class="text-body-sm text-on-surface-variant">{{ conn.host }}:{{ conn.port }}/{{ conn.database }}</span>
            </div>
            <button @click="deleteConnection(conn.id!)" class="material-symbols-outlined text-[18px] text-outline-variant hover:text-error transition-colors">delete</button>
          </div>
        </div>
      </section>


    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { connections, loadConnections } from '@/store/app'
import { connectionsApi, type ConnectionConfig } from '@/api/connections'

const form = ref<ConnectionConfig>({
  name: '',
  dbType: 'POSTGRESQL',
  host: '',
  port: 5432,
  database: '',
  username: '',
  password: '',
})

const testing = ref(false)
const saving = ref(false)
const error = ref('')
const success = ref('')
const showPassword = ref(false)

onMounted(loadConnections)

async function testConnection() {
  testing.value = true
  error.value = ''
  success.value = ''
  try {
    await connectionsApi.test(form.value)
    success.value = 'Connection successful!'
  } catch (e: any) {
    error.value = e.message || 'Connection failed'
  } finally {
    testing.value = false
  }
}

async function saveConnection() {
  saving.value = true
  error.value = ''
  success.value = ''
  try {
    await connectionsApi.create(form.value)
    await loadConnections()
    success.value = 'Connection saved!'
  } catch (e: any) {
    error.value = e.message || 'Failed to save'
  } finally {
    saving.value = false
  }
}

function selectDbType(type: ConnectionConfig['dbType']) {
  form.value.dbType = type
  if (type === 'POSTGRESQL') form.value.port = 5432
  else if (type === 'MYSQL') form.value.port = 3306
  else if (type === 'ORACLE') form.value.port = 1521
  else form.value.port = 0
}

async function deleteConnection(id: number) {
  try {
    await connectionsApi.delete(id)
    await loadConnections()
  } catch {}
}
</script>
