<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">Daglig konkurranse</span>
        <h1 class="display" style="font-size: clamp(40px, 6vw, 84px); margin: 12px 0 16px;">Poeng<em>tavle</em>.</h1>
        <p class="lede">Hvor flink er du egentlig sammenlignet med de andre? Resultater fra Tankeburet og Ordburet.</p>
      </div>
      <div class="page-header-meta">
        {{ dateLabel }}<br/>
        {{ semesterLabel }}<br/>
        OPPDATERES AUTOMATISK
      </div>
    </div>

    <!-- Manual score submission removed: scores come from gameplay only -->

    <div class="lb-tabs">
      <button
        v-for="(tab, i) in tabs"
        :key="tab"
        :class="['lb-tab', { active: activeTab === i }]"
        @click="activeTab = i; loadLeaderboard()"
      >
        {{ tab }}
      </button>
    </div>

    <!-- Game filter -->
    <div class="game-filter">
      <button
        v-for="g in gameFilters"
        :key="g.value"
        :class="['filter-btn', { active: activeGame === g.value }]"
        @click="activeGame = g.value; loadLeaderboard()"
      >{{ g.label }}</button>
    </div>

    <div class="lb-grid">
      <div v-if="leaderboard.length > 0" class="game-card" style="grid-column: 1 / -1;">
        <div class="game-head">
          <span class="game-name">{{ activeGameLabel }}</span>
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
        Ingen poeng registrert for denne perioden.
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { scoreApi } from '../services/api'
import { useLiveDateInfo } from '../composables/useLiveDateInfo'
import { subscribeToUpdates } from '../services/realtime'

const tabs = ['I dag', 'Denne uken', 'Denne måneden', 'All-time']
const activeTab = ref(0)
const leaderboard = ref([])

const gameFilters = [
  { label: 'Alle spill', value: '' },
  { label: 'Tankeburet', value: 'CONNECTIONS' },
  { label: 'Ordburet', value: 'WORDLE' },
]
const activeGame = ref('')
const activeGameLabel = computed(() => gameFilters.find(g => g.value === activeGame.value)?.label ?? 'Leaderboard')
const { dateLabel, semesterLabel } = useLiveDateInfo({ intervalMs: 60000 })

// Manual score submission removed — scores should only come from games

const periodMap = { 0: 'daily', 1: 'weekly', 2: 'monthly', 3: 'alltime' }

const loadLeaderboard = async () => {
  try {
    const period = periodMap[activeTab.value]
    const lb = await scoreApi.getLeaderboard(period, activeGame.value)
    // Use API result directly; if empty array returned, show empty state
    leaderboard.value = Array.isArray(lb) ? lb : []
  } catch {
    leaderboard.value = []
  }
}

// submitScore removed — scores are handled by game backend

let unsubscribeScores = null

onMounted(() => {
  loadLeaderboard()
  unsubscribeScores = subscribeToUpdates('scores', loadLeaderboard)
})

onUnmounted(() => {
  if (unsubscribeScores) unsubscribeScores()
})

watch(() => activeTab.value, loadLeaderboard)
</script>

<style scoped>
.game-filter {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
}
.filter-btn {
  padding: 8px 16px;
  font-family: var(--mono);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  border: 1px solid var(--line-soft);
  border-radius: 999px;
  color: var(--ink-mute);
  transition: all 0.2s;
}
.filter-btn:hover { border-color: var(--ink); color: var(--ink); }
.filter-btn.active { background: var(--ink); color: var(--bg); border-color: var(--ink); }
.admin-bar {
  margin-bottom: 32px;
  padding: 20px;
  background: var(--paper);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
}
.admin-form { margin-top: 16px; display: flex; flex-direction: column; gap: 12px; }
.form-row { display: flex; gap: 8px; flex-wrap: wrap; }
.form-row .admin-input { flex: 1; min-width: 120px; }
.admin-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--line-soft);
  background: var(--bg);
  font-family: var(--sans);
  font-size: 14px;
  border-radius: var(--radius);
  color: var(--ink);
}
.admin-input:focus { outline: none; border-color: var(--accent); }
.admin-actions { display: flex; gap: 10px; }
.btn-primary {
  padding: 10px 20px;
  background: var(--accent);
  color: #f4f1ea;
  border-radius: 999px;
  font-family: var(--mono);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}
.btn-primary:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-secondary {
  padding: 10px 20px;
  border: 1px solid var(--line-soft);
  border-radius: 999px;
  font-family: var(--mono);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--ink-mute);
}
.creator-toggle {
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: var(--mono);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--ink-mute);
}
.creator-toggle:hover { color: var(--ink); }
.creator-toggle-icon {
  width: 24px; height: 24px;
  border: 1px solid var(--line-soft);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px;
}
.score-saved {
  font-family: var(--mono);
  font-size: 12px;
  color: var(--accent);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}
.lb-tabs {
  display: flex;
  border-bottom: 1px solid var(--line);
  margin-bottom: 24px;
}
.lb-tab {
  padding: 12px 24px;
  font-family: var(--mono);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--ink-mute);
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}
.lb-tab:hover { color: var(--ink); }
.lb-tab.active { color: var(--ink); border-bottom-color: var(--accent); }
.badge {
  background: var(--accent);
  color: #f4f1ea;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 999px;
}
</style>
