<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">{{ photos.length }} bilder · 18 album</span>
        <h1 class="display" style="font-size: clamp(40px, 6vw, 84px); margin: 12px 0 16px;">Galleri<em>.</em></h1>
        <p class="lede">Bilder fra Glassburet, fra fjellturer, kollokvier som ble til pizzakvelder, og helt vanlige tirsdager.</p>
      </div>
      <div class="page-header-meta">
        SIST OPPLASTET<br/>
        I GÅR · KL 21:14<br/>
        AV — Inna
      </div>
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
      <div v-for="photo in filteredPhotos" :key="photo.id" class="photo">
        <img :src="photo.imageUrl" :alt="photo.caption" />
        <div class="caption">{{ photo.caption }}</div>
      </div>

      <div v-if="filteredPhotos.length === 0" style="grid-column: 1 / -1; padding: 40px; text-align: center; opacity: 0.6;">
        Laster bilder...
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { galleryApi } from '../services/api'

const filters = ['Alt', 'Glassburet', 'Turer', 'Fest', 'Kollokvie', 'Echo-events', '2025', '2026']
const activeFilter = ref('Alt')
const photos = ref([])

const filteredPhotos = computed(() => {
  if (activeFilter.value === 'Alt') {
    return photos.value
  }
  return photos.value.filter(p => p.category === activeFilter.value)
})

const applyFilter = (filter) => {
  activeFilter.value = filter
}

onMounted(async () => {
  try {
    photos.value = await galleryApi.getAll()
  } catch (error) {
    console.error('Failed to load photos:', error)
  }
})
</script>
