<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">Hall of fame · {{ liners.length }} stk</span>
        <h1 class="display" style="font-size: clamp(40px, 6vw, 84px); margin: 12px 0 16px;">Beste <em>one-liners</em>.</h1>
        <p class="lede">Setninger som har overlevd kontekst, tid og skam. Ord som har blitt egne kategorier her i Glassburet.</p>
      </div>
      <div class="page-header-meta">
        ALLE TIDERS BESTE<br/>
        VOTERT AV ROMMET<br/>
        OPPDATERT UKENTLIG
      </div>
    </div>

    <div class="liner-wall">
      <div v-for="(liner, idx) in liners" :key="liner.id" :class="['liner', `liner-${(idx % 7) + 1}`]" @click="incrementSaid(liner.id)">
        <span class="liner-num">№ {{ String(idx + 1).padStart(2, '0') }}</span>
        <p class="liner-text">"{{ liner.text }}"</p>
        <div class="liner-meta"><span>SAGT {{ liner.saidCount || 0 }} GANGER</span><span>— {{ liner.author }}</span></div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { linerApi } from '../services/api'

const liners = ref([])

const incrementSaid = async (id) => {
  try {
    const updated = await linerApi.incrementSaidCount(id)
    const liner = liners.value.find(l => l.id === id)
    if (liner) {
      liner.saidCount = updated.saidCount
    }
  } catch (error) {
    console.error('Failed to increment said count:', error)
  }
}

onMounted(async () => {
  try {
    liners.value = await linerApi.getAll()
  } catch (error) {
    console.error('Failed to load liners:', error)
  }
})
</script>
