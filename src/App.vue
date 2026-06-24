<template>
  <div class="flex flex-col bg-background text-on-background h-screen overflow-hidden">
    <AppHeader />
    <div class="flex flex-1 overflow-hidden min-h-0">
      <AppSidebar />
      <main class="flex-1 flex flex-col bg-surface-container-lowest min-w-0 overflow-y-auto custom-scrollbar fade-in">
        <!-- Dynamic component based on state -->
        <component :is="currentViewComponent" />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import AppHeader from './components/AppHeader.vue'
import AppSidebar from './components/AppSidebar.vue'
import { useAppStore } from './store/app'
import { checkForUpdate } from './tauri/updater'

const appStore = useAppStore()
const { activeView, theme } = storeToRefs(appStore)
const { applyTheme } = appStore

onMounted(async () => {
  applyTheme(theme.value)
  await checkForUpdate()
})
watch(theme, applyTheme)

import DashboardView from './views/DashboardView.vue'
import ConnectionManagerView from './views/ConnectionManagerView.vue'
import TableExplorerView from './views/TableExplorerView.vue'
import SqlEditorView from './views/SqlEditorView.vue'

const currentViewComponent = computed(() => {
  switch (activeView.value) {
    case 'dashboard': return DashboardView
    case 'connections': return ConnectionManagerView
    case 'tables': return TableExplorerView
    case 'queries': return SqlEditorView
    default: return DashboardView
  }
})
</script>
