import { ref } from 'vue'

export type ViewName = 'dashboard' | 'connections' | 'tables' | 'queries'

export const activeView = ref<ViewName>('queries')
export const isSidebarCollapsed = ref(false)
