<template>
  <section>

    <div class="hero-grid">
      <div class="hero-left">
        <span class="eyebrow">Utgave 142 · Vår 2026</span>
        <h1 class="display">Velkommen<br/>til <em>Glassburet</em>.</h1>
        <p class="lede">Et kontor med altfor mange vinduer, altfor mange kaffekopper, og helt klart altfor mange one-linere. Her samler vi det vi sier, det vi gjør, og det vi planlegger sammen.</p>
        <div class="hero-meta">
          <div><strong>{{ stats?.memberCount || '-' }}</strong>aktive medlemmer</div>
          <div><strong>{{ stats?.quoteCount || '-' }}</strong>siterte quotes</div>
          <div><strong>{{ stats?.activitiesThisWeek || '-' }}</strong>aktiviteter denne uken</div>
          <div><strong>∞</strong>kaffekopper</div>
        </div>
      </div>

      <div class="hero-card">
        <span class="eyebrow">Dagens quote</span>
        <p class="quote-of-day">{{ featuredQuote?.text || 'Laster...' }}</p>
        <div class="quote-author" v-if="featuredQuote">— {{ featuredQuote.author }}</div>
      </div>
    </div>

    <div class="dash">

      <RouterLink to="/liners" class="card card-7 card-dark">
        <div class="card-head">
          <span class="eyebrow">Pinnet på veggen</span>
          <div class="card-arrow">→</div>
        </div>
        <p class="liner-pull">"Helt <span style="background: linear-gradient(transparent 60%, rgba(212,167,44,0.55) 60%); padding: 0 6px;">film</span>" — sa han, og så kom ulvene.</p>
        <div style="display:flex; justify-content:space-between; align-items:center; padding-top: 16px; border-top: 1px solid rgba(255,255,255,0.12); font-family: var(--mono); font-size: 11px; opacity: 0.7;">
          <span>72 ONE-LINERS I ARKIVET</span>
          <span>OPPDATERT I DAG</span>
        </div>
      </RouterLink>

      <RouterLink to="/poeng" class="card card-5 card-accent">
        <div class="card-head">
          <span class="eyebrow">Dagens leder</span>
          <div class="card-arrow">→</div>
        </div>
        <div class="stat-big"><em>{{ leaderboard[0]?.memberName || '-' }}</em></div>
        <div style="font-family: var(--mono); font-size: 12px; opacity: 0.75;">{{ leaderboard[0]?.score || '-' }} PTS</div>
        <div class="rank-mini" style="margin-top: 18px;">
          <div v-for="(entry, idx) in leaderboard.slice(1, 4)" :key="entry.memberName" class="rank-row">
            <span class="rank-num">{{ idx + 2 }}</span>
            <span class="rank-name">{{ entry.memberName }}</span>
            <span class="rank-score">{{ entry.score }}</span>
          </div>
        </div>
      </RouterLink>

      <RouterLink to="/aktiviteter" class="card card-8">
        <div class="card-head">
          <div>
            <span class="eyebrow">Kommer opp</span>
            <h3 style="margin-top: 12px; font-size: 26px;">Aktiviteter & ting vi planlegger</h3>
          </div>
          <div class="card-arrow">→</div>
        </div>
        <div class="announce-list">
          <div v-for="event in events.slice(0, 3)" :key="event.id" class="announce-item">
            <div class="announce-date"><strong>{{ formatDay(event.eventDate) }}</strong>{{ formatMonth(event.eventDate) }}</div>
            <div>
              <div class="announce-title">{{ event.title }}</div>
              <div class="announce-meta">{{ event.timeStart }} · {{ event.location }} · {{ event.attendees?.length || 0 }} påmeldte</div>
            </div>
            <span :class="['pill', getCategoryClass(event.category)]">{{ event.category }}</span>
          </div>
        </div>
      </RouterLink>

      <RouterLink to="/quotes" class="card card-4">
        <div class="card-head">
          <span class="eyebrow">Sitater</span>
          <div class="card-arrow">→</div>
        </div>
        <p style="font-family: var(--serif); font-style: italic; font-size: 22px; line-height: 1.25; margin-top: 8px; letter-spacing: -0.01em;">"{{ sidebarQuote?.text }}"</p>
        <div style="font-family: var(--mono); font-size: 11px; color: var(--ink-mute); margin-top: 16px; text-transform: uppercase; letter-spacing: 0.12em;">— {{ sidebarQuote?.author }}</div>
        <div style="margin-top: 24px; padding-top: 16px; border-top: 1px solid var(--line-soft); font-size: 12px; color: var(--ink-mute);">{{ staticQuotes.length }} quotes i arkivet</div>
      </RouterLink>

      <RouterLink to="/galleri" class="card card-12">
        <div class="card-head">
          <div>
            <span class="eyebrow">Galleriet</span>
            <h3 style="margin-top: 12px; font-size: 26px;">Bilder fra Glassburet & turer <em style="font-style: italic; color: var(--ink-mute); font-weight: 400; font-size: 18px;">/ 247 stk</em></h3>
          </div>
          <div class="card-arrow">→</div>
        </div>
        <div class="photo-strip">
          <div class="photo-thumb"></div>
          <div class="photo-thumb"></div>
          <div class="photo-thumb"></div>
          <div class="photo-thumb"></div>
          <div class="photo-thumb"></div>
        </div>
      </RouterLink>

    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { statsApi, quoteApi, eventApi, scoreApi } from '../services/api'
import staticQuotes from '../data/quotes.json'
import staticEvents from '../data/events.json'
import staticLeaderboard from '../data/leaderboard.json'

const stats = ref(null)
const featuredQuote = ref(null)
const sidebarQuote = staticQuotes[staticQuotes.length - 1]
const events = ref([])
const leaderboard = ref([])

const formatDay = (dateString) => new Date(dateString).getDate()
const formatMonth = (dateString) => {
  const months = ['JAN', 'FEB', 'MAR', 'APR', 'MAI', 'JUN', 'JUL', 'AUG', 'SEP', 'OKT', 'NOV', 'DES']
  return months[new Date(dateString).getMonth()]
}
const getCategoryClass = (category) => {
  if (category === 'SOSIALT') return 'pill-accent'
  if (category === 'TUR') return 'pill-soft'
  return ''
}

onMounted(async () => {
  try {
    stats.value = await statsApi.getStats()
  } catch (error) {
    console.error('Failed to load stats:', error)
  }

  try {
    featuredQuote.value = await quoteApi.getFeatured()
  } catch (error) {
    featuredQuote.value = staticQuotes.find(q => q.featured) || staticQuotes[0]
  }

  try {
    const api = await eventApi.getUpcoming()
    events.value = api.length > 0 ? api : staticEvents
  } catch (error) {
    events.value = staticEvents
  }

  try {
    const lb = await scoreApi.getLeaderboard('weekly')
    leaderboard.value = lb.length > 0 ? lb : staticLeaderboard
  } catch (error) {
    leaderboard.value = staticLeaderboard
  }
})
</script>
