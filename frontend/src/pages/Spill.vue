<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">Daglige spill · 2 tilgjengelig</span>
        <h1 class="display" style="font-size: clamp(40px, 6vw, 84px); margin: 12px 0 16px;">
          Dagens <em>spill</em>.
        </h1>
        <p class="lede">Daglige utfordringer for deg og vennegjengen. Hvem er egentlig skarpest?</p>
      </div>
      <div class="page-header-meta">
        {{ dateLabel }}<br />
        {{ semesterLabel }}<br />
        2 SPILL I DAG
      </div>
    </div>

    <div class="sp-tabs">
      <button :class="['sp-tab', activeGame === 'connections' && 'active']" @click="activeGame = 'connections'; creatorOpen = false">
        Tankeburet
      </button>
      <button :class="['sp-tab', activeGame === 'wordle' && 'active']" @click="activeGame = 'wordle'; creatorOpen = false">
        Ordburet
      </button>
    </div>

    <!-- ═══════════════ CONNECTIONS ═══════════════ -->
    <div v-if="activeGame === 'connections'" class="sp-panel">
      <div class="conn-top">
        <div>
          <h2 class="section-title">Tankeburet</h2>
          <p class="sp-sub">
            Finn fire grupper av fire ord som hører sammen.
            <span v-if="connPuzzle.createdBy" class="puzzle-by">Laget av {{ connPuzzle.createdBy }}</span>
          </p>
        </div>
        <div class="conn-mistakes-box">
          <span class="eyebrow">Feil igjen</span>
          <div class="mistake-dots">
            <span v-for="i in 4" :key="i" :class="['mdot', i <= (4 - connMistakes) ? 'active' : '']" />
          </div>
        </div>
      </div>

      <!-- Puzzle library -->
      <div class="puzzle-library">
        <span class="eyebrow" style="margin-bottom:8px;display:block;">Velg puzzle</span>
        <div class="puzzle-list">
          <button
            v-for="p in allConnPuzzles"
            :key="p.id"
            :class="['puzzle-pick', selectedConnId === p.id && 'active', completedConnIds.includes(p.id) && 'done']"
            @click="loadConnPuzzle(p)"
          >
            <span>{{ p.isDaily ? '⊙ Daglig' : (p.createdBy || 'Anonym') }}</span>
            <span class="puzzle-pick-date">{{ formatPuzzleDate(p.createdAt) }}</span>
            <span v-if="completedConnIds.includes(p.id)" class="puzzle-pick-done">✓</span>
          </button>
        </div>
      </div>

      <div class="conn-solved">
        <div
          v-for="group in connSolved"
          :key="group.category"
          :class="['conn-group-solved', `diff-${group.difficulty}`]"
        >
          <strong>{{ group.category }}</strong>
          <span>{{ group.words.join(' · ') }}</span>
        </div>
      </div>

      <template v-if="connGameState === 'playing'">
        <div class="conn-grid">
          <button
            v-for="word in connShuffled"
            :key="word"
            :class="['conn-word', connSelected.includes(word) && 'selected', connBadWords.includes(word) && 'shake']"
            @click="connToggle(word)"
          >
            {{ word }}
          </button>
        </div>
        <div class="conn-actions">
          <button class="conn-btn" @click="connShuffle">Stokk om</button>
          <button
            class="conn-btn conn-btn-primary"
            :disabled="connSelected.length !== 4"
            @click="connSubmit"
          >
            Send inn ({{ connSelected.length }}/4)
          </button>
        </div>
      </template>

      <div v-if="connGameState === 'won'" class="sp-result">
        <div class="sp-result-icon">🎉</div>
        <h3>Gratulerer!</h3>
        <p>Du løste Tankeburet! <strong>{{ connScore }} poeng</strong></p>
        <p v-if="connScoreSubmitted" class="score-saved">✓ Poeng registrert!</p>
        <p v-else-if="!isAuthenticated" class="score-saved" style="color:var(--ink-mute)">Logg inn for å registrere poeng</p>
      </div>

      <div v-if="connGameState === 'lost'" class="sp-result">
        <div class="sp-result-icon">😔</div>
        <h3>For mange feil</h3>
        <p>Her var fasiten:</p>
        <div class="conn-solved" style="margin-top: 12px">
          <div
            v-for="group in connPuzzle.groups"
            :key="group.category"
            :class="['conn-group-solved', `diff-${group.difficulty}`]"
          >
            <strong>{{ group.category }}</strong>
            <span>{{ group.words.join(' · ') }}</span>
          </div>
        </div>
        <p v-if="connScoreSubmitted" class="score-saved">✓ Poeng registrert!</p>
        <p v-else-if="!isAuthenticated" class="score-saved" style="color:var(--ink-mute)">Logg inn for å registrere poeng</p>
      </div>

      <div v-if="connGameState === 'completed'" class="sp-result">
        <div class="sp-result-icon">✅</div>
        <h3>Allerede fullført</h3>
        <p>Du har allerede spilt denne puzzlen. Velg en annen fra listen over.</p>
      </div>

      <!-- Creator form -->
      <div class="creator-wrap">
        <template v-if="isAuthenticated">
          <button class="creator-toggle" @click="creatorOpen = !creatorOpen">
            <span class="creator-toggle-icon">{{ creatorOpen ? '−' : '+' }}</span>
            Lag ny Tankeburet-puzzle
          </button>
        </template>
        <RouterLink v-else class="creator-login-hint" to="/login">Logg inn for å lage puzzle →</RouterLink>

        <form v-if="creatorOpen && isAuthenticated" class="creator-form" @submit.prevent="submitConnCreator">
          <div class="creator-groups">
            <div
              v-for="(group, gi) in connCreatorForm"
              :key="gi"
              :class="['creator-group', `diff-${gi}`]"
            >
              <div class="creator-group-label">
                Gruppe {{ gi + 1 }}
                <span class="creator-diff-badge">{{ ['Gul · Lett', 'Grønn · Medium', 'Blå · Vanskelig', 'Lilla · Ekspert'][gi] }}</span>
              </div>
              <input
                v-model="group.category"
                class="creator-input"
                :placeholder="`Kategori for gruppe ${gi + 1}…`"
                maxlength="60"
                required
              />
              <div class="creator-words-row">
                <input
                  v-for="wi in 4"
                  :key="wi"
                  v-model="group.words[wi - 1]"
                  class="creator-input creator-word-input"
                  :placeholder="`ORD ${wi}`"
                  maxlength="30"
                  required
                  @input="group.words[wi - 1] = group.words[wi - 1].toUpperCase()"
                />
              </div>
            </div>
          </div>

          <div class="creator-actions">
            <button type="button" class="conn-btn" @click="creatorOpen = false">Avbryt</button>
            <button type="submit" class="conn-btn conn-btn-primary" :disabled="connCreating">
              {{ connCreating ? 'Lagrer…' : 'Publiser puzzle' }}
            </button>
          </div>

          <p v-if="connCreatorError" class="creator-error">{{ connCreatorError }}</p>
        </form>
      </div>
    </div>

    <!-- ═══════════════ WORDLE ═══════════════ -->
    <div v-if="activeGame === 'wordle'" class="sp-panel">
      <div class="wrd-top">
        <div>
          <h2 class="section-title">Ordburet</h2>
          <p class="sp-sub">
            Gjett det 5-bokstavers ordet på 6 forsøk.
            <span v-if="wrdCreatedBy" class="puzzle-by">Laget av {{ wrdCreatedBy }}</span>
          </p>
        </div>
        <div class="wrd-legend">
          <div class="wrd-legend-item"><span class="wrd-sq correct" /> Riktig posisjon</div>
          <div class="wrd-legend-item"><span class="wrd-sq present" /> Feil posisjon</div>
          <div class="wrd-legend-item"><span class="wrd-sq absent" /> Ikke i ordet</div>
        </div>
      </div>

      <!-- Puzzle library -->
      <div class="puzzle-library">
        <span class="eyebrow" style="margin-bottom:8px;display:block;">Velg ord</span>
        <div class="puzzle-list">
          <button
            v-for="p in allWrdPuzzles"
            :key="p.id"
            :class="['puzzle-pick', selectedWrdId === p.id && 'active', completedWrdIds.includes(p.id) && 'done']"
            @click="loadWrdPuzzle(p)"
          >
            <span>{{ p.isDaily ? '⊙ Daglig' : (p.createdBy || 'Anonym') }}</span>
            <span class="puzzle-pick-date">{{ formatPuzzleDate(p.createdAt) }}</span>
            <span v-if="completedWrdIds.includes(p.id)" class="puzzle-pick-done">✓</span>
          </button>
        </div>
      </div>

      <div class="wrd-grid">
        <div v-for="rIdx in 6" :key="rIdx" class="wrd-row">
          <div
            v-for="cIdx in 5"
            :key="cIdx"
            :class="['wrd-cell', wrdCellClass(rIdx - 1, cIdx - 1)]"
          >
            {{ wrdCellLetter(rIdx - 1, cIdx - 1) }}
          </div>
        </div>
      </div>

      <div v-if="wrdMessage" class="wrd-message">{{ wrdMessage }}</div>

      <div v-if="wrdGameState === 'won'" class="sp-result">
        <div class="sp-result-icon">🟩</div>
        <h3>Imponerende!</h3>
        <p>Du gjettet <strong>{{ wrdTarget }}</strong> på {{ wrdGuesses.length }} forsøk! <strong>{{ wrdScore }} poeng</strong></p>
        <p v-if="wrdScoreSubmitted" class="score-saved">✓ Poeng registrert!</p>
        <p v-else-if="!isAuthenticated" class="score-saved" style="color:var(--ink-mute)">Logg inn for å registrere poeng</p>
      </div>

      <div v-if="wrdGameState === 'lost'" class="sp-result">
        <div class="sp-result-icon">😔</div>
        <h3>Bedre lykke neste gang!</h3>
        <p>Ordet var <strong>{{ wrdTarget }}</strong></p>
        <p v-if="wrdScoreSubmitted" class="score-saved">✓ Poeng registrert!</p>
        <p v-else-if="!isAuthenticated" class="score-saved" style="color:var(--ink-mute)">Logg inn for å registrere poeng</p>
      </div>

      <div v-if="wrdGameState === 'completed'" class="sp-result">
        <div class="sp-result-icon">✅</div>
        <h3>Allerede fullført</h3>
        <p>Du har allerede spilt dette ordet. Velg et annet fra listen over.</p>
      </div>

      <div class="wrd-keyboard">
        <div v-for="(row, rIdx) in keyboardRows" :key="rIdx" class="wrd-kb-row">
          <button
            v-for="key in row"
            :key="key"
            :class="['wrd-key', (key === 'ENTER' || key === 'DEL') && 'wrd-key-wide', wrdLetterState(key)]"
            @click="wrdKeyPress(key)"
          >
            {{ key }}
          </button>
        </div>
      </div>

      <!-- Creator form -->
      <div class="creator-wrap">
        <template v-if="isAuthenticated">
          <button class="creator-toggle" @click="creatorOpen = !creatorOpen">
            <span class="creator-toggle-icon">{{ creatorOpen ? '−' : '+' }}</span>
            Lag nytt Ordburet-ord
          </button>
        </template>
        <RouterLink v-else class="creator-login-hint" to="/login">Logg inn for å lage ord →</RouterLink>

        <form v-if="creatorOpen && isAuthenticated" class="creator-form" @submit.prevent="submitWrdCreator">
          <div class="creator-author-row">
            <label class="creator-label">Ord (5 bokstaver)</label>
            <input
              v-model="wrdCreatorWord"
              class="creator-input creator-word-big"
              placeholder="SPILL"
              maxlength="5"
              required
              @input="wrdCreatorWord = wrdCreatorWord.toUpperCase()"
            />
            <span class="creator-char-count" :class="wrdCreatorWord.length === 5 ? 'ok' : ''">
              {{ wrdCreatorWord.length }}/5
            </span>
          </div>

          <div class="creator-actions">
            <button type="button" class="conn-btn" @click="creatorOpen = false">Avbryt</button>
            <button type="submit" class="conn-btn conn-btn-primary" :disabled="wrdCreating || wrdCreatorWord.length !== 5">
              {{ wrdCreating ? 'Lagrer…' : 'Publiser ord' }}
            </button>
          </div>

          <p v-if="wrdCreatorError" class="creator-error">{{ wrdCreatorError }}</p>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { puzzleApi, scoreApi } from '../services/api'
import { useLiveDateInfo } from '../composables/useLiveDateInfo'
import { isAuthenticated, displayName } from '../services/authState'
import { subscribeToUpdates } from '../services/realtime'

const activeGame = ref('connections')
const { dateLabel, semesterLabel } = useLiveDateInfo({ intervalMs: 60000 })

// ——— CONNECTIONS ——————————————————————————————————————

const DEFAULT_CONN_PUZZLE = {
  createdBy: null,
  groups: [
    { category: 'Noe å drikke',          words: ['KAFFE', 'TE', 'SAFT', 'BRUS'],             difficulty: 0 },
    { category: 'Programmeringsspråk',    words: ['PYTHON', 'JAVA', 'SWIFT', 'RUBY'],         difficulty: 1 },
    { category: 'Programmeringsbegreper', words: ['LISTE', 'METODE', 'LØKKE', 'OBJEKT'],      difficulty: 2 },
    { category: 'Sider på Glassburet',    words: ['GALLERI', 'POENG', 'HJEM', 'QUOTES'],      difficulty: 3 },
  ],
}

const connPuzzle    = ref({ ...DEFAULT_CONN_PUZZLE })
const connShuffled  = ref([])
const connSelected  = ref([])
const connSolved    = ref([])
const connBadWords  = ref([])
const connMistakes  = ref(0)
const connGameState = ref('playing')
const connScoreSubmitted = ref(false)
const allConnPuzzles = ref([])
const selectedConnId = ref(null)

const completedConnIds = ref([])
const completedWrdIds  = ref([])

const connScore = computed(() => {
  if (connGameState.value === 'won') return Math.max(0, 1000 - (connMistakes.value * 125))
  return 0
})

function connInit() {
  const all = connPuzzle.value.groups.flatMap(g => g.words)
  connShuffled.value  = [...all].sort(() => Math.random() - 0.5)
  connSelected.value  = []
  connSolved.value    = []
  connBadWords.value  = []
  connMistakes.value  = 0
  connGameState.value = 'playing'
  connScoreSubmitted.value = false
}

function connShuffle() {
  connShuffled.value = [...connShuffled.value].sort(() => Math.random() - 0.5)
}

function connToggle(word) {
  if (connSelected.value.includes(word)) {
    connSelected.value = connSelected.value.filter(w => w !== word)
  } else if (connSelected.value.length < 4) {
    connSelected.value = [...connSelected.value, word]
  }
}

function connSubmit() {
  if (connSelected.value.length !== 4) return
  const sel = connSelected.value
  const match = connPuzzle.value.groups.find(g =>
    sel.every(w => g.words.includes(w)) &&
    !connSolved.value.some(s => s.category === g.category)
  )
  if (match) {
    connSolved.value    = [...connSolved.value, match]
    connShuffled.value  = connShuffled.value.filter(w => !match.words.includes(w))
    connSelected.value  = []
    if (connSolved.value.length === 4) {
      connGameState.value = 'won'
      if (isAuthenticated.value && !connScoreSubmitted.value) submitConnScore()
    }
  } else {
    connBadWords.value = [...sel]
    connMistakes.value++
    setTimeout(() => {
      connBadWords.value = []
      connSelected.value = []
      if (connMistakes.value >= 4) {
        connGameState.value = 'lost'
        if (isAuthenticated.value && !connScoreSubmitted.value) submitConnScore()
      }
    }, 600)
  }
}

function applyConnectionsPuzzle(data) {
  connPuzzle.value = {
    createdBy: data.createdBy,
    groups: data.groups.map(g => ({
      category:   g.category,
      words:      g.words,
      difficulty: g.difficulty,
    })),
  }
  connInit()
}

function loadConnPuzzle(p) {
  selectedConnId.value = p.id
  if (completedConnIds.value.includes(p.id)) {
    connGameState.value = 'completed'
    return
  }
  applyConnectionsPuzzle(p)
}

async function submitConnScore() {
  if (!displayName.value || !displayName.value.trim()) {
    return
  }
  try {
    await scoreApi.submit({
      memberName: displayName.value,
      gameName: 'CONNECTIONS',
      scoreValue: connScore.value,
      gameDate: new Date().toISOString().split('T')[0],
      puzzleId: selectedConnId.value,
    })
    connScoreSubmitted.value = true
    const completed = await scoreApi.getCompleted(displayName.value, 'CONNECTIONS')
    completedConnIds.value = completed
    if (selectedConnId.value && !completedConnIds.value.includes(selectedConnId.value)) {
      completedConnIds.value = [...completedConnIds.value, selectedConnId.value]
    }
  } catch {}
}

const formatPuzzleDate = (dateString) => {
  if (!dateString) return ''
  const d = new Date(dateString)
  return `${d.getDate()}.${d.getMonth() + 1}`
}

// ——— WORDLE ————————————————————————————————————————————

const wrdTarget    = ref('')
const wrdCreatedBy = ref(null)
const wrdGuesses   = ref([])
const wrdCurrent   = ref('')
const wrdGameState = ref('playing')
const wrdMessage   = ref('')
const wrdScoreSubmitted = ref(false)
const allWrdPuzzles = ref([])
const selectedWrdId = ref(null)

const wrdScore = computed(() => {
  if (wrdGameState.value === 'won') return Math.max(0, 1000 - ((wrdGuesses.value.length - 1) * 125))
  return 0
})

const keyboardRows = [
  ['Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'Å'],
  ['A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Ø', 'Æ'],
  ['ENTER', 'Z', 'X', 'C', 'V', 'B', 'N', 'M', 'DEL'],
]

function wrdLetterState(letter) {
  if (letter === 'ENTER' || letter === 'DEL') return ''
  let best = ''
  for (const guess of wrdGuesses.value) {
    for (let i = 0; i < guess.length; i++) {
      if (guess[i] === letter) {
        if (wrdTarget.value[i] === letter) { best = 'correct'; break }
        else if (wrdTarget.value.includes(letter) && best !== 'correct') best = 'present'
        else if (best === '') best = 'absent'
      }
    }
    if (best === 'correct') break
  }
  return best
}

function wrdCellLetter(row, col) {
  if (row < wrdGuesses.value.length) return wrdGuesses.value[row][col] ?? ''
  if (row === wrdGuesses.value.length && wrdGameState.value === 'playing') return wrdCurrent.value[col] ?? ''
  return ''
}

function wrdCellClass(row, col) {
  if (row < wrdGuesses.value.length) {
    const letter = wrdGuesses.value[row][col]
    if (!letter) return ''
    if (wrdTarget.value[col] === letter) return 'correct'
    if (wrdTarget.value.includes(letter)) return 'present'
    return 'absent'
  }
  if (row === wrdGuesses.value.length && wrdGameState.value === 'playing' && wrdCurrent.value[col]) return 'filled'
  return ''
}

function wrdKeyPress(key) {
  if (wrdGameState.value !== 'playing') return
  if (key === 'DEL') {
    wrdCurrent.value = wrdCurrent.value.slice(0, -1)
  } else if (key === 'ENTER') {
    if (wrdCurrent.value.length < 5) {
      wrdMessage.value = 'For kort – skriv 5 bokstaver'
      setTimeout(() => { wrdMessage.value = '' }, 1500)
      return
    }
    const guess = wrdCurrent.value
    wrdGuesses.value = [...wrdGuesses.value, guess]
    wrdCurrent.value = ''
    if (guess === wrdTarget.value) {
      wrdGameState.value = 'won'
      if (isAuthenticated.value && !wrdScoreSubmitted.value) submitWrdScore()
    } else if (wrdGuesses.value.length >= 6) {
      wrdGameState.value = 'lost'
      if (isAuthenticated.value && !wrdScoreSubmitted.value) submitWrdScore()
    }
  } else if (/^[A-ZÆØÅ]$/.test(key) && wrdCurrent.value.length < 5) {
    wrdCurrent.value += key
  }
}

function loadWrdPuzzle(p) {
  selectedWrdId.value = p.id
  if (completedWrdIds.value.includes(p.id)) {
    wrdGameState.value = 'completed'
    return
  }
  wrdTarget.value    = p.word
  wrdCreatedBy.value = p.createdBy
  wrdGuesses.value   = []
  wrdCurrent.value   = ''
  wrdGameState.value = 'playing'
  wrdMessage.value   = ''
  wrdScoreSubmitted.value = false
}

async function submitWrdScore() {
  if (!displayName.value || !displayName.value.trim()) {
    return
  }
  try {
    await scoreApi.submit({
      memberName: displayName.value,
      gameName: 'WORDLE',
      scoreValue: wrdScore.value,
      gameDate: new Date().toISOString().split('T')[0],
      puzzleId: selectedWrdId.value,
    })
    wrdScoreSubmitted.value = true
    const completed = await scoreApi.getCompleted(displayName.value, 'WORDLE')
    completedWrdIds.value = completed
    if (selectedWrdId.value && !completedWrdIds.value.includes(selectedWrdId.value)) {
      completedWrdIds.value = [...completedWrdIds.value, selectedWrdId.value]
    }
  } catch {}
}

// ——— CREATOR SHARED STATE ——————————————————————————————

const creatorOpen = ref(false)

// ——— CONNECTIONS CREATOR ———————————————————————————————

const connCreatorForm = ref([
  { category: '', words: ['', '', '', ''] },
  { category: '', words: ['', '', '', ''] },
  { category: '', words: ['', '', '', ''] },
  { category: '', words: ['', '', '', ''] },
])
const connCreating     = ref(false)
const connCreatorError = ref('')

async function submitConnCreator() {
  connCreatorError.value = ''
  for (const g of connCreatorForm.value) {
    if (!g.category.trim()) { connCreatorError.value = 'Alle kategorier må fylles ut.'; return }
    if (g.words.some(w => !w.trim())) { connCreatorError.value = 'Alle 16 ord må fylles ut.'; return }
  }
  connCreating.value = true
  try {
    const data = await puzzleApi.createConnections({
      createdBy: displayName.value || 'Anonym',
      groups: connCreatorForm.value.map(g => ({
        category: g.category.trim(),
        words:    g.words.map(w => w.trim().toUpperCase()),
      })),
    })
    allConnPuzzles.value.unshift(data)
    selectedConnId.value = data.id
    applyConnectionsPuzzle(data)
    creatorOpen.value = false
    connCreatorForm.value = [
      { category: '', words: ['', '', '', ''] },
      { category: '', words: ['', '', '', ''] },
      { category: '', words: ['', '', '', ''] },
      { category: '', words: ['', '', '', ''] },
    ]
  } catch {
    connCreatorError.value = 'Noe gikk galt. Prøv igjen.'
  } finally {
    connCreating.value = false
  }
}

// ——— WORDLE CREATOR ————————————————————————————————————

const wrdCreatorWord  = ref('')
const wrdCreating     = ref(false)
const wrdCreatorError = ref('')

async function submitWrdCreator() {
  wrdCreatorError.value = ''
  const word = wrdCreatorWord.value.trim().toUpperCase()
  if (word.length !== 5) { wrdCreatorError.value = 'Ordet må ha nøyaktig 5 bokstaver.'; return }
  wrdCreating.value = true
  try {
    const data = await puzzleApi.createWordle({
      createdBy: displayName.value || 'Anonym',
      word,
    })
    allWrdPuzzles.value.unshift(data)
    loadWrdPuzzle(data)
    creatorOpen.value  = false
    wrdCreatorWord.value = ''
  } catch {
    wrdCreatorError.value = 'Noe gikk galt. Prøv igjen.'
  } finally {
    wrdCreating.value = false
  }
}

// ——— KEYBOARD HANDLER ——————————————————————————————————

function handleKeydown(e) {
  if (activeGame.value !== 'wordle' || creatorOpen.value) return
  const k = e.key
  if (k === 'Backspace')                { e.preventDefault(); wrdKeyPress('DEL') }
  else if (k === 'Enter')               { e.preventDefault(); wrdKeyPress('ENTER') }
  else if (/^[a-zA-ZæøåÆØÅ]$/.test(k)) { e.preventDefault(); wrdKeyPress(k.toUpperCase()) }
}

// ——— INIT ———————————————————————————————————————————————

async function loadCompletedPuzzles() {
  if (!isAuthenticated.value || !displayName.value) return
  const [connComp, wrdComp] = await Promise.allSettled([
    scoreApi.getCompleted(displayName.value, 'CONNECTIONS'),
    scoreApi.getCompleted(displayName.value, 'WORDLE'),
  ])
  if (connComp.status === 'fulfilled') completedConnIds.value = connComp.value
  if (wrdComp.status === 'fulfilled')  completedWrdIds.value  = wrdComp.value
}

async function loadPuzzleLibrary({ applyDaily = false } = {}) {
  const [dailyConnResult, dailyWrdResult, allConnResult, allWrdResult] = await Promise.allSettled([
    puzzleApi.getDailyConnections(),
    puzzleApi.getDailyWordle(),
    puzzleApi.getAllConnections(),
    puzzleApi.getAllWordle(),
  ])

  if (allConnResult.status === 'fulfilled') allConnPuzzles.value = allConnResult.value
  if (allWrdResult.status === 'fulfilled')  allWrdPuzzles.value  = allWrdResult.value

  // Ensure daily puzzles appear in the library (race: getAll may resolve before daily is created)
  if (dailyConnResult.status === 'fulfilled' && dailyConnResult.value?.id) {
    if (!allConnPuzzles.value.some(p => p.id === dailyConnResult.value.id)) {
      allConnPuzzles.value = [dailyConnResult.value, ...allConnPuzzles.value]
    }
  }
  if (dailyWrdResult.status === 'fulfilled' && dailyWrdResult.value?.id) {
    if (!allWrdPuzzles.value.some(p => p.id === dailyWrdResult.value.id)) {
      allWrdPuzzles.value = [dailyWrdResult.value, ...allWrdPuzzles.value]
    }
  }

  if (applyDaily && dailyConnResult.status === 'fulfilled' && dailyConnResult.value?.groups) {
    loadConnPuzzle(dailyConnResult.value)
  }
  if (applyDaily && dailyWrdResult.status === 'fulfilled' && dailyWrdResult.value?.word) {
    loadWrdPuzzle(dailyWrdResult.value)
  }
}

let unsubscribePuzzles = null
let unsubscribeScores = null

onMounted(async () => {
  connInit()
  window.addEventListener('keydown', handleKeydown)
  await loadCompletedPuzzles()
  await loadPuzzleLibrary({ applyDaily: true })
  unsubscribePuzzles = subscribeToUpdates('puzzles', () => loadPuzzleLibrary())
  unsubscribeScores = subscribeToUpdates('scores', loadCompletedPuzzles)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  if (unsubscribePuzzles) unsubscribePuzzles()
  if (unsubscribeScores) unsubscribeScores()
})
</script>

<style scoped>
.sp-tabs {
  display: flex;
  border-bottom: 1px solid var(--line);
  margin-bottom: 40px;
}
.sp-tab {
  padding: 14px 28px;
  font-family: var(--mono);
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--ink-mute);
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: all 0.2s ease;
}
.sp-tab:hover { color: var(--ink); }
.sp-tab.active { color: var(--ink); border-bottom-color: var(--accent-2); }

.sp-panel { max-width: 700px; margin: 0 auto; }
.sp-sub { color: var(--ink-mute); font-size: 13px; margin-top: 8px; }
.puzzle-by { font-family: var(--mono); font-size: 11px; letter-spacing: 0.06em; color: var(--accent-3); margin-left: 8px; }

/* ── Puzzle library ──────────────────────────── */
.puzzle-library {
  margin-bottom: 24px;
  padding: 16px;
  background: var(--paper);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
}
.puzzle-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.puzzle-pick {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 8px 14px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
  font-family: var(--mono);
  font-size: 11px;
  color: var(--ink-mute);
  transition: all 0.15s ease;
  text-align: left;
}
.puzzle-pick:hover { border-color: var(--ink); color: var(--ink); }
.puzzle-pick.active { border-color: var(--accent); color: var(--accent); background: var(--bg-soft); }
.puzzle-pick.done { opacity: 0.55; }
.puzzle-pick-date { font-size: 10px; opacity: 0.6; margin-top: 2px; }
.puzzle-pick-done { font-size: 10px; color: var(--accent); margin-top: 2px; }

/* ── Connections ─────────────────────────────── */
.conn-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
}
.conn-mistakes-box { text-align: right; }
.mistake-dots { display: flex; gap: 8px; justify-content: flex-end; margin-top: 10px; }
.mdot {
  width: 18px; height: 18px;
  border-radius: 50%;
  background: var(--bg-soft);
  border: 1px solid var(--line-soft);
  transition: background 0.3s ease;
}
.mdot.active { background: var(--accent); border-color: var(--accent); }

.conn-solved { display: flex; flex-direction: column; gap: 8px; margin-bottom: 8px; }
.conn-group-solved {
  padding: 18px 24px;
  border-radius: var(--radius);
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 6px;
  animation: fadeUp 0.4s ease;
}
.conn-group-solved strong { font-family: var(--mono); font-size: 11px; text-transform: uppercase; letter-spacing: 0.14em; }
.conn-group-solved span { font-size: 14px; font-weight: 600; }

.diff-0 { background: var(--accent-3); color: var(--ink); }
.diff-1 { background: var(--accent); color: #f4f1ea; }
.diff-2 { background: var(--accent-2); color: #f4f1ea; }
.diff-3 { background: var(--ink); color: var(--bg); }

.conn-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 16px;
}
.conn-word {
  padding: 0 8px;
  min-height: 76px;
  background: var(--paper);
  border: 1.5px solid var(--line-soft);
  border-radius: var(--radius);
  font-family: var(--sans);
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  text-align: center;
  cursor: pointer;
  transition: all 0.15s ease;
  display: flex; align-items: center; justify-content: center;
}
.conn-word:hover { border-color: var(--ink); background: var(--bg); }
.conn-word.selected { background: var(--ink-soft); color: var(--bg); border-color: var(--ink); }

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20%       { transform: translateX(-5px); }
  40%       { transform: translateX(5px); }
  60%       { transform: translateX(-5px); }
  80%       { transform: translateX(5px); }
}
.conn-word.shake { animation: shake 0.55s ease; }

.conn-actions { display: flex; gap: 12px; justify-content: center; margin-top: 8px; }
.conn-btn {
  padding: 12px 24px;
  border: 1.5px solid var(--line-soft);
  border-radius: 999px;
  font-family: var(--mono);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--ink-soft);
  transition: all 0.2s ease;
}
.conn-btn:hover { border-color: var(--ink); color: var(--ink); }
.conn-btn-primary { background: var(--accent); color: #f4f1ea; border-color: var(--accent); }
.conn-btn-primary:hover:not(:disabled) { background: var(--accent-2); border-color: var(--accent-2); color: #f4f1ea; }
.conn-btn-primary:disabled { opacity: 0.38; cursor: not-allowed; }

/* ── Wordle ──────────────────────────────────── */
.wrd-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
}
.wrd-legend { display: flex; flex-direction: column; gap: 7px; font-family: var(--mono); font-size: 11px; color: var(--ink-mute); }
.wrd-legend-item { display: flex; align-items: center; gap: 8px; }
.wrd-sq { width: 16px; height: 16px; border-radius: 2px; display: inline-block; flex-shrink: 0; }
.wrd-sq.correct { background: var(--accent); }
.wrd-sq.present { background: var(--accent-3); }
.wrd-sq.absent  { background: var(--ink-mute); }

.wrd-grid { display: flex; flex-direction: column; gap: 6px; margin-bottom: 20px; align-items: center; }
.wrd-row  { display: flex; gap: 6px; }
.wrd-cell {
  width: 64px; height: 64px;
  border: 1.5px solid var(--line-soft);
  display: flex; align-items: center; justify-content: center;
  font-family: var(--sans);
  font-size: 26px;
  font-weight: 800;
  text-transform: uppercase;
  border-radius: var(--radius);
  transition: background 0.25s ease, border-color 0.25s ease, color 0.25s ease;
}
.wrd-cell.filled  { border-color: var(--ink-soft); }
.wrd-cell.correct { background: var(--accent);   color: #f4f1ea;    border-color: var(--accent); }
.wrd-cell.present { background: var(--accent-3); color: var(--ink); border-color: var(--accent-3); }
.wrd-cell.absent  { background: var(--ink-mute); color: var(--bg);  border-color: var(--ink-mute); }

.wrd-message {
  text-align: center;
  font-family: var(--mono);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--accent-2);
  margin-bottom: 12px;
}

.wrd-keyboard { display: flex; flex-direction: column; gap: 6px; align-items: center; margin-top: 8px; }
.wrd-kb-row   { display: flex; gap: 5px; }
.wrd-key {
  min-width: 40px; height: 56px;
  padding: 0 4px;
  background: var(--paper);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
  font-family: var(--mono);
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: all 0.15s ease;
  user-select: none;
}
.wrd-key:hover      { background: var(--bg-soft); border-color: var(--ink-soft); }
.wrd-key-wide       { min-width: 58px; font-size: 10px; letter-spacing: 0.04em; }
.wrd-key.correct    { background: var(--accent);   color: #f4f1ea;    border-color: var(--accent); }
.wrd-key.present    { background: var(--accent-3); color: var(--ink); border-color: var(--accent-3); }
.wrd-key.absent     { background: var(--ink-mute); color: var(--bg);  border-color: var(--ink-mute); }

/* ── Shared result panel ─────────────────────── */
.sp-result {
  text-align: center;
  padding: 48px 32px;
  background: var(--paper);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
  margin: 16px 0;
  animation: fadeUp 0.4s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.sp-result-icon { font-size: 52px; }
.sp-result h3 { font-family: var(--serif); font-size: 36px; font-weight: 400; letter-spacing: -0.02em; }
.sp-result p  { color: var(--ink-soft); font-size: 15px; }
.sp-result strong { color: var(--ink); }
.score-saved { font-family: var(--mono); font-size: 12px; color: var(--accent); text-transform: uppercase; letter-spacing: 0.08em; }

/* ── Creator ─────────────────────────────────── */
.creator-wrap {
  margin-top: 48px;
  border-top: 1px solid var(--line-soft);
  padding-top: 24px;
}
.creator-login-hint {
  font-family: var(--mono);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--ink-mute);
  transition: color 0.2s ease;
}
.creator-login-hint:hover { color: var(--ink); }

.creator-toggle {
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: var(--mono);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--ink-mute);
  transition: color 0.2s ease;
}
.creator-toggle:hover { color: var(--ink); }
.creator-toggle-icon {
  width: 24px; height: 24px;
  border: 1px solid var(--line-soft);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px;
  font-weight: 400;
  transition: all 0.2s ease;
}
.creator-toggle:hover .creator-toggle-icon { border-color: var(--ink); color: var(--ink); }

.creator-form {
  margin-top: 28px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  animation: fadeUp 0.3s ease;
}

.creator-label {
  font-family: var(--mono);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--ink-mute);
  display: block;
  margin-bottom: 6px;
}
.creator-author-row { display: flex; flex-direction: column; max-width: 320px; position: relative; }
.creator-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--line-soft);
  background: var(--bg);
  font-family: var(--sans);
  font-size: 14px;
  border-radius: var(--radius);
  color: var(--ink);
  transition: border-color 0.2s ease;
}
.creator-input:focus { outline: none; border-color: var(--accent); }
.creator-word-big { font-size: 22px; font-weight: 700; letter-spacing: 0.1em; max-width: 160px; text-transform: uppercase; }
.creator-char-count {
  font-family: var(--mono);
  font-size: 11px;
  color: var(--ink-mute);
  margin-top: 4px;
}
.creator-char-count.ok { color: var(--accent); }

.creator-groups { display: flex; flex-direction: column; gap: 16px; }
.creator-group {
  padding: 20px;
  border: 1.5px solid var(--line-soft);
  border-radius: var(--radius);
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.creator-group.diff-0 { border-color: var(--accent-3); }
.creator-group.diff-1 { border-color: var(--accent); }
.creator-group.diff-2 { border-color: var(--accent-2); }
.creator-group.diff-3 { border-color: var(--ink-soft); }

.creator-group-label {
  font-family: var(--mono);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--ink-mute);
  display: flex;
  align-items: center;
  gap: 10px;
}
.creator-diff-badge {
  background: var(--bg-soft);
  border: 1px solid var(--line-soft);
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 10px;
}

.creator-words-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}
.creator-word-input { text-transform: uppercase; font-weight: 700; letter-spacing: 0.06em; text-align: center; }

.creator-actions { display: flex; gap: 12px; }
.creator-error {
  font-family: var(--mono);
  font-size: 12px;
  color: var(--accent-2);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

/* ── Responsive ──────────────────────────────── */
@media (max-width: 600px) {
  .conn-word  { font-size: 12px; min-height: 60px; }
  .wrd-cell   { width: 52px; height: 52px; font-size: 20px; }
  .wrd-key    { min-width: 30px; height: 46px; font-size: 11px; }
  .wrd-key-wide { min-width: 46px; font-size: 9px; }
  .wrd-top, .conn-top { flex-direction: column; gap: 16px; }
  .creator-words-row { grid-template-columns: repeat(2, 1fr); }
  .puzzle-list { flex-direction: column; }
}
</style>
