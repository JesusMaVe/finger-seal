<template>
  <div class="border border-outline-variant rounded-lg p-md bg-surface-container-low">
    <div class="flex items-center justify-between mb-md">
      <div class="flex items-center gap-xs">
        <span class="material-symbols-outlined text-[18px] text-on-surface-variant">key</span>
        <span class="font-label-caps text-label-caps text-on-surface-variant uppercase">SSH Tunnel</span>
      </div>
      <label class="flex items-center gap-xs cursor-pointer select-none">
        <span class="text-body-sm text-on-surface-variant">Enabled</span>
        <input type="checkbox" v-model="enabled"
          class="w-4 h-4 rounded border-outline-variant text-primary focus:ring-primary" />
      </label>
    </div>

    <template v-if="enabled">
      <div class="grid grid-cols-2 gap-md">
        <div>
          <label class="block text-label-caps text-on-surface-variant mb-xs text-[11px]">SSH Host</label>
          <input type="text" v-model="sshHost"
            class="w-full bg-surface border border-outline-variant rounded px-md py-sm text-on-surface font-code-md outline-none focus:border-primary" />
        </div>
        <div>
          <label class="block text-label-caps text-on-surface-variant mb-xs text-[11px]">SSH Port</label>
          <input type="number" v-model.number="sshPort"
            class="w-full bg-surface border border-outline-variant rounded px-md py-sm text-on-surface font-code-md outline-none focus:border-primary" />
        </div>
        <div>
          <label class="block text-label-caps text-on-surface-variant mb-xs text-[11px]">SSH User</label>
          <input type="text" v-model="sshUser"
            class="w-full bg-surface border border-outline-variant rounded px-md py-sm text-on-surface font-code-md outline-none focus:border-primary" />
        </div>
        <div>
          <label class="block text-label-caps text-on-surface-variant mb-xs text-[11px]">Password / Key Path</label>
          <input type="text" v-model="sshPassword" placeholder="password or /path/to/key"
            class="w-full bg-surface border border-outline-variant rounded px-md py-sm text-on-surface font-code-md outline-none focus:border-primary" />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  modelValue: {
    useSshTunnel: boolean
    sshHost: string
    sshPort: number
    sshUser: string
    sshPassword: string
    sshPrivateKeyPath: string
  }
}>()

const emit = defineEmits<{
  'update:modelValue': [value: typeof props.modelValue]
}>()

const enabled = computed({
  get: () => props.modelValue.useSshTunnel,
  set: (v) => emit('update:modelValue', { ...props.modelValue, useSshTunnel: v })
})
const sshHost = computed({
  get: () => props.modelValue.sshHost,
  set: (v) => emit('update:modelValue', { ...props.modelValue, sshHost: v })
})
const sshPort = computed({
  get: () => props.modelValue.sshPort,
  set: (v) => emit('update:modelValue', { ...props.modelValue, sshPort: v })
})
const sshUser = computed({
  get: () => props.modelValue.sshUser,
  set: (v) => emit('update:modelValue', { ...props.modelValue, sshUser: v })
})
const sshPassword = computed({
  get: () => props.modelValue.sshPassword,
  set: (v) => emit('update:modelValue', { ...props.modelValue, sshPassword: v })
})
</script>
