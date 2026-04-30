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
        MELD INTERESSE →
      </div>
    </div>

    <!-- Admin: Add event -->
    <div v-if="isAdminOrAbove" class="admin-bar">
      <button class="creator-toggle" @click="showCreate = !showCreate">
        <span class="creator-toggle-icon">{{ showCreate ? '−' : '+' }}</span>
        Legg til ny aktivitet
      </button>
      <form v-if="showCreate" class="admin-form" @submit.prevent="createEvent">
        <input v-model="newEvent.title" class="admin-input" placeholder="Tittel" required />
        <textarea v-model="newEvent.description" class="admin-input" placeholder="Beskrivelse" rows="2"></textarea>
        <div class="form-row">
          <input v-model="newEvent.eventDate" class="admin-input" type="date" required />
          <input v-model="newEvent.timeStart" class="admin-input" type="time" placeholder="Start" />
          <input v-model="newEvent.timeEnd" class="admin-input" type="time" placeholder="Slutt" />
        </div>
        <input v-model="newEvent.location" class="admin-input" placeholder="Sted" />
        <select v-model="newEvent.category" class="admin-input">
          <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
        </select>
        <div class="admin-actions">
          <button type="button" class="btn-secondary" @click="showCreate = false">Avbryt</button>
          <button type="submit" class="btn-primary" :disabled="creating">{{ creating ? 'Lagrer…' : 'Publiser' }}</button>
        </div>
      </form>
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
            <button
              v-if="isAuthenticated"
              :class="['btn-interest', { joined: isAttending(event) }]"
              @click="toggleAttendance(event, idx)"
            >
              {{ isAttending(event) ? '✓ Påmeldt' : 'Meld interesse' }}
            </button>
            <div v-if="isAdminOrAbove" class="admin-controls" style="margin-top:8px;">
              <button class="btn-icon" @click="startEdit(event)">Rediger</button>
              <button class="btn-icon btn-danger" @click="deleteEvent(event.id)">Slett</button>
            </div>
          </div>
        </div>

        <div v-if="events.length === 0" style="padding: 40px; text-align: center; opacity: 0.6;">
          Laster aktiviteter...
        </div>
      </div>
    </div>

    <!-- Edit modal -->
    <div v-if="editingEvent" class="modal-overlay" @click.self="editingEvent = null">
      <div class="modal">
        <h3>Rediger aktivitet</h3>
        <form @submit.prevent="saveEdit">
          <input v-model="editForm.title" class="admin-input" placeholder="Tittel" required />
          <textarea v-model="editForm.description" class="admin-input" rows="2"></textarea>
          <div class="form-row">
            <input v-model="editForm.eventDate" class="admin-input" type="date" required />
            <input v-model="editForm.timeStart" class="admin-input" type="time" />
            <input v-model="editForm.timeEnd" class="admin-input" type="time" />
          </div>
          <input v-model="editForm.location" class="admin-input" placeholder="Sted" />
          <select v-model="editForm.category" class="admin-input">
            <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
          </select>
          <div class="admin-actions">
            <button type="button" class="btn-secondary" @click="editingEvent = null">Avbryt</button>
            <button type="submit" class="btn-primary" :disabled="saving">{{ saving ? 'Lagrer…' : 'Lagre' }}</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { eventApi } from '../services/api'
import { isAuthenticated, isAdminOrAbove, displayName } from '../services/authState'
import staticEvents from '../data/events.json'
import { subscribeToUpdates } from '../services/realtime'

const events = ref([])
const categories = ['SOSIALT', 'FAGLIG', 'TUR', 'ECHO', 'GLASSBURET']

const showCreate = ref(false)
const creating = ref(false)
const newEvent = ref({ title: '', description: '', eventDate: '', timeStart: '', timeEnd: '', location: '', category: 'SOSIALT' })

const editingEvent = ref(null)
const editForm = ref({})
const saving = ref(false)

const formatDay = (dateString) => new Date(dateString).getDate()
const formatMonth = (dateString) => {
  const months = ['JAN', 'FEB', 'MAR', 'APR', 'MAI', 'JUN', 'JUL', 'AUG', 'SEP', 'OKT', 'NOV', 'DES']
  return months[new Date(dateString).getMonth()]
}
const getCategoryClass = (category) => {
  if (category === 'SOSIALT') return 'pill-soft'
  if (category === 'ECHO') return 'pill-accent'
  return ''
}

const isAttending = (event) => {
  if (!displayName.value) return false
  return event.attendees?.includes(displayName.value) ?? false
}

const toggleAttendance = async (event) => {
  if (!displayName.value) return
  try {
    const updated = await eventApi.toggleAttendance(event.id, displayName.value)
    const idx = events.value.findIndex(e => e.id === event.id)
    if (idx !== -1) events.value[idx] = updated
  } catch {}
}

const createEvent = async () => {
  creating.value = true
  try {
    const created = await eventApi.create({ ...newEvent.value })
    events.value.push(created)
    events.value.sort((a, b) => new Date(a.eventDate) - new Date(b.eventDate))
    newEvent.value = { title: '', description: '', eventDate: '', timeStart: '', timeEnd: '', location: '', category: 'SOSIALT' }
    showCreate.value = false
  } catch {} finally {
    creating.value = false
  }
}

const startEdit = (event) => {
  editingEvent.value = event
  editForm.value = {
    title: event.title,
    description: event.description || '',
    eventDate: event.eventDate,
    timeStart: event.timeStart || '',
    timeEnd: event.timeEnd || '',
    location: event.location || '',
    category: event.category,
  }
}

const saveEdit = async () => {
  saving.value = true
  try {
    const updated = await eventApi.update(editingEvent.value.id, { ...editForm.value })
    const idx = events.value.findIndex(e => e.id === editingEvent.value.id)
    if (idx !== -1) events.value[idx] = updated
    editingEvent.value = null
  } catch {} finally {
    saving.value = false
  }
}

const deleteEvent = async (id) => {
  if (!confirm('Slett denne aktiviteten?')) return
  try {
    await eventApi.delete(id)
    events.value = events.value.filter(e => e.id !== id)
  } catch {}
}

const loadEvents = async () => {
  try {
    const api = await eventApi.getUpcoming()
    events.value = api.length > 0 ? api : staticEvents
  } catch {
    events.value = staticEvents
  }
}

let unsubscribeEvents = null

onMounted(async () => {
  await loadEvents()
  unsubscribeEvents = subscribeToUpdates('events', loadEvents)
})

onUnmounted(() => {
  if (unsubscribeEvents) unsubscribeEvents()
})
</script>

<style scoped>
.form-row { display: flex; gap: 8px; }
.form-row .admin-input { flex: 1; }
.admin-controls { display: flex; gap: 6px; }
.btn-icon {
  font-family: var(--mono);
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--ink-mute);
  border: 1px solid var(--line-soft);
  border-radius: 4px;
  padding: 3px 8px;
}
.btn-icon:hover { color: var(--ink); border-color: var(--ink); }
.btn-danger:hover { color: var(--accent-2); border-color: var(--accent-2); }
.admin-bar {
  margin-bottom: 32px;
  padding: 20px;
  background: var(--paper);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
}
.admin-form { margin-top: 16px; display: flex; flex-direction: column; gap: 12px; }
.admin-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--line-soft);
  background: var(--bg);
  font-family: var(--sans);
  font-size: 14px;
  border-radius: var(--radius);
  color: var(--ink);
  resize: vertical;
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
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal {
  background: var(--bg);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 32px;
  width: min(520px, 90vw);
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 90vh;
  overflow-y: auto;
}
.modal h3 { font-family: var(--serif); font-size: 24px; }
</style>
