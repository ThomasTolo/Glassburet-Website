<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">{{ photos.length }} bilder · 18 album</span>
        <h1 class="display" style="font-size: clamp(40px, 6vw, 84px); margin: 12px 0 16px;">Galleri<em>.</em></h1>
        <p class="lede">Bilder fra Glassburet, fra fjellturer, studiedager som ble til pizzakvelder, og helt vanlige tirsdager.</p>
      </div>
      <div class="page-header-meta">
        SIST OPPLASTET<br/>
        {{ lastUploadedInfo }}<br/>
        {{ lastUploadedBy }}
      </div>
    </div>

    <!-- Admin: Upload photo -->
    <div v-if="isAdminOrAbove" class="admin-bar">
      <button class="creator-toggle" @click="showUpload = !showUpload">
        <span class="creator-toggle-icon">{{ showUpload ? '−' : '+' }}</span>
        Last opp bilde / video
      </button>
      <form v-if="showUpload" class="admin-form" @submit.prevent="uploadPhoto">
        <label class="admin-input" style="display:flex;flex-direction:column;gap:8px;">
          <span style="font-size:12px;opacity:0.7">Last opp fra-PC</span>
          <input ref="fileInput" type="file" accept="image/*,video/*" />
        </label>
        <input v-model="newPhoto.imageUrl" class="admin-input" placeholder="eller URL til bilde eller video" />
        <input v-model="newPhoto.caption" class="admin-input" placeholder="Bildetekst" />
        <select v-model="newPhoto.category" class="admin-input">
          <option v-for="f in filters" :key="f" :value="f">{{ f }}</option>
        </select>
        <div class="admin-actions">
          <button type="button" class="btn-secondary" @click="showUpload = false">Avbryt</button>
          <button type="submit" class="btn-primary" :disabled="uploading">{{ uploading ? 'Laster opp…' : 'Last opp' }}</button>
        </div>
      </form>
    </div>

    <div class="gallery-controls">
      <button
        v-for="filter in filters"
        :key="filter"
        :class="['filter-btn', { active: activeFilter === filter }]"
        @click="applyFilter(filter)"
      >{{ filter }}</button>
    </div>

    <div class="gallery-grid">
      <div v-for="(photo, idx) in filteredPhotos" :key="photo.id" :class="['photo', `p${(idx % 10) + 1}`]">
        <img :src="resolveMediaUrl(photo.imageUrl)" :alt="photo.caption" />
        <div class="caption">{{ photo.caption }}</div>

        <div v-if="isAdminOrAbove" class="photo-admin-overlay">
          <button class="overlay-btn overlay-edit" @click.stop="startEdit(photo)">Rediger</button>
          <button class="overlay-btn overlay-delete" @click.stop="deletePhoto(photo.id)">Slett</button>
        </div>

        <div class="photo-actions">
          <button v-if="isAuthenticated" class="like-btn" :class="{ liked: hasLiked(photo) }" @click="toggleLike(photo)">
            {{ hasLiked(photo) ? '♥' : '♡' }} {{ photo.likes?.length || 0 }}
          </button>
        </div>
      </div>

      <div v-if="filteredPhotos.length === 0" style="grid-column: 1 / -1; padding: 40px; text-align: center; opacity: 0.6;">
        Laster bilder...
      </div>
    </div>

    <!-- Edit modal -->
    <div v-if="editingPhoto" class="modal-overlay" @click.self="editingPhoto = null">
      <div class="modal">
        <h3>Rediger bilde</h3>
        <form @submit.prevent="saveEdit">
          <input v-model="editForm.caption" class="admin-input" placeholder="Bildetekst" />
          <select v-model="editForm.category" class="admin-input">
            <option v-for="f in filters" :key="f" :value="f">{{ f }}</option>
          </select>
          <div class="admin-actions">
            <button type="button" class="btn-secondary" @click="editingPhoto = null">Avbryt</button>
            <button type="submit" class="btn-primary" :disabled="saving">{{ saving ? 'Lagrer…' : 'Lagre' }}</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { galleryApi, resolveMediaUrl } from '../services/api'
import { isAuthenticated, isAdminOrAbove, displayName } from '../services/authState'
import { subscribeToUpdates } from '../services/realtime'

const filters = ['Alt', 'Glassburet', 'Turer', 'Fest', 'Studiedager ', 'Echo-events', 'Memes', '2025', '2026']
const activeFilter = ref('Alt')
const photos = ref([])
const showUpload = ref(false)
const uploading = ref(false)
const newPhoto = ref({ imageUrl: '', caption: '', category: 'Glassburet' })

const editingPhoto = ref(null)
const editForm = ref({ caption: '', category: '' })
const saving = ref(false)

const filteredPhotos = computed(() => {
  if (activeFilter.value === 'Alt') return photos.value
  return photos.value.filter(p => p.category === activeFilter.value)
})

const lastUploadedInfo = computed(() => {
  if (photos.value.length === 0) return 'INGEN BILDER'
  const latest = photos.value[0]
  if (!latest.uploadedAt) return 'UKJENT'
  const d = new Date(latest.uploadedAt)
  return `${d.getDate()}.${d.getMonth() + 1}.${d.getFullYear()}`
})

const lastUploadedBy = computed(() => {
  if (photos.value.length === 0) return ''
  return photos.value[0]?.uploadedBy ? `AV — ${photos.value[0].uploadedBy}` : ''
})

const applyFilter = (filter) => { activeFilter.value = filter }

const hasLiked = (photo) => photo.likes?.includes(displayName.value)

const toggleLike = async (photo) => {
  try {
    const updated = await galleryApi.like(photo.id)
    const idx = photos.value.findIndex(p => p.id === photo.id)
    if (idx !== -1) photos.value[idx] = updated
  } catch {}
}

  const fileInput = ref(null)

  const uploadPhoto = async () => {
    uploading.value = true
    try {
      const file = fileInput.value?.files?.[0]
      let created
      if (file) {
        const fd = new FormData()
        fd.append('file', file)
        fd.append('caption', newPhoto.value.caption || '')
        fd.append('category', newPhoto.value.category || '')
        fd.append('album', newPhoto.value.category || '')
        fd.append('uploadedBy', displayName.value || '')
        created = await galleryApi.uploadFile(fd)
      } else {
        created = await galleryApi.upload({
          ...newPhoto.value,
          uploadedBy: displayName.value,
          album: newPhoto.value.category,
        })
      }
      photos.value.unshift(created)
      newPhoto.value = { imageUrl: '', caption: '', category: 'Glassburet' }
      if (fileInput.value) fileInput.value.value = null
      showUpload.value = false
    } catch (e) {
      console.error('Upload failed', e)
    } finally {
      uploading.value = false
    }
  }

const startEdit = (photo) => {
  editingPhoto.value = photo
  editForm.value = { caption: photo.caption || '', category: photo.category || 'Alt' }
}

const saveEdit = async () => {
  saving.value = true
  try {
    const updated = await galleryApi.update(editingPhoto.value.id, { ...editForm.value })
    const idx = photos.value.findIndex(p => p.id === editingPhoto.value.id)
    if (idx !== -1) photos.value[idx] = updated
    editingPhoto.value = null
  } catch {} finally {
    saving.value = false
  }
}

const deletePhoto = async (id) => {
  if (!confirm('Slett dette bildet?')) return
  try {
    await galleryApi.delete(id)
    photos.value = photos.value.filter(p => p.id !== id)
  } catch {}
}

const loadPhotos = async () => {
  try { photos.value = await galleryApi.getAll() } catch {}
}

let unsubscribeGallery = null

onMounted(async () => {
  await loadPhotos()
  unsubscribeGallery = subscribeToUpdates('gallery', loadPhotos)
})

onUnmounted(() => {
  if (unsubscribeGallery) unsubscribeGallery()
})
</script>

<style scoped>
.photo-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 0 2px;
}
.like-btn {
  font-family: var(--mono);
  font-size: 12px;
  padding: 3px 10px;
  border: 1px solid var(--line-soft);
  border-radius: 999px;
  color: var(--ink-mute);
  transition: all 0.2s;
}
.like-btn:hover, .like-btn.liked { color: var(--accent-2); border-color: var(--accent-2); }
.photo-admin-overlay {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: opacity 0.2s;
  z-index: 2;
}
.photo:hover .photo-admin-overlay { opacity: 1; }
.overlay-btn {
  padding: 5px 12px;
  font-family: var(--mono);
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  border-radius: 4px;
  cursor: pointer;
  border: none;
}
.overlay-edit { background: rgba(255, 255, 255, 0.88); color: #111; }
.overlay-delete { background: rgba(180, 40, 40, 0.88); color: #fff; }
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
  margin-bottom: 24px;
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
