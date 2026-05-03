<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">Arkiv · {{ quotes.length }} sitater</span>
        <h1 class="display" style="font-size: clamp(40px, 6vw, 84px); margin: 12px 0 16px;">Dagens <em>quote</em>.</h1>
        <p class="lede">Det folk har sagt høyt, ofte ved et uhell. Sitater hentet ut av kontekst og bevart for all evighet.</p>
      </div>
      <div class="page-header-meta">
        {{ dateLabel }}<br/>
        {{ semesterLabel }}<br/>
        {{ quotes.length > 0 ? quotes.length + ' I ARKIVET' : 'LASTER...' }}
      </div>
    </div>

    <!-- Admin: Add quote -->
    <div v-if="isAdminOrAbove" class="admin-bar">
      <button class="creator-toggle" @click="showCreate = !showCreate">
        <span class="creator-toggle-icon">{{ showCreate ? '−' : '+' }}</span>
        Legg til ny quote
      </button>
      <form v-if="showCreate" class="admin-form" @submit.prevent="createQuote">
        <textarea v-model="newQuote.text" class="admin-input" placeholder="Sitatets tekst…" rows="3" required></textarea>
        <input v-model="newQuote.author" class="admin-input" placeholder="Hvem sa det?" required />
        <label style="font-size:13px;color:var(--ink-mute);">Dato for sitatet
          <input type="date" v-model="newQuote.createdAt" class="admin-input" style="margin-top:6px;" />
        </label>
        <label class="admin-check"><input type="checkbox" v-model="newQuote.featured" /> Sett som dagens quote</label>
        <div class="admin-actions">
          <button type="button" class="btn-secondary" @click="showCreate = false">Avbryt</button>
          <button type="submit" class="btn-primary" :disabled="creating">{{ creating ? 'Lagrer…' : 'Publiser' }}</button>
        </div>
      </form>
    </div>

    <div class="quote-grid">
      <article v-for="(quote, idx) in quotes" :key="quote.id" :class="quoteArtClasses(quote, idx)">
        <span class="quote-number">№ {{ String(idx + 1).padStart(2, '0') }}</span>
        <span v-if="isToday(quote.createdAt)" class="quote-day-tag">⭐ DAGENS</span>
        <p class="quote-text">"{{ quote.text }}"</p>
        <div class="quote-foot">
          <span>— {{ quote.author }}</span>
          <span v-if="quote.createdAt">{{ formatDate(quote.createdAt) }}</span>
        </div>

        <!-- Like button -->
        <div class="quote-actions" v-if="isAuthenticated">
          <button class="like-btn" :class="{ liked: hasLiked(quote) }" @click="toggleLike(quote)">
            {{ hasLiked(quote) ? '♥' : '♡' }} {{ quote.likes?.length || 0 }}
          </button>
        </div>

        <!-- Admin controls -->
        <div class="admin-controls" v-if="isAdminOrAbove">
          <button class="btn-icon" @click="startEdit(quote)">Rediger</button>
          <button class="btn-icon btn-danger" @click="deleteQuote(quote.id)">Slett</button>
        </div>
      </article>
    </div>

    <!-- Edit modal -->
    <div v-if="editingQuote" class="modal-overlay" @click.self="editingQuote = null">
      <div class="modal">
        <h3>Rediger quote</h3>
        <form @submit.prevent="saveEdit">
          <textarea v-model="editForm.text" class="admin-input" rows="3" required></textarea>
          <input v-model="editForm.author" class="admin-input" required />
          <label style="font-size:13px;color:var(--ink-mute);">Dato for sitatet
            <input type="date" v-model="editForm.createdAt" class="admin-input" style="margin-top:6px;" />
          </label>
          <label class="admin-check"><input type="checkbox" v-model="editForm.featured" /> Dagens quote</label>
          <div class="admin-actions">
            <button type="button" class="btn-secondary" @click="editingQuote = null">Avbryt</button>
            <button type="submit" class="btn-primary" :disabled="saving">{{ saving ? 'Lagrer…' : 'Lagre' }}</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { quoteApi } from '../services/api'
import { isAuthenticated, isAdminOrAbove, displayName } from '../services/authState'
import { useLiveDateInfo } from '../composables/useLiveDateInfo'
import { subscribeToUpdates } from '../services/realtime'

const quotes = ref([])
const showCreate = ref(false)
const creating = ref(false)
const newQuote = ref({ text: '', author: '', featured: false, createdAt: null })
const { dateLabel, semesterLabel } = useLiveDateInfo({ intervalMs: 60000 })

const editingQuote = ref(null)
const editForm = ref({ text: '', author: '', featured: false, createdAt: null })
const saving = ref(false)
const quoteTones = ['forest', 'paper', 'rust', 'cream', 'ink', 'gold', 'sage', 'clay']
const quoteShapes = ['hero', 'wide', 'stack', 'spotlight', 'small', 'tall', 'wide', 'small']

const quoteArtClasses = (quote, idx) => {
  const textLength = quote.text?.length || 0
  return [
    'quote-card',
    `quote-tone-${quoteTones[idx % quoteTones.length]}`,
    `quote-shape-${quoteShapes[idx % quoteShapes.length]}`,
    textLength <= 52 ? 'quote-short' : '',
    textLength > 90 ? 'quote-long' : '',
    textLength > 140 ? 'quote-very-long' : '',
    isToday(quote.createdAt) ? 'featured' : '',
  ]
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  // Accept either full ISO datetime or date-only (YYYY-MM-DD or YYYY-MM-DDTHH:mm:ss)
  const datePart = dateString.length >= 10 ? dateString.substring(0, 10) : dateString
  const d = new Date(datePart + 'T00:00:00')
  const dd = String(d.getDate()).padStart(2, '0')
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const yyyy = d.getFullYear()
  return `${dd}.${mm}.${yyyy}`
}

const isToday = (dateString) => {
  if (!dateString) return false
  const datePart = dateString.length >= 10 ? dateString.substring(0, 10) : dateString
  const d = new Date(datePart + 'T00:00:00')
  const today = new Date()
  return d.getFullYear() === today.getFullYear() && d.getMonth() === today.getMonth() && d.getDate() === today.getDate()
}

const hasLiked = (quote) => quote.likes?.includes(displayName.value)

const toggleLike = async (quote) => {
  try {
    const updated = await quoteApi.like(quote.id)
    const idx = quotes.value.findIndex(q => q.id === quote.id)
    if (idx !== -1) quotes.value[idx] = updated
  } catch {}
}

const createQuote = async () => {
  creating.value = true
  try {
    const payload = { ...newQuote.value }
    if (payload.createdAt) payload.createdAt = normalizeDateForApi(payload.createdAt)
    const created = await quoteApi.create(payload)
    quotes.value.unshift(created)
    newQuote.value = { text: '', author: '', featured: false, createdAt: null }
    showCreate.value = false
  } catch {} finally {
    creating.value = false
  }
}

const startEdit = (quote) => {
  editingQuote.value = quote
  editForm.value = { text: quote.text, author: quote.author, featured: quote.featured, createdAt: quote.createdAt ? quote.createdAt.substring(0,10) : null }
}

const saveEdit = async () => {
  saving.value = true
  try {
    const payload = { ...editForm.value }
    if (payload.createdAt) payload.createdAt = normalizeDateForApi(payload.createdAt)
    const updated = await quoteApi.update(editingQuote.value.id, payload)
    const idx = quotes.value.findIndex(q => q.id === editingQuote.value.id)
    if (idx !== -1) quotes.value[idx] = updated
    editingQuote.value = null
  } catch {} finally {
    saving.value = false
  }
}

const normalizeDateForApi = (dateString) => {
  // date input gives 'YYYY-MM-DD' — convert to LocalDateTime at midnight for backend parse
  if (!dateString) return null
  // if user supplies full datetime already, keep it
  if (dateString.length === 10) return dateString + 'T00:00:00'
  return dateString
}

const deleteQuote = async (id) => {
  if (!confirm('Slett denne quoten?')) return
  try {
    await quoteApi.delete(id)
    quotes.value = quotes.value.filter(q => q.id !== id)
  } catch {}
}

const loadQuotes = async () => {
  try {
    quotes.value = await quoteApi.getAll()
  } catch {}
}

let unsubscribeQuotes = null

onMounted(async () => {
  await loadQuotes()
  unsubscribeQuotes = subscribeToUpdates('quotes', loadQuotes)
})

onUnmounted(() => {
  if (unsubscribeQuotes) unsubscribeQuotes()
})
</script>

<style scoped>
.admin-bar {
  margin-bottom: 32px;
  padding: 20px;
  background: var(--paper);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
}
.admin-form {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
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
.admin-check {
  font-size: 13px;
  color: var(--ink-soft);
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
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
.quote-actions {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid var(--quote-line, var(--line-soft));
  position: relative;
  z-index: 1;
}
.like-btn {
  font-family: var(--mono);
  font-size: 13px;
  color: var(--quote-muted, var(--ink-mute));
  padding: 4px 10px;
  border: 1px solid var(--quote-line, var(--line-soft));
  border-radius: 999px;
  transition: all 0.2s;
}
.like-btn:hover { color: var(--accent-2); border-color: var(--accent-2); }
.like-btn.liked { color: var(--accent-2); border-color: var(--accent-2); }
.admin-controls {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  position: relative;
  z-index: 1;
}
.btn-icon {
  font-family: var(--mono);
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--quote-muted, var(--ink-mute));
  border: 1px solid var(--quote-line, var(--line-soft));
  border-radius: 4px;
  padding: 3px 8px;
}
.btn-icon:hover { color: var(--quote-fg, var(--ink)); border-color: var(--quote-fg, var(--ink)); }
.btn-danger:hover { color: var(--accent-2); border-color: var(--accent-2); }
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
  width: min(480px, 90vw);
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.modal h3 { font-family: var(--serif); font-size: 24px; }
</style>
