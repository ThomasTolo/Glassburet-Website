<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">Daglig konkurranse</span>
        <h1 class="display" style="font-size: clamp(40px, 6vw, 84px); margin: 12px 0 16px;">Poeng<em>tavle</em>.</h1>
        <p class="lede">Hvor flink er du egentlig sammenlignet med Maya? Resultater fra dagens og ukens spill — Connections, Wordle, More or Less, Spotle, TimeGuessr.</p>
      </div>
      <div class="page-header-meta">
        DAG 142 · UKE 18<br/>
        18 / 23 SPILT I DAG<br/>
        OPPDATERES AUTOMATISK
      </div>
    </div>

    <div class="lb-tabs">
      <button
        v-for="(tab, i) in tabs"
        :key="tab"
        :class="['lb-tab', { active: activeTab === i }]"
        @click="activeTab = i"
      >
        {{ tab }}<span v-if="i === 0" class="badge">142</span>
      </button>
    </div>

    <div class="lb-grid">
      <div class="game-card">
        <div class="game-head">
          <span class="game-name">Connections</span>
          <span class="game-emoji">🟦🟨🟩🟪</span>
        </div>
        <div class="game-stats">
          <div class="game-stat-block"><div class="game-stat-label">Snitt forsøk</div><div class="game-stat-val">4.2</div></div>
          <div class="game-stat-block"><div class="game-stat-label">Klart i dag</div><div class="game-stat-val">12/18</div></div>
        </div>
        <div class="game-leaders">
          <div class="game-leader"><span class="gl-rank">1</span><span class="gl-name">Thomas</span><span class="gl-score">4·0 feil</span></div>
          <div class="game-leader"><span class="gl-rank">2</span><span class="gl-name">Inna</span><span class="gl-score">4·1 feil</span></div>
          <div class="game-leader"><span class="gl-rank">3</span><span class="gl-name">Ragnhild</span><span class="gl-score">5·1 feil</span></div>
        </div>
      </div>

      <div class="game-card">
        <div class="game-head">
          <span class="game-name">Wordle</span>
          <span class="game-emoji">🟩🟨⬜</span>
        </div>
        <div class="game-stats">
          <div class="game-stat-block"><div class="game-stat-label">Snitt forsøk</div><div class="game-stat-val">3.8</div></div>
          <div class="game-stat-block"><div class="game-stat-label">Streak</div><div class="game-stat-val">23🔥</div></div>
        </div>
        <div class="game-leaders">
          <div class="game-leader"><span class="gl-rank">1</span><span class="gl-name">Maya</span><span class="gl-score">3/6</span></div>
          <div class="game-leader"><span class="gl-rank">2</span><span class="gl-name">Thomas</span><span class="gl-score">3/6</span></div>
          <div class="game-leader"><span class="gl-rank">3</span><span class="gl-name">Victor</span><span class="gl-score">4/6</span></div>
        </div>
      </div>

      <div class="game-card">
        <div class="game-head">
          <span class="game-name">More or Less</span>
          <span class="game-emoji">📈</span>
        </div>
        <div class="game-stats">
          <div class="game-stat-block"><div class="game-stat-label">Snitt streak</div><div class="game-stat-val">14</div></div>
          <div class="game-stat-block"><div class="game-stat-label">Best i dag</div><div class="game-stat-val">42</div></div>
        </div>
        <div class="game-leaders">
          <div class="game-leader"><span class="gl-rank">1</span><span class="gl-name">Ragnhild</span><span class="gl-score">42 i streak</span></div>
          <div class="game-leader"><span class="gl-rank">2</span><span class="gl-name">Kriss</span><span class="gl-score">31</span></div>
          <div class="game-leader"><span class="gl-rank">3</span><span class="gl-name">Hannah</span><span class="gl-score">28</span></div>
        </div>
      </div>

      <div class="game-card">
        <div class="game-head">
          <span class="game-name">Spotle</span>
          <span class="game-emoji">🎵</span>
        </div>
        <div class="game-stats">
          <div class="game-stat-block"><div class="game-stat-label">Snitt forsøk</div><div class="game-stat-val">2.4</div></div>
          <div class="game-stat-block"><div class="game-stat-label">Klart i dag</div><div class="game-stat-val">14/18</div></div>
        </div>
        <div class="game-leaders">
          <div class="game-leader"><span class="gl-rank">1</span><span class="gl-name">Inna</span><span class="gl-score">1/6</span></div>
          <div class="game-leader"><span class="gl-rank">2</span><span class="gl-name">Halvard</span><span class="gl-score">2/6</span></div>
          <div class="game-leader"><span class="gl-rank">3</span><span class="gl-name">Magne</span><span class="gl-score">3/6</span></div>
        </div>
      </div>

      <div class="game-card">
        <div class="game-head">
          <span class="game-name">TimeGuessr</span>
          <span class="game-emoji">🕰️</span>
        </div>
        <div class="game-stats">
          <div class="game-stat-block"><div class="game-stat-label">Snitt poeng</div><div class="game-stat-val">38k</div></div>
          <div class="game-stat-block"><div class="game-stat-label">Maks i dag</div><div class="game-stat-val">47k</div></div>
        </div>
        <div class="game-leaders">
          <div class="game-leader"><span class="gl-rank">1</span><span class="gl-name">Thomas</span><span class="gl-score">47 200</span></div>
          <div class="game-leader"><span class="gl-rank">2</span><span class="gl-name">Maya</span><span class="gl-score">44 850</span></div>
          <div class="game-leader"><span class="gl-rank">3</span><span class="gl-name">Henrik</span><span class="gl-score">41 100</span></div>
        </div>
      </div>

      <div class="game-card">
        <div class="game-head">
          <span class="game-name">GeoGuessr</span>
          <span class="game-emoji">🌍</span>
        </div>
        <div class="game-stats">
          <div class="game-stat-block"><div class="game-stat-label">Snitt poeng</div><div class="game-stat-val">22k</div></div>
          <div class="game-stat-block"><div class="game-stat-label">Maks i dag</div><div class="game-stat-val">28k</div></div>
        </div>
        <div class="game-leaders">
          <div class="game-leader"><span class="gl-rank">1</span><span class="gl-name">Maya</span><span class="gl-score">28 400</span></div>
          <div class="game-leader"><span class="gl-rank">2</span><span class="gl-name">Ragnhild</span><span class="gl-score">25 100</span></div>
          <div class="game-leader"><span class="gl-rank">3</span><span class="gl-name">Thomas</span><span class="gl-score">23 700</span></div>
        </div>
      </div>
    </div>

    <h2 class="section-title" style="margin: 16px 0 24px;">Sammenlagt <em>i dag</em></h2>

    <div class="lb-table">
      <div class="lb-table-head">
        <span>#</span>
        <span>Navn</span>
        <span>Connections</span>
        <span>Wordle</span>
        <span>MoL</span>
        <span>Spotle</span>
        <span>TimeGuessr</span>
        <span style="text-align:right;">Total</span>
      </div>

      <div class="lb-row podium-1">
        <span class="row-rank r1">1</span>
        <div class="row-name">Thomas<small>THOM · 6 spill</small></div>
        <span class="row-score">95</span><span class="row-score">82</span><span class="row-score">71</span><span class="row-score">79</span><span class="row-score">85</span>
        <span class="row-total">412</span>
      </div>
      <div class="lb-row podium-2">
        <span class="row-rank r2">2</span>
        <div class="row-name">Maya<small>MAYA · 6 spill</small></div>
        <span class="row-score">78</span><span class="row-score">90</span><span class="row-score">68</span><span class="row-score">73</span><span class="row-score">80</span>
        <span class="row-total">389</span>
      </div>
      <div class="lb-row podium-3">
        <span class="row-rank r3">3</span>
        <div class="row-name">Inna<small>INNA · 6 spill</small></div>
        <span class="row-score">88</span><span class="row-score">75</span><span class="row-score">62</span><span class="row-score">90</span><span class="row-score">62</span>
        <span class="row-total">377</span>
      </div>
      <div class="lb-row">
        <span class="row-rank">4</span>
        <div class="row-name">Ragnhild<small>RAGN · 5 spill</small></div>
        <span class="row-score">81</span><span class="row-score">70</span><span class="row-score">95</span><span class="row-score">—</span><span class="row-score">75</span>
        <span class="row-total">351</span>
      </div>
      <div class="lb-row">
        <span class="row-rank">5</span>
        <div class="row-name">Kriss<small>KRIS · 5 spill</small></div>
        <span class="row-score">72</span><span class="row-score">78</span><span class="row-score">82</span><span class="row-score">68</span><span class="row-score">—</span>
        <span class="row-total">340</span>
      </div>
      <div class="lb-row">
        <span class="row-rank">6</span>
        <div class="row-name">Halvard<small>HALV · 5 spill</small></div>
        <span class="row-score">68</span><span class="row-score">72</span><span class="row-score">—</span><span class="row-score">85</span><span class="row-score">70</span>
        <span class="row-total">325</span>
      </div>
      <div class="lb-row">
        <span class="row-rank">7</span>
        <div class="row-name">Victor<small>VICT · 5 spill</small></div>
        <span class="row-score">65</span><span class="row-score">80</span><span class="row-score">58</span><span class="row-score">—</span><span class="row-score">68</span>
        <span class="row-total">312</span>
      </div>
      <div class="lb-row">
        <span class="row-rank">8</span>
        <div class="row-name">Magne<small>MAGN · 4 spill</small></div>
        <span class="row-score">70</span><span class="row-score">—</span><span class="row-score">60</span><span class="row-score">76</span><span class="row-score">65</span>
        <span class="row-total">271</span>
      </div>
      <div class="lb-row">
        <span class="row-rank">9</span>
        <div class="row-name">Henrik<small>HENR · 3 spill</small></div>
        <span class="row-score">—</span><span class="row-score">68</span><span class="row-score">—</span><span class="row-score">62</span><span class="row-score">82</span>
        <span class="row-total">212</span>
      </div>
      <div class="lb-row">
        <span class="row-rank">10</span>
        <div class="row-name">Hannah<small>HANN · 3 spill</small></div>
        <span class="row-score">62</span><span class="row-score">—</span><span class="row-score">75</span><span class="row-score">—</span><span class="row-score">55</span>
        <span class="row-total">192</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'

const tabs = ['I dag', 'Denne uken', 'Denne måneden', 'All-time']
const activeTab = ref(0)
</script>
