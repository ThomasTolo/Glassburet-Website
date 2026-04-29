<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">Annonser & planlegging</span>
        <h1 class="display" style="font-size: clamp(40px, 6vw, 84px); margin: 12px 0 16px;">Aktiviteter<em>.</em></h1>
        <p class="lede">Det vi planlegger, det vi melder oss på, og ting Echo eller noen i Glassburet vil at vi skal være med på.</p>
      </div>
      <div class="page-header-meta">
        {{ events.length }} KOMMENDE<br/>
        6 DENNE UKEN<br/>
        MELD INTERESSE →
      </div>
    </div>

    <div class="announce-grid">
      <div class="event-list">
        <div v-for="(event, idx) in events" :key="event.id" class="event-row">
          <div class="event-date-block">
            <div class="event-day">{{ formatDay(event.eventDate) }}</div>
            <div class="event-month">{{ formatMonth(event.eventDate) }}</div>
            <div class="event-time">{{ event.timeStart }} → {{ event.timeEnd }}</div>
          </div>
          <div class="event-body">
            <h3>{{ event.title }}</h3>
            <p class="event-desc">{{ event.description }}</p>
            <div class="event-tags">
              <span class="pill" :class="getCategoryClass(event.category)">{{ event.category }}</span>
            </div>
          </div>
          <div class="event-action">
            <div class="interest-counter">{{ event.attendees?.length || 0 }}<span>påmeldte</span></div>
            <button :class="['btn-interest', { joined: joined[idx] }]" @click="toggleAttendance(event.id, idx)">
              {{ joined[idx] ? ' Påmeldt' : 'Meld interesse' }}
            </button>
          </div>
        </div>

        <div v-if="events.length === 0" style="padding: 40px; text-align: center; opacity: 0.6;">
          Laster aktiviteter...
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { eventApi } from '../services/api'
import staticEvents from '../data/events.json'

const events = ref([])
const joined = ref({})

const formatDay = (dateString) => {
  return new Date(dateString).getDate()
}

const formatMonth = (dateString) => {
  const months = ['JAN', 'FEB', 'MAR', 'APR', 'MAI', 'JUN', 'JUL', 'AUG', 'SEP', 'OKT', 'NOV', 'DES']
  return months[new Date(dateString).getMonth()]
}

const getCategoryClass = (category) => {
  if (category === 'SOSIALT') return 'pill-soft'
  if (category === 'ECHO') return 'pill-accent'
  return ''
}

const toggleAttendance = async (eventId, idx) => {
  try {
    joined.value[idx] = !joined.value[idx]
    // Call backend to toggle attendance
    await eventApi.toggleAttendance(eventId, 'CurrentUser')
  } catch (error) {
    console.error('Failed to toggle attendance:', error)
    joined.value[idx] = !joined.value[idx]
  }
}

onMounted(async () => {
  try {
    const api = await eventApi.getUpcoming()
    events.value = api.length > 0 ? api : staticEvents
  } catch (error) {
    events.value = staticEvents
  }
  events.value.forEach((_, idx) => {
    joined.value[idx] = false
  })
})
</script>
