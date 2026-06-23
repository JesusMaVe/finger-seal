<template>
  <aside class="h-full w-64 flex flex-col bg-surface-container-low border-r border-outline-variant py-md px-sm z-30 shrink-0">
    <div class="mb-lg px-xs">
      <div class="flex items-center gap-sm mb-md">
        <div class="w-8 h-8 rounded-sm bg-primary-container flex items-center justify-center">
          <span class="material-symbols-outlined text-on-primary-container" style="font-variation-settings: 'FILL' 1;">database</span>
        </div>
        <div>
          <h2 class="font-headline-md text-[14px] text-on-surface leading-tight">Localhost</h2>
          <p class="font-body-sm text-[11px] text-on-surface-variant">PostgreSQL 15.2</p>
        </div>
      </div>
      <button class="w-full bg-primary text-on-primary font-body-md text-body-md py-sm rounded-sm hover:opacity-90 transition-all flex items-center justify-center gap-xs">
         <span class="material-symbols-outlined text-[18px]">add</span>
         New Connection
      </button>
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
      
      <!-- Sub-list of tables if Tables is active -->
      <div v-if="activeView === 'tables'" class="ml-8 flex flex-col gap-1 border-l border-outline-variant/30 pl-xs mt-1 mb-2">
         <div class="p-xs text-body-sm rounded hover:bg-surface-variant cursor-pointer text-primary font-code-md">users</div>
         <div class="p-xs text-body-sm rounded hover:bg-surface-variant cursor-pointer text-on-surface-variant font-code-md">orders</div>
         <div class="p-xs text-body-sm rounded hover:bg-surface-variant cursor-pointer text-on-surface-variant font-code-md">products</div>
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
import { activeView } from '@/store/app'
</script>
