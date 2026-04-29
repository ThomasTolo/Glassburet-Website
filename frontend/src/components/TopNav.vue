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
        <RouterLink class="nav-link" to="/poeng">Poengtavle</RouterLink>
      </div>
      <div class="nav-meta">
        <span class="live-dot"></span>
        <span>{{ clockText }}</span>
        <span>·</span>
        <span>UiB MATNAT × ECHO</span>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const days = ['SØNDAG', 'MANDAG', 'TIRSDAG', 'ONSDAG', 'TORSDAG', 'FREDAG', 'LØRDAG']
const clockText = ref('')

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
</script>
