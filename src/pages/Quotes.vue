<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">Arkiv · {{ quotes.length }} sitater</span>
        <h1 class="display" style="font-size: clamp(40px, 6vw, 84px); margin: 12px 0 16px;">Dagens <em>quote</em>.</h1>
        <p class="lede">Det folk har sagt høyt, ofte ved et uhell. Sitater hentet ut av kontekst og bevart for ettertiden.</p>
      </div>
      <div class="page-header-meta">
        ONSDAG 29 APR 2026<br/>
        DAG 142 / SEMESTERET<br/>
        {{ quotes.length > 0 ? '3 NYE I DAG' : 'LASTER...' }}
      </div>
    </div>

    <div class="quote-grid">
      <article v-for="(quote, idx) in quotes" :key="quote.id" :class="['quote-card', idx === 0 ? 'featured' : '', idx === 1 ? 'tall' : '', idx > 2 && (idx - 3) % 3 === 0 ? 'wide' : '']">
        <span v-if="idx === 0" class="quote-day-tag">⭐ DAGENS</span>
        <p class="quote-text" :style="{ 'font-size': idx === 0 ? '44px' : 'inherit' }">{{ quote.text }}</p>
        <div class="quote-foot"><span>— {{ quote.author }}</span><span v-if="quote.createdAt">{{ formatDate(quote.createdAt) }}</span></div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { quoteApi } from '../services/api'
import staticQuotes from '../data/quotes.json'

const quotes = ref([])

const formatDate = (dateString) => {
  const date = new Date(dateString)
  const days = ['SØN', 'MAN', 'TIR', 'ONS', 'TOR', 'FRE', 'LØR']
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${days[date.getDay()]} · ${hours}:${minutes}`
}

onMounted(async () => {
  try {
    const apiQuotes = await quoteApi.getAll()
    quotes.value = apiQuotes.length > 0 ? apiQuotes : staticQuotes
  } catch (error) {
    quotes.value = staticQuotes
  }
})
</script>
