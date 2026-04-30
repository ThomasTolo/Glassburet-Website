<template>
  <div class="um-backdrop" @click.self="$emit('close')">
    <div class="um-panel">
      <div class="um-header">
        <span class="um-title">Brukere</span>
        <button class="um-close" type="button" @click="$emit('close')">✕</button>
      </div>

      <div v-if="loading" class="um-state">Laster...</div>
      <div v-else-if="error" class="um-state um-error">{{ error }}</div>
      <ul v-else class="um-list">
        <li v-for="member in members" :key="member.name" class="um-row">
          <span class="um-name">{{ member.name }}</span>
          <span class="um-role-pill" :class="roleClass(member.role)">{{ roleLabel(member.role) }}</span>
          <button
            v-if="member.role === 'ROLE_MEMBER'"
            class="um-action promote"
            type="button"
            @click="setRole(member, 'ROLE_ADMIN')"
          >Forfrem</button>
          <button
            v-else-if="member.role === 'ROLE_ADMIN'"
            class="um-action demote"
            type="button"
            @click="setRole(member, 'ROLE_MEMBER')"
          >Degrader</button>
          <span v-else class="um-action-placeholder"></span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { memberApi } from '../services/api'
import { subscribeToUpdates } from '../services/realtime'

defineEmits(['close'])

const members = ref([])
const loading = ref(true)
const error = ref(null)

async function fetchMembers() {
  loading.value = true
  error.value = null
  try {
    members.value = await memberApi.getAll()
  } catch {
    error.value = 'Kunne ikke laste brukere.'
  } finally {
    loading.value = false
  }
}

async function setRole(member, role) {
  try {
    await memberApi.updateRole(member.name, role)
    member.role = role
  } catch {
    error.value = 'Kunne ikke oppdatere rolle.'
  }
}

function roleLabel(role) {
  if (role === 'ROLE_OWNER') return 'Owner'
  if (role === 'ROLE_ADMIN') return 'Admin'
  return 'Medlem'
}

function roleClass(role) {
  if (role === 'ROLE_OWNER') return 'owner'
  if (role === 'ROLE_ADMIN') return 'admin'
  return 'member'
}

let unsubscribeMembers = null

onMounted(() => {
  fetchMembers()
  unsubscribeMembers = subscribeToUpdates('members', fetchMembers)
})

onUnmounted(() => {
  if (unsubscribeMembers) unsubscribeMembers()
})
</script>

<style scoped>
.um-backdrop {
  position: fixed;
  inset: 0;
  z-index: 200;
  background: rgba(0, 0, 0, 0.25);
}

.um-panel {
  position: absolute;
  top: 72px;
  right: 24px;
  width: 340px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border: 1px solid var(--glass-line);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.um-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--glass-line);
}

.um-title {
  font-family: var(--mono);
  font-size: 11px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--ink);
}

.um-close {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 12px;
  color: var(--ink-soft);
  padding: 2px 4px;
}

.um-state {
  padding: 20px 16px;
  font-family: var(--mono);
  font-size: 12px;
  color: var(--ink-soft);
}

.um-error {
  color: #b00;
}

.um-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.um-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--glass-line);
}

.um-row:last-child {
  border-bottom: none;
}

.um-name {
  flex: 1;
  font-family: var(--mono);
  font-size: 12px;
  color: var(--ink);
}

.um-role-pill {
  font-family: var(--mono);
  font-size: 10px;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  padding: 3px 8px;
  border-radius: 999px;
  border: 1px solid var(--glass-line);
}

.um-role-pill.owner {
  color: #7c3aed;
  border-color: rgba(124, 58, 237, 0.25);
}

.um-role-pill.admin {
  color: var(--accent);
  border-color: rgba(13, 59, 46, 0.18);
}

.um-role-pill.member {
  color: var(--ink-soft);
}

.um-action {
  font-family: var(--mono);
  font-size: 10px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid;
  cursor: pointer;
  background: none;
  transition: all 0.15s ease;
  white-space: nowrap;
}

.um-action.promote {
  color: var(--accent);
  border-color: rgba(13, 59, 46, 0.3);
}

.um-action.promote:hover {
  background: var(--accent);
  color: var(--bg);
}

.um-action.demote {
  color: var(--ink-soft);
  border-color: rgba(0, 0, 0, 0.15);
}

.um-action.demote:hover {
  background: var(--ink);
  color: var(--bg);
}

.um-action-placeholder {
  width: 60px;
}
</style>
