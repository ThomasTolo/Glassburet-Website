<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">Annonser & planlegging</span>
        <h1 class="display" style="font-size: clamp(40px, 6vw, 84px); margin: 12px 0 16px;">Aktiviteter<em>.</em></h1>
        <p class="lede">Det vi planlegger, det vi melder oss på, og ting Echo eller noen i Glassburet vil at vi skal være med på.</p>
      </div>
      <div class="page-header-meta">
        14 KOMMENDE<br/>
        6 DENNE UKEN<br/>
        MELD INTERESSE →
      </div>
    </div>

    <div class="announce-grid">
      <div class="event-list">

        <div class="event-row">
          <div class="event-date-block">
            <div class="event-day">30</div>
            <div class="event-month">APRIL</div>
            <div class="event-time">19:00 → 23:00</div>
          </div>
          <div class="event-body">
            <h3>Pizzakveld i Glassburet</h3>
            <p class="event-desc">Tar med pizza fra Peppes, åpner vinduene, setter på en spilleliste. Ta med drikke selv. Maya tar med spill.</p>
            <div class="event-tags">
              <span class="pill pill-accent">SOSIALT</span>
              <span class="pill">GLASSBURET</span>
              <span class="pill pill-soft">SPONTAN</span>
            </div>
          </div>
          <div class="event-action">
            <div class="interest-counter">12<span>påmeldte</span></div>
            <button :class="['btn-interest', { joined: joined[0] }]" @click="joined[0] = !joined[0]">
              {{ joined[0] ? ' Påmeldt' : 'Meld interesse' }}
            </button>
          </div>
        </div>

        <div class="event-row">
          <div class="event-date-block">
            <div class="event-day">03</div>
            <div class="event-month">MAI</div>
            <div class="event-time">14:00 → 17:00</div>
          </div>
          <div class="event-body">
            <h3>Kollokvie: Lineær Algebra eksamen</h3>
            <p class="event-desc">Vi tar oss gjennom gamle eksamener fra MAT121. Ragnhild har laget en oversikt. Ta med kaffe og frustrasjon.</p>
            <div class="event-tags">
              <span class="pill">FAGLIG</span>
              <span class="pill pill-soft">REALFAGBYGGET</span>
            </div>
          </div>
          <div class="event-action">
            <div class="interest-counter">8<span>påmeldte</span></div>
            <button :class="['btn-interest', { joined: joined[1] }]" @click="joined[1] = !joined[1]">
              {{ joined[1] ? ' Påmeldt' : 'Meld interesse' }}
            </button>
          </div>
        </div>

        <div class="event-row">
          <div class="event-date-block">
            <div class="event-day">07</div>
            <div class="event-month">MAI</div>
            <div class="event-time">16:30 → SENT</div>
          </div>
          <div class="event-body">
            <h3>Echo-tur til Fløyen + grilling</h3>
            <p class="event-desc">Møtes ved Fløybanen 16:30. Echo arrangerer, hele MATNAT er invitert. Grill og tilbehør står klart oppe. Ta med varme klær.</p>
            <div class="event-tags">
              <span class="pill pill-accent">ECHO</span>
              <span class="pill">TUR</span>
              <span class="pill pill-soft">GRILLING</span>
            </div>
          </div>
          <div class="event-action">
            <div class="interest-counter">19<span>påmeldte</span></div>
            <button :class="['btn-interest', { joined: joined[2] }]" @click="joined[2] = !joined[2]">
              {{ joined[2] ? ' Påmeldt' : 'Meld interesse' }}
            </button>
          </div>
        </div>

        <div class="event-row">
          <div class="event-date-block">
            <div class="event-day">12</div>
            <div class="event-month">MAI</div>
            <div class="event-time">20:00 → 02:00</div>
          </div>
          <div class="event-body">
            <h3>Echo Quiz Night — Cafe Opera</h3>
            <p class="event-desc">Lag på maks 6 personer. Glassburet stiller med to lag. Påmelding via Echo sin Facebook, men registrer dere her også så vi vet hvem som er med.</p>
            <div class="event-tags">
              <span class="pill pill-accent">ECHO</span>
              <span class="pill">QUIZ</span>
            </div>
          </div>
          <div class="event-action">
            <div class="interest-counter">11<span>påmeldte</span></div>
            <button :class="['btn-interest', { joined: joined[3] }]" @click="joined[3] = !joined[3]">
              {{ joined[3] ? ' Påmeldt' : 'Meld interesse' }}
            </button>
          </div>
        </div>

        <div class="event-row">
          <div class="event-date-block">
            <div class="event-day">18</div>
            <div class="event-month">MAI</div>
            <div class="event-time">11:00 → 16:00</div>
          </div>
          <div class="event-body">
            <h3>Bryggen-tur og lunsj</h3>
            <p class="event-desc">Spaserer ned til Bryggen, fisketorget, lunsj på et eller annet sted som ikke er for dyrt. Thomas finner sted.</p>
            <div class="event-tags">
              <span class="pill pill-soft">SOSIALT</span>
              <span class="pill">BERGEN</span>
            </div>
          </div>
          <div class="event-action">
            <div class="interest-counter">6<span>påmeldte</span></div>
            <button :class="['btn-interest', { joined: joined[4] }]" @click="joined[4] = !joined[4]">
              {{ joined[4] ? ' Påmeldt' : 'Meld interesse' }}
            </button>
          </div>
        </div>

      </div>

      <aside class="sidebar-card">
        <span class="eyebrow">Foreslå aktivitet</span>
        <h3 style="margin: 12px 0 18px;">Planlegg noe nytt</h3>
        <div class="new-event-form">
          <input type="text" placeholder="Tittel · f.eks. Filmkveld" />
          <textarea placeholder="Hva, hvor, hvorfor — kort og greit"></textarea>
          <div class="form-row">
            <input type="date" />
            <input type="time" />
          </div>
          <select>
            <option>Kategori — sosialt</option>
            <option>Faglig</option>
            <option>Tur</option>
            <option>Echo-event</option>
            <option>Glassburet</option>
          </select>
          <button class="btn-primary">Legg ut forslag</button>
        </div>
        <div style="margin-top: 24px; padding-top: 20px; border-top: 1px solid var(--line-soft); font-size: 12px; color: var(--ink-mute); line-height: 1.5;">
          Forslag blir lagt ut til hele Glassburet. De får meldt interesse direkte. Du kan redigere senere.
        </div>
      </aside>
    </div>
  </section>
</template>

<script setup>
import { reactive } from 'vue'

const joined = reactive([true, false, false, false, false])
</script>
