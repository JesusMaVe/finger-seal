<template>
  <div class="flex-1 flex flex-col h-full bg-surface-container-lowest overflow-y-auto px-edge-margin pt-edge-margin pb-24 items-center">
    <div class="w-full max-w-4xl flex flex-col gap-lg">
      <div class="flex justify-between items-end">
        <div>
          <h1 class="font-headline-lg text-headline-lg text-on-surface">Connection Manager</h1>
          <p class="font-body-md text-on-surface-variant text-body-md mt-xs">Configure access to your remote or local data sources.</p>
        </div>
        <div class="flex gap-sm">
          <span class="text-body-sm font-code-sm bg-surface-container px-sm py-1 rounded border border-outline-variant text-on-surface-variant">v2.4.1-stable</span>
        </div>
      </div>

      <!-- Main Form Card -->
      <section class="w-full bg-surface-container-lowest border border-outline-variant rounded-xl shadow-lg shadow-black/5 overflow-hidden flex flex-col md:flex-row">
        
        <!-- Left Sidebar Type List -->
        <div class="w-full md:w-1/3 bg-surface-container-low border-r border-outline-variant p-lg flex flex-col gap-md shrink-0">
          <h3 class="font-label-caps text-label-caps text-on-surface-variant">DATABASE TYPE</h3>
          
          <div class="space-y-sm">
            <button class="w-full text-left flex items-start gap-md p-md rounded-lg border-2 border-primary bg-primary-container/[0.05] transition-all group">
              <div class="w-10 h-10 shrink-0 rounded bg-surface border border-outline-variant/30 flex items-center justify-center p-2 mt-0.5">
                <img src="https://lh3.googleusercontent.com/aida-public/AB6AXuADlqvHo3XphVkpQ-yR-1CrxlH8MfA_OJVyMv_9XAqY60X9xGxHk8y9JlkO1BFvvKRwUagkQXfZ_s4mZmkMf4FgxKLj3xUIzdoV-OECJx4SUOqu_OHkX4-kungQzZcyBOPB2OeJ3EfJUxHhLUiYSuDmM1Q_xzgmTsw5wc6nJv2oO5u1PeMhtTzbkSVt8WLC6b96MY-KoDfT2HE1-RZIyYbjlYTsZF0qOcQSJYrrKQQLf9ItIHYqlQRwAuQrfRTNbQ6I-tquThJ8-lM" class="w-full h-full object-contain filter grayscale invert opacity-80" />
              </div>
              <div>
                <p class="font-bold text-on-surface">PostgreSQL</p>
                <p class="text-body-sm text-on-surface-variant">Recommended</p>
              </div>
            </button>
            <button class="w-full text-left flex items-start gap-md p-md rounded-lg border border-outline-variant bg-surface transition-all group hover:border-outline">
              <div class="w-10 h-10 shrink-0 rounded bg-surface-variant flex items-center justify-center p-2 mt-0.5 border border-transparent group-hover:border-outline-variant">
                <img src="https://lh3.googleusercontent.com/aida-public/AB6AXuAesD-m8yRsMM5sLGsmE4DZ9LByWp_a3El7TNGXt87zT_LhD05wMhLVC_oprGUHn25fg4lfLLjDxEXCcN5hxMnnP7rMPH9o1NVNeGY5xu3a7DSgXDzG9zb4AExbcG565IxB9PSmKcaEs9FxNTkxjh0n5hETMj05F1hw8U0stp8YqrsVHbEUxefWuT29dAnyAoj5wl7LbMtmd5OSV8-ssGApxWu8tBnzWDVojgRkWAg1PBdP7SSVLUnC1euKn3ha8RxUUkfTFCnVkQs" class="w-full h-full object-contain filter grayscale invert opacity-60 group-hover:opacity-100" />
              </div>
              <div>
                <p class="font-bold text-on-surface">MySQL</p>
                <p class="text-body-sm text-on-surface-variant">Standard</p>
              </div>
            </button>
            <button class="w-full text-left flex items-start gap-md p-md rounded-lg border border-outline-variant bg-surface transition-all group hover:border-outline">
               <div class="w-10 h-10 shrink-0 rounded bg-surface-variant flex items-center justify-center p-2 mt-0.5 border border-transparent group-hover:border-outline-variant">
                <img src="https://lh3.googleusercontent.com/aida-public/AB6AXuDJb7Ls3pX9WlGKjL4nEzrXoFzwkZ5dCmCI1bUGkKVDKI80jFPK4pCj3Aeq8SMxqHuPbk7iM0KS0Mxm3fOZi14acnRFBjj39tuv8Zj3HLRhe1KMGZcdVA-aUwdc344XAXJzOk7ovisJg2PAwMjb0jPghEcdgFqV3j3K6t10HWtkyQ4WDRY0HBJ9lh3jY0CM-PHawCi3jVKwvMOhSoXabJup0KcLMvK51h8nrVQVFVJyHDYHCLpSH_wvxPpx72Vx_qd38X-lopWXuYU" class="w-full h-full object-contain filter grayscale invert opacity-60 group-hover:opacity-100" />
              </div>
              <div>
                <p class="font-bold text-on-surface">SQLite</p>
                <p class="text-body-sm text-on-surface-variant">Embedded</p>
              </div>
            </button>
            <button class="w-full text-left flex items-start gap-md p-md rounded-lg border border-outline-variant bg-surface transition-all group hover:border-outline">
               <div class="w-10 h-10 shrink-0 rounded bg-surface-variant flex items-center justify-center p-2 mt-0.5 border border-transparent group-hover:border-outline-variant">
                  <span class="material-symbols-outlined text-outline">more_horiz</span>
              </div>
              <div>
                <p class="font-bold text-on-surface">Others</p>
                <p class="text-body-sm text-on-surface-variant">Redis, MongoDB...</p>
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
            <input class="w-full bg-surface border border-outline-variant rounded px-md py-sm focus:border-primary focus:ring-1 focus:ring-primary text-on-surface transition-all outline-none font-code-md" type="text" value="Production Core DB"/>
          </div>

          <div class="grid grid-cols-2 gap-md">
            <div class="col-span-2 md:col-span-1 border-r border-transparent md:border-outline-variant/30 md:pr-4">
              <label class="block text-label-caps text-on-surface-variant mb-xs">HOST</label>
              <div class="relative">
                <input class="w-full bg-surface border border-outline-variant rounded px-md py-sm pl-10 focus:border-primary focus:ring-1 focus:ring-primary text-on-surface transition-all outline-none font-code-md" type="text" value="db.production.internal"/>
                <span class="material-symbols-outlined absolute left-sm top-1/2 -translate-y-1/2 text-outline text-[18px]">dns</span>
              </div>
            </div>
            <div class="col-span-2 md:col-span-1">
              <label class="block text-label-caps text-on-surface-variant mb-xs">PORT</label>
              <input class="w-full bg-surface border border-outline-variant rounded px-md py-sm focus:border-primary focus:ring-1 focus:ring-primary text-on-surface transition-all outline-none font-code-md" type="number" value="5432"/>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-md">
            <div class="col-span-2 md:col-span-1 border-r border-transparent md:border-outline-variant/30 md:pr-4">
              <label class="block text-label-caps text-on-surface-variant mb-xs">USER</label>
              <div class="relative">
                <input class="w-full bg-surface border border-outline-variant rounded px-md py-sm pl-10 focus:border-primary focus:ring-1 focus:ring-primary text-on-surface transition-all outline-none font-code-md" type="text" value="admin_readonly"/>
                <span class="material-symbols-outlined absolute left-sm top-1/2 -translate-y-1/2 text-outline text-[18px]">person</span>
              </div>
            </div>
            <div class="col-span-2 md:col-span-1">
              <label class="block text-label-caps text-on-surface-variant mb-xs">PASSWORD</label>
              <div class="relative">
                <input class="w-full bg-surface border border-outline-variant rounded px-md py-sm pl-10 pr-10 focus:border-primary focus:ring-1 focus:ring-primary text-on-surface transition-all outline-none font-code-md" type="password" value="••••••••••••"/>
                <span class="material-symbols-outlined absolute left-sm top-1/2 -translate-y-1/2 text-outline text-[18px]">lock</span>
                <button class="material-symbols-outlined absolute right-sm top-1/2 -translate-y-1/2 text-outline hover:text-on-surface transition-colors text-[18px] cursor-pointer">visibility</button>
              </div>
            </div>
          </div>

          <div class="col-span-2 mb-md hover:translate-y-0.5 transition-transform duration-300">
            <label class="block text-label-caps text-on-surface-variant mb-xs">DATABASE</label>
            <div class="relative">
              <input class="w-full bg-surface border border-outline-variant rounded px-md py-sm pl-10 focus:border-primary focus:ring-1 focus:ring-primary text-on-surface transition-all outline-none font-code-md" type="text" value="analytics_v3"/>
              <span class="material-symbols-outlined absolute left-sm top-1/2 -translate-y-1/2 text-outline text-[18px]">folder_data</span>
            </div>
          </div>

          <div class="mt-auto pt-lg border-t border-outline-variant flex items-center justify-between">
             <button class="flex items-center gap-sm px-md py-sm border border-outline-variant rounded font-medium text-on-surface font-body-md hover:bg-surface transition-all active:scale-[0.98]">
              <span class="material-symbols-outlined text-outline">network_check</span>
              Test Connection
            </button>
            <div class="flex gap-md">
               <button class="text-on-surface-variant hover:text-on-surface font-bold text-body-md transition-colors px-md">Cancel</button>
               <button class="bg-primary text-on-primary px-lg py-sm rounded border border-transparent font-bold text-body-md hover:opacity-90 active:scale-[0.98] transition-all shadow-sm">Connect</button>
            </div>
          </div>
        </div>
      </section>

      <!-- Bottom Status Grid -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-md">
         <div class="bg-surface border border-outline-variant p-md rounded-lg flex flex-col justify-between">
           <div>
             <span class="material-symbols-outlined text-outline mb-xs">history</span>
             <h4 class="font-bold text-on-surface text-body-md">Recent Sessions</h4>
           </div>
           <p class="text-body-sm text-on-surface-variant mt-sm">Last connected to <span class="text-on-surface font-code-sm">analytics_v3</span> 2 hours ago.</p>
         </div>
         <div class="bg-surface border border-outline-variant p-md rounded-lg flex flex-col justify-between">
           <div>
             <span class="material-symbols-outlined text-outline mb-xs">security</span>
             <h4 class="font-bold text-on-surface text-body-md">Encryption</h4>
           </div>
           <p class="text-body-sm text-on-surface-variant mt-sm">SSL/TLS is enabled by default for all production hosts.</p>
         </div>
         <div class="bg-surface border border-outline-variant p-md rounded-lg flex flex-col justify-between">
           <div>
             <span class="material-symbols-outlined text-outline mb-xs">sync</span>
             <h4 class="font-bold text-on-surface text-body-md">Cloud Sync</h4>
           </div>
           <p class="text-body-sm text-on-surface-variant mt-sm">Connection settings are encrypted and synced to your profile.</p>
         </div>
      </div>
    </div>
  </div>
</template>
