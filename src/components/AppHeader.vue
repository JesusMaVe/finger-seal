<template>
  <header class="bg-surface-container flex justify-between items-center px-md w-full h-12 border-b border-outline-variant z-40 shrink-0">
    <div class="flex items-center gap-md h-full">
      <span class="font-headline-md text-headline-md font-bold text-primary">Finger Seal</span>
      <nav class="hidden md:flex gap-md h-full ml-lg items-center">
        <a 
          href="#" 
          class="font-body-md text-body-md transition-colors h-full flex items-center"
          :class="['text-on-surface font-bold border-b-2 border-primary']"
        >Query</a>
        <a href="#" class="text-on-surface-variant font-medium hover:text-primary transition-colors font-body-md text-body-md h-full flex items-center">Schema</a>
        <a href="#" class="text-on-surface-variant font-medium hover:text-primary transition-colors font-body-md text-body-md h-full flex items-center">Users</a>
        <a href="#" class="text-on-surface-variant font-medium hover:text-primary transition-colors font-body-md text-body-md h-full flex items-center">Logs</a>
      </nav>
    </div>
    <div class="flex items-center gap-md">
      <div class="relative hidden sm:block">
        <input 
          class="bg-surface-container-lowest border border-outline-variant rounded-sm px-sm py-1 text-body-sm font-body-sm w-64 focus:outline-none focus:ring-1 focus:ring-primary placeholder-on-surface-variant/50" 
          placeholder="Search resources..." 
          type="text"
        />
        <span class="material-symbols-outlined absolute right-2 top-1.5 text-[16px] text-outline">search</span>
      </div>
      <div class="flex gap-sm">
        <button class="material-symbols-outlined text-on-surface-variant hover:text-primary cursor-pointer">settings</button>
        <button class="material-symbols-outlined text-on-surface-variant hover:text-primary cursor-pointer">notifications</button>
        <button class="material-symbols-outlined text-on-surface-variant hover:text-primary cursor-pointer">account_circle</button>
      </div>
    </div>
  </header>
</template>
