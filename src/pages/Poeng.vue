<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">Daglig konkurranse</span>
        <h1 class="display" style="font-size: clamp(40px, 6vw, 84px); margin: 12px 0 16px;">Poeng<em>tavle</em>.</h1>
        <p class="lede">Hvor flink er du egentlig sammenlignet med Maya? Resultater fra dagens og ukens spill — Connections, Wordle, More or Less, Spotle, TimeGuessr.</p>
      </div>
      <div class="page-header-meta">
        DAG 142 · UKE 18<br/>
        18 / 23 SPILT I DAG<br/>
        OPPDATERES AUTOMATISK
      </div>
    </div>

    <div class="lb-tabs">
      <button
        v-for="(tab, i) in tabs"
        :key="tab"
        :class="['lb-tab', { active: activeTab === i }]"
        @click="activeTab = i"
      >
        {{ tab }}<span v-if="i === 0" class="badge">142</span>
      </button>
    </div>

    <div class="lb-grid">
      <div v-if="leaderboard.length > 0" class="game-card" style="grid-column: 1 / -1;">
        <div class="game-head">
          <span class="game-name">Leaderboard</span>
          <span class="game-emoji">🏆</span>
        </div>
        <div class="game-leaders" style="display: flex; flex-direction: column; gap: 12px;">
          <div v-for="(entry, idx) in leaderboard.slice(0, 10)" :key="entry.memberName" class="game-leader">
            <span class="gl-rank">{{ idx + 1 }}</span>
            <span class="gl-name">{{ entry.memberName }}</span>
            <span class="gl-score">{{ entry.score }} pts</span>
          </div>
        </div>
      </div>
      <div v-else class="game-card" style="grid-column: 1 / -1; padding: 40px; text-align: center; opacity: 0.6;">
        Laster leaderboard...
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { scoreApi } from '../services/api'

const tabs = ['I dag', 'Denne uken', 'Denne måneden', 'All-time']
const activeTab = ref(0)
const leaderboard = ref([])

const periodMap = {
  0: 'daily',
  1: 'weekly',
  2: 'monthly',
  3: 'alltime'
}

const loadLeaderboard = async () => {
  try {
    const period = periodMap[activeTab.value]
    leaderboard.value = await scoreApi.getLeaderboard(period)
  } catch (error) {
    console.error('Failed to load leaderboard:', error)
  }
}

onMounted(() => {
  loadLeaderboard()
})

watch(() => activeTab.value, () => {
  loadLeaderboard()
})
</script>
