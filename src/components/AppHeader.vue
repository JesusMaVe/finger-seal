<template>
  <header class="bg-surface-container flex justify-between items-center px-md w-full h-12 border-b border-outline-variant z-40 shrink-0">
    <div class="flex items-center gap-md h-full">
      <span class="font-headline-md text-headline-md font-bold text-primary">Finger Seal</span>
    </div>
    <div class="flex items-center gap-md">
      <nav class="hidden md:flex gap-md items-center">
        <a href="#" @click.prevent="activeView = 'queries'"
          class="font-body-md text-body-md transition-colors px-sm py-1 rounded"
          :class="activeView === 'queries' ? 'text-primary font-bold bg-primary-container/20' : 'text-on-surface-variant hover:text-primary'">Query</a>
        <a href="#" @click.prevent="activeView = 'tables'"
          class="font-body-md text-body-md transition-colors px-sm py-1 rounded"
          :class="activeView === 'tables' ? 'text-primary font-bold bg-primary-container/20' : 'text-on-surface-variant hover:text-primary'">Schema</a>
        <a href="#" @click.prevent="activeView = 'dashboard'"
          class="font-body-md text-body-md transition-colors px-sm py-1 rounded"
          :class="activeView === 'dashboard' ? 'text-primary font-bold bg-primary-container/20' : 'text-on-surface-variant hover:text-primary'">Logs</a>
      </nav>
    </div>
  </header>
</template>

<script setup lang="ts">
import { activeView } from '@/store/app'
</script>
