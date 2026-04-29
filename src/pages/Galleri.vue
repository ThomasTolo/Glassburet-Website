<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">247 bilder · 18 album</span>
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
        @click="activeFilter = filter"
      >{{ filter }}</button>
    </div>

    <div class="gallery-grid">
      <div class="photo p1"><div class="caption">Fløyen-tur · Mai 2025</div></div>
      <div class="photo p2"><div class="caption">Pizzakveld</div></div>
      <div class="photo p3"><div class="caption">Eksamen-fest</div></div>
      <div class="photo p4"><div class="caption">Blue-hour på UiB</div></div>
      <div class="photo p5"><div class="caption">Kollokvie</div></div>
      <div class="photo p6"><div class="caption">Echo-arrangement</div></div>
      <div class="photo p7"><div class="caption">Glassburet kl 23</div></div>
      <div class="photo p8"><div class="caption">Hytta i jul</div></div>
      <div class="photo p9"><div class="caption">Strand-tur Askøy</div></div>
      <div class="photo p10"><div class="caption">Sent på en torsdag</div></div>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'

const filters = ['Alt', 'Glassburet', 'Turer', 'Fest', 'Kollokvie', 'Echo-events', '2025', '2026']
const activeFilter = ref('Alt')
</script>
