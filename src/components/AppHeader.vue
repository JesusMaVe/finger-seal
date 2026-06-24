<template>
  <header class="bg-surface-container flex justify-between items-center px-md w-full h-12 border-b border-outline-variant z-40 shrink-0">
    <div class="flex items-center gap-md h-full">
      <span class="font-headline-md text-headline-md font-bold text-primary">Finger Seal</span>
    </div>
    <div class="flex items-center gap-md">
      <nav class="hidden md:flex gap-md items-center">
        <a href="#" @click.prevent="activeView = 'queries'"
          class="font-body-md text-body-md btn-transition px-sm py-1 rounded"
          :class="activeView === 'queries' ? 'bg-secondary-container text-on-secondary-container font-semibold' : 'text-on-surface-variant hover:text-on-surface'">Query</a>
        <a href="#" @click.prevent="activeView = 'tables'"
          class="font-body-md text-body-md btn-transition px-sm py-1 rounded"
          :class="activeView === 'tables' ? 'bg-secondary-container text-on-secondary-container font-semibold' : 'text-on-surface-variant hover:text-on-surface'">Schema</a>
        <a href="#" @click.prevent="activeView = 'dashboard'"
          class="font-body-md text-body-md btn-transition px-sm py-1 rounded"
          :class="activeView === 'dashboard' ? 'bg-secondary-container text-on-secondary-container font-semibold' : 'text-on-surface-variant hover:text-on-surface'">Logs</a>
      </nav>
    </div>
  </header>
</template>

<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useAppStore } from '@/store/app'
const { activeView } = storeToRefs(useAppStore())
</script>
