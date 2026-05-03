<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">Hall of fame · {{ liners.length }} stk</span>
        <h1 class="display" style="font-size: clamp(40px, 6vw, 84px); margin: 12px 0 16px;">Beste <em>one-liners</em>.</h1>
        <p class="lede">Setninger som har overlevd tid og skam.</p>
      </div>
      <div class="page-header-meta">
        ALLE TIDERS BESTE<br/>
        ONE-LINERS FRA ROMMET<br/>
        OPPDATERT UKENTLIG
      </div>
    </div>

    <!-- Admin: Add liner -->
    <div v-if="isAdminOrAbove" class="admin-bar">
      <button class="creator-toggle" @click="showCreate = !showCreate">
        <span class="creator-toggle-icon">{{ showCreate ? '−' : '+' }}</span>
        Legg til ny one-liner
      </button>
      <form v-if="showCreate" class="admin-form" @submit.prevent="createLiner">
        <textarea v-model="newLiner.text" class="admin-input" placeholder="One-lineren…" rows="2" required></textarea>
        <input v-model="newLiner.author" class="admin-input" placeholder="Hvem sa det?" />
        <input v-model.number="newLiner.sinceYear" class="admin-input" type="number" placeholder="År (valgfritt)" />
        <div class="admin-actions">
          <button type="button" class="btn-secondary" @click="showCreate = false">Avbryt</button>
          <button type="submit" class="btn-primary" :disabled="creating">{{ creating ? 'Lagrer…' : 'Publiser' }}</button>
        </div>
      </form>
    </div>

    <div class="liner-wall">
      <div v-for="(liner, idx) in liners" :key="liner.id" :class="['liner', `liner-${(idx % 7) + 1}`]">
        <div class="liner-inner" @click="incrementSaid(liner.id)">
          <span class="liner-num">№ {{ String(idx + 1).padStart(2, '0') }}</span>
          <p class="liner-text">"{{ liner.text }}"</p>
          <div class="liner-meta">
            <span v-if="(liner.saidCount || 0) > 0">SAGT {{ liner.saidCount }} GANGER</span>
            <span>— {{ liner.author }}</span>
          </div>
        </div>

        <!-- Like + admin controls -->
        <div class="liner-footer">
          <button v-if="isAuthenticated" class="like-btn" :class="{ liked: hasLiked(liner) }" @click.stop="toggleLike(liner)">
            {{ hasLiked(liner) ? '♥' : '♡' }} {{ liner.likes?.length || 0 }}
          </button>
          <div v-if="isAdminOrAbove" class="admin-controls">
            <button class="btn-icon" @click.stop="startEdit(liner)">Rediger</button>
            <button class="btn-icon btn-danger" @click.stop="deleteLiner(liner.id)">Slett</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit modal -->
    <div v-if="editingLiner" class="modal-overlay" @click.self="editingLiner = null">
      <div class="modal">
        <h3>Rediger one-liner</h3>
        <form @submit.prevent="saveEdit">
          <textarea v-model="editForm.text" class="admin-input" rows="2" required></textarea>
          <input v-model="editForm.author" class="admin-input" placeholder="Forfatter" />
          <input v-model.number="editForm.sinceYear" class="admin-input" type="number" placeholder="År" />
          <div class="admin-actions">
            <button type="button" class="btn-secondary" @click="editingLiner = null">Avbryt</button>
            <button type="submit" class="btn-primary" :disabled="saving">{{ saving ? 'Lagrer…' : 'Lagre' }}</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { linerApi } from '../services/api'
import { isAuthenticated, isAdminOrAbove, displayName } from '../services/authState'
import { subscribeToUpdates } from '../services/realtime'

const liners = ref([])
const showCreate = ref(false)
const creating = ref(false)
const newLiner = ref({ text: '', author: '', sinceYear: null })

const editingLiner = ref(null)
const editForm = ref({ text: '', author: '', sinceYear: null })
const saving = ref(false)

const hasLiked = (liner) => liner.likes?.includes(displayName.value)

const incrementSaid = async (id) => {
  try {
    const updated = await linerApi.incrementSaidCount(id)
    const liner = liners.value.find(l => l.id === id)
    if (liner) liner.saidCount = updated.saidCount
  } catch {}
}

const toggleLike = async (liner) => {
  try {
    const updated = await linerApi.like(liner.id)
    const idx = liners.value.findIndex(l => l.id === liner.id)
    if (idx !== -1) liners.value[idx] = updated
  } catch {}
}

const createLiner = async () => {
  creating.value = true
  try {
    const created = await linerApi.create({ ...newLiner.value })
    liners.value.push(created)
    liners.value.sort((a, b) => a.number - b.number)
    newLiner.value = { text: '', author: '', sinceYear: null }
    showCreate.value = false
  } catch {} finally {
    creating.value = false
  }
}

const startEdit = (liner) => {
  editingLiner.value = liner
  editForm.value = { text: liner.text, author: liner.author || '', sinceYear: liner.sinceYear }
}

const saveEdit = async () => {
  saving.value = true
  try {
    const updated = await linerApi.update(editingLiner.value.id, { ...editForm.value, number: editingLiner.value.number })
    const idx = liners.value.findIndex(l => l.id === editingLiner.value.id)
    if (idx !== -1) liners.value[idx] = updated
    editingLiner.value = null
  } catch {} finally {
    saving.value = false
  }
}

const deleteLiner = async (id) => {
  if (!confirm('Slett denne one-lineren?')) return
  try {
    await linerApi.delete(id)
    liners.value = liners.value.filter(l => l.id !== id)
  } catch {}
}

const loadLiners = async () => {
  try { liners.value = await linerApi.getAll() } catch {}
}

let unsubscribeLiners = null

onMounted(async () => {
  await loadLiners()
  unsubscribeLiners = subscribeToUpdates('liners', loadLiners)
})

onUnmounted(() => {
  if (unsubscribeLiners) unsubscribeLiners()
})
</script>

<style scoped>
.liner {
  --liner-action-color: rgba(20,32,28,0.58);
  --liner-action-line: rgba(20,32,28,0.22);
  overflow: visible;
}
.liner-1,
.liner-3,
.liner-5 {
  --liner-action-color: rgba(255,255,255,0.68);
  --liner-action-line: rgba(255,255,255,0.24);
}
.liner-inner { cursor: pointer; }
.liner-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px solid var(--liner-action-line);
  position: relative;
  z-index: 1;
}
.like-btn {
  font-family: var(--mono);
  font-size: 12px;
  padding: 3px 10px;
  border: 1px solid var(--liner-action-line);
  border-radius: 999px;
  color: var(--liner-action-color);
  transition: all 0.2s;
}
.like-btn:hover, .like-btn.liked { color: currentColor; border-color: currentColor; }
.admin-controls { display: flex; gap: 6px; }
.btn-icon {
  font-family: var(--mono);
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--liner-action-color);
  border: 1px solid var(--liner-action-line);
  border-radius: 4px;
  padding: 3px 8px;
}
.btn-icon:hover { color: currentColor; border-color: currentColor; }
.btn-danger:hover { color: #f88; border-color: #f88; }
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
  width: min(480px, 90vw);
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.modal h3 { font-family: var(--serif); font-size: 24px; }
</style>
