<template>
  <nav class="topnav">
    <div class="topnav-inner">
      <RouterLink to="/" class="brand">
        <div class="brand-mark"></div>
        <span>Glassburet <em>/ et lite hjørne</em></span>
      </RouterLink>
      <div class="nav-links">
        <RouterLink class="nav-link" to="/">Hjem</RouterLink>
        <RouterLink class="nav-link" to="/quotes">Dagens Quote</RouterLink>
        <RouterLink class="nav-link" to="/liners">Beste One-Liners</RouterLink>
        <RouterLink class="nav-link" to="/galleri">Galleri</RouterLink>
        <RouterLink class="nav-link" to="/aktiviteter">Aktiviteter</RouterLink>
        <RouterLink class="nav-link" to="/spill">Spill</RouterLink>
        <RouterLink class="nav-link" to="/poeng">Poengtavle</RouterLink>
      </div>
      <div class="nav-aside">
        <div class="nav-meta">
          <span class="live-dot"></span>
          <span>{{ clockText }}</span>
          <span>·</span>
          <span>UiB MATNAT × ECHO</span>
        </div>
        <div class="nav-actions">
          <span v-if="isAuthenticated" class="role-pill" :class="isAdmin ? 'admin' : 'member'">
            {{ isAdmin ? 'Admin' : 'Medlem' }} · {{ displayName || 'Logget inn' }}
          </span>
          <RouterLink v-if="!isAuthenticated" class="auth-link" to="/login">Logg inn</RouterLink>
          <button v-else class="auth-link" type="button" @click="logout">Logg ut</button>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { clearAuthToken, displayName, isAdmin, isAuthenticated } from '../services/authState'

const days = ['SØNDAG', 'MANDAG', 'TIRSDAG', 'ONSDAG', 'TORSDAG', 'FREDAG', 'LØRDAG']
const clockText = ref('')
const router = useRouter()

function updateClock() {
  const now = new Date()
  const h = String(now.getHours()).padStart(2, '0')
  const m = String(now.getMinutes()).padStart(2, '0')
  clockText.value = `${days[now.getDay()]} · ${h}:${m}`
}

let interval
onMounted(() => {
  updateClock()
  interval = setInterval(updateClock, 30000)
})
onUnmounted(() => clearInterval(interval))

function logout() {
  clearAuthToken()
  router.push('/')
}
</script>

<style scoped>
.nav-aside {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.nav-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.role-pill,
.auth-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border-radius: 999px;
  padding: 7px 12px;
  font-family: var(--mono);
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  border: 1px solid var(--glass-line);
  background: rgba(255, 255, 255, 0.45);
  transition: all 0.2s ease;
}

.role-pill.member {
  color: var(--ink-soft);
}

.role-pill.admin {
  color: var(--accent);
  border-color: rgba(13, 59, 46, 0.18);
}

.auth-link:hover {
  background: var(--ink);
  color: var(--bg);
  border-color: var(--ink);
}

@media (max-width: 1100px) {
  .nav-aside {
    align-items: flex-start;
  }
}
</style>
