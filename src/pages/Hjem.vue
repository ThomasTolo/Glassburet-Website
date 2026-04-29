<template>
  <section>

    <div class="hero-grid">
      <div class="hero-left">
        <span class="eyebrow">Utgave 142 · Vår 2026</span>
        <h1 class="display">Velkommen<br/>til <em>Glassburet</em>.</h1>
        <p class="lede">Et kontor med altfor mange vinduer, altfor mange kaffekopper, og helt klart altfor mange one-linere. Her samler vi det vi sier, det vi gjør, og det vi planlegger sammen.</p>
        <div class="hero-meta">
          <div><strong>23</strong>aktive medlemmer</div>
          <div><strong>847</strong>siterte quotes</div>
          <div><strong>14</strong>aktiviteter denne uken</div>
          <div><strong>∞</strong>kaffekopper</div>
        </div>
      </div>

      <div class="hero-card">
        <span class="eyebrow">Dagens quote</span>
        <p class="quote-of-day">var det nå vi skulle grille pølser?</p>
        <div class="quote-author">— Maya, fredag 17:43, plutselig stille i rommet</div>
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
        <div class="stat-big"><em>Thomas</em></div>
        <div style="font-family: var(--mono); font-size: 12px; opacity: 0.75;">412 PTS · 6 SPILL FERDIG</div>
        <div class="rank-mini" style="margin-top: 18px;">
          <div class="rank-row"><span class="rank-num">2</span><span class="rank-name">Maya</span><span class="rank-score">389</span></div>
          <div class="rank-row"><span class="rank-num">3</span><span class="rank-name">Inna</span><span class="rank-score">377</span></div>
          <div class="rank-row"><span class="rank-num">4</span><span class="rank-name">Ragnhild</span><span class="rank-score">351</span></div>
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
          <div class="announce-item">
            <div class="announce-date"><strong>30</strong>APR</div>
            <div>
              <div class="announce-title">Pizzakveld i Glassburet</div>
              <div class="announce-meta">19:00 · Cathinka Guldbergs hus · 12 påmeldte</div>
            </div>
            <span class="pill pill-accent">SOSIALT</span>
          </div>
          <div class="announce-item">
            <div class="announce-date"><strong>03</strong>MAI</div>
            <div>
              <div class="announce-title">Kollokvie: Lineær Algebra eksamen</div>
              <div class="announce-meta">14:00 · Realfagbygget rom 3A1 · 8 påmeldte</div>
            </div>
            <span class="pill">FAGLIG</span>
          </div>
          <div class="announce-item">
            <div class="announce-date"><strong>07</strong>MAI</div>
            <div>
              <div class="announce-title">Echo-tur til Fløyen + grilling</div>
              <div class="announce-meta">16:30 · Møtes ved Fløybanen · 19 påmeldte</div>
            </div>
            <span class="pill pill-soft">TUR</span>
          </div>
        </div>
      </RouterLink>

      <RouterLink to="/quotes" class="card card-4">
        <div class="card-head">
          <span class="eyebrow">Sitater</span>
          <div class="card-arrow">→</div>
        </div>
        <p style="font-family: var(--serif); font-style: italic; font-size: 22px; line-height: 1.25; margin-top: 8px; letter-spacing: -0.01em;">"jeg trodde derivasjon var noe man gjorde med snø"</p>
        <div style="font-family: var(--mono); font-size: 11px; color: var(--ink-mute); margin-top: 16px; text-transform: uppercase; letter-spacing: 0.12em;">— ANONYM, MANDAG MORGEN</div>
        <div style="margin-top: 24px; padding-top: 16px; border-top: 1px solid var(--line-soft); font-size: 12px; color: var(--ink-mute);">3 nye quotes denne uken</div>
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
