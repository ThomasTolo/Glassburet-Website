<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">Daglige spill · 6 tilgjengelig</span>
        <h1 class="display" style="font-size: clamp(40px, 6vw, 84px); margin: 12px 0 16px;">
          Dagens <em>spill</em>.
        </h1>
        <p class="lede">Daglige utfordringer for deg og vennegjengen. Hvem er egentlig skarpest?</p>
      </div>
      <div class="page-header-meta">
        {{ dateLabel }}<br />
        {{ semesterLabel }}<br />
        6 SPILL I DAG
      </div>
    </div>

    <div class="sp-tabs">
      <button :class="['sp-tab', activeGame === 'connections' && 'active']" @click="activeGame = 'connections'; creatorOpen = false">
        Tankeburet
      </button>
      <button :class="['sp-tab', activeGame === 'wordle' && 'active']" @click="activeGame = 'wordle'; creatorOpen = false">
        Ordburet
      </button>
      <button
        v-for="game in nativeGames"
        :key="game.id"
        :class="['sp-tab', activeGame === game.id && 'active']"
        @click="activeGame = game.id; creatorOpen = false"
      >
        {{ game.name }}
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
          <a class="source-button" href="https://www.nytimes.com/games/connections" target="_blank" rel="noopener noreferrer">Inspirasjon</a>
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
                <span class="creator-diff-badge">{{ ['Gul · Lett', 'Grønn · Medium', 'Rød · Vanskelig', 'Svart · Ekspert'][gi] }}</span>
              </div>
              <input
                v-model="group.category"So
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
          <a class="source-button" href="https://www.nytimes.com/games/wordle/index.html" target="_blank" rel="noopener noreferrer">Inspirasjon</a>
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

    <!-- ═══════════════ NATIVE DAILY GAMES ═══════════════ -->
    <div v-if="activeNativeGame" class="sp-panel">
      <div class="native-game-panel">
        <div class="native-game-icon">{{ activeNativeGame.icon }}</div>
        <h2 class="section-title">{{ activeNativeGame.name }}</h2>
        <p class="sp-sub">
          {{ activeNativeGame.description }}
        </p>
        <a class="source-button" :href="activeNativeGame.url" target="_blank" rel="noopener noreferrer">Inspirasjon</a>

        <div class="puzzle-library native-library">
          <span class="eyebrow" style="margin-bottom:8px;display:block;">Velg spill</span>
          <div class="puzzle-list">
            <button
              v-for="p in activeNativePuzzles"
              :key="p.id"
              :class="['puzzle-pick', selectedNativeIds[activeGame] === p.id && 'active', activeNativeCompletedIds.includes(p.id) && 'done']"
              @click="loadNativePuzzle(activeGame, p)"
            >
              <span>{{ p.isDaily ? '⊙ Daglig' : (p.createdBy || 'Anonym') }}</span>
              <span class="puzzle-pick-date">{{ formatPuzzleDate(p.createdAt) }}</span>
              <span v-if="activeNativeCompletedIds.includes(p.id)" class="puzzle-pick-done">✓</span>
            </button>
          </div>
        </div>

        <div v-if="activeNativeGameState === 'completed'" class="sp-result compact-result">
          <div class="sp-result-icon">✅</div>
          <h3>Allerede fullført</h3>
          <p>Du har allerede spilt denne. Velg en annen fra listen over.</p>
        </div>

        <div v-if="activeGame === 'merburet'" class="native-game-body">
          <template v-if="activeNativeGameState !== 'completed' && !merDone">
            <span class="eyebrow">Runde {{ merIndex + 1 }} / {{ merRounds.length }}</span>
            <h3 class="native-prompt">{{ merCurrent.metric }}</h3>
            <div class="choice-grid">
              <button class="choice-card" @click="merChoose('left')">{{ merCurrent.left }}</button>
              <button class="choice-card" @click="merChoose('right')">{{ merCurrent.right }}</button>
            </div>
            <p v-if="merMessage" class="wrd-message">{{ merMessage }}</p>
          </template>
          <div v-if="activeNativeGameState !== 'completed' && merDone" class="sp-result compact-result">
            <div class="sp-result-icon">↕</div>
            <h3>{{ merCorrect }} av {{ merRounds.length }} riktig</h3>
            <p><strong>{{ merScore }} poeng</strong></p>
            <p v-if="nativeSubmitted.merburet" class="score-saved">✓ Poeng registrert!</p>
            <p v-else-if="!isAuthenticated" class="score-saved" style="color:var(--ink-mute)">Logg inn for å registrere poeng</p>
          </div>
        </div>

        <div v-if="activeGame === 'latburet'" class="native-game-body">
          <template v-if="activeNativeGameState !== 'completed' && !songDone">
            <div class="clue-list">
              <span v-for="clue in songPuzzle.clues.slice(0, songGuesses.length + 1)" :key="clue" class="clue-pill">{{ clue }}</span>
            </div>
            <form class="native-form" @submit.prevent="songSubmit">
              <input v-model="songGuess" class="creator-input native-input" placeholder="Skriv tittel" />
              <button class="conn-btn conn-btn-primary" type="submit">Gjett</button>
            </form>
            <p v-if="songMessage" class="wrd-message">{{ songMessage }}</p>
          </template>
          <div v-if="activeNativeGameState !== 'completed' && songDone" class="sp-result compact-result">
            <div class="sp-result-icon">♪</div>
            <h3>{{ songPuzzle.answer }}</h3>
            <p><strong>{{ songScore }} poeng</strong></p>
            <p v-if="nativeSubmitted.latburet" class="score-saved">✓ Poeng registrert!</p>
            <p v-else-if="!isAuthenticated" class="score-saved" style="color:var(--ink-mute)">Logg inn for å registrere poeng</p>
          </div>
        </div>

        <div v-if="activeGame === 'kryssburet'" class="native-game-body">
          <form v-if="activeNativeGameState !== 'completed' && !crossDone" class="cross-form" @submit.prevent="crossSubmit">
            <label v-for="(clue, idx) in crossPuzzle.clues" :key="clue.prompt" class="cross-row">
              <span>{{ idx + 1 }}. {{ clue.prompt }}</span>
              <input v-model="crossInputs[idx]" class="creator-input native-input" />
            </label>
            <button class="conn-btn conn-btn-primary" type="submit">Send inn</button>
          </form>
          <div v-if="activeNativeGameState !== 'completed' && crossDone" class="sp-result compact-result">
            <div class="sp-result-icon">+</div>
            <h3>{{ crossCorrect }} av {{ crossPuzzle.clues.length }} riktig</h3>
            <p><strong>{{ crossScore }} poeng</strong></p>
            <p v-if="nativeSubmitted.kryssburet" class="score-saved">✓ Poeng registrert!</p>
            <p v-else-if="!isAuthenticated" class="score-saved" style="color:var(--ink-mute)">Logg inn for å registrere poeng</p>
          </div>
        </div>

        <div v-if="activeGame === 'tidsburet'" class="native-game-body">
          <template v-if="activeNativeGameState !== 'completed' && !timeDone">
            <h3 class="native-prompt">{{ timePuzzle.prompt }}</h3>
            <div class="time-grid">
              <label class="creator-label">
                År
                <input v-model.number="timeYear" class="creator-input native-input" type="number" min="1800" max="2026" />
              </label>
              <label class="creator-label">
                Sted
                <select v-model="timePlace" class="creator-input native-input">
                  <option value="" disabled>Velg sted</option>
                  <option v-for="place in timePuzzle.options" :key="place" :value="place">{{ place }}</option>
                </select>
              </label>
            </div>
            <button class="conn-btn conn-btn-primary" :disabled="!timePlace" @click="timeSubmit">Send inn</button>
          </template>
          <div v-if="activeNativeGameState !== 'completed' && timeDone" class="sp-result compact-result">
            <div class="sp-result-icon">◷</div>
            <h3>{{ timePuzzle.year }} · {{ timePuzzle.place }}</h3>
            <p><strong>{{ timeScore }} poeng</strong></p>
            <p v-if="nativeSubmitted.tidsburet" class="score-saved">✓ Poeng registrert!</p>
            <p v-else-if="!isAuthenticated" class="score-saved" style="color:var(--ink-mute)">Logg inn for å registrere poeng</p>
          </div>
        </div>

        <div class="creator-wrap native-creator">
          <template v-if="isAuthenticated">
            <button class="creator-toggle" @click="creatorOpen = !creatorOpen">
              <span class="creator-toggle-icon">{{ creatorOpen ? '−' : '+' }}</span>
              Lag ny {{ activeNativeGame.name }}
            </button>
          </template>
          <RouterLink v-else class="creator-login-hint" to="/login">Logg inn for å lage spill →</RouterLink>

          <form v-if="creatorOpen && isAuthenticated" class="creator-form" @submit.prevent="submitNativeCreator">
            <template v-if="activeGame === 'merburet'">
              <div v-for="(round, idx) in merCreatorRounds" :key="idx" class="creator-group">
                <label class="creator-label">Duell {{ idx + 1 }}</label>
                <input v-model="round.metric" class="creator-input" placeholder="Spørsmål" required />
                <div class="creator-words-row">
                  <input v-model="round.left" class="creator-input" placeholder="Venstre" required />
                  <input v-model="round.right" class="creator-input" placeholder="Høyre" required />
                </div>
                <select v-model="round.answer" class="creator-input" required>
                  <option value="left">Venstre er riktig</option>
                  <option value="right">Høyre er riktig</option>
                </select>
              </div>
            </template>

            <template v-if="activeGame === 'latburet'">
              <input v-model="songCreator.answer" class="creator-input" placeholder="Tittel" required @input="songCreator.answer = songCreator.answer.toUpperCase()" />
              <input v-for="idx in 3" :key="idx" v-model="songCreator.clues[idx - 1]" class="creator-input" :placeholder="`Hint ${idx}`" required />
            </template>

            <template v-if="activeGame === 'kryssburet'">
              <div v-for="(clue, idx) in crossCreator.clues" :key="idx" class="creator-group">
                <label class="creator-label">Hint {{ idx + 1 }}</label>
                <input v-model="clue.prompt" class="creator-input" placeholder="Hint" required />
                <input v-model="clue.answer" class="creator-input" placeholder="Svar" required @input="clue.answer = clue.answer.toUpperCase()" />
              </div>
            </template>

            <template v-if="activeGame === 'tidsburet'">
              <input v-model="timeCreator.prompt" class="creator-input" placeholder="Historisk spor" required />
              <div class="creator-words-row">
                <input v-model.number="timeCreator.year" class="creator-input" type="number" placeholder="År" required />
                <input v-model="timeCreator.place" class="creator-input" placeholder="Riktig sted" required />
              </div>
              <input v-model="timeCreator.optionsText" class="creator-input" placeholder="Valg, separert med komma" required />
            </template>

            <div class="creator-actions">
              <button type="button" class="conn-btn" @click="creatorOpen = false">Avbryt</button>
              <button type="submit" class="conn-btn conn-btn-primary" :disabled="nativeCreating">
                {{ nativeCreating ? 'Lagrer…' : 'Publiser spill' }}
              </button>
            </div>
            <p v-if="nativeCreatorError" class="creator-error">{{ nativeCreatorError }}</p>
          </form>
        </div>
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
const nativeGames = [
  {
    id: 'merburet',
    name: 'Merburet',
    description: 'Gjett hva som har størst tallverdi i dagens fem dueller.',
    gameName: 'MORE_OR_LESS',
    url: 'https://lessgames.com/moreless',
    icon: '↕',
  },
  {
    id: 'latburet',
    name: 'Låtburet',
    description: 'Finn tittelen fra korte, trygge musikkhint.',
    gameName: 'SONGLESS',
    url: 'https://lessgames.com/songless',
    icon: '♪',
  },
  {
    id: 'kryssburet',
    name: 'Kryssburet',
    description: 'Løs fire musikkord fra dagens krysshint.',
    gameName: 'CROSSTUNES',
    url: 'https://crosstune.io',
    icon: '+',
  },
  {
    id: 'tidsburet',
    name: 'Tidsburet',
    description: 'Gjett år og sted ut fra historiske spor.',
    gameName: 'TIMEGUESSR',
    url: 'https://timeguessr.com',
    icon: '◷',
  },
]
const activeNativeGame = computed(() => nativeGames.find(game => game.id === activeGame.value))
const nativePuzzles = ref({ merburet: [], latburet: [], kryssburet: [], tidsburet: [] })
const selectedNativeIds = ref({})
const completedNativeIds = ref({ merburet: [], latburet: [], kryssburet: [], tidsburet: [] })
const nativeSubmitted = ref({})
const activeNativePuzzles = computed(() => nativePuzzles.value[activeGame.value] ?? [])
const activeNativeCompletedIds = computed(() => completedNativeIds.value[activeGame.value] ?? [])
const activeNativeGameState = computed(() => {
  const id = selectedNativeIds.value[activeGame.value]
  return id && activeNativeCompletedIds.value.includes(id) ? 'completed' : 'playing'
})

async function submitNativeScore(gameId, scoreValue) {
  const game = nativeGames.find(g => g.id === gameId)
  if (!game || !displayName.value || !displayName.value.trim()) return
  try {
    await scoreApi.submit({
      memberName: displayName.value,
      gameName: game.gameName,
      scoreValue,
      gameDate: new Date().toISOString().split('T')[0],
      puzzleId: selectedNativeIds.value[gameId],
    })
    nativeSubmitted.value = { ...nativeSubmitted.value, [gameId]: true }
    const completed = await scoreApi.getCompleted(displayName.value, game.gameName)
    completedNativeIds.value = { ...completedNativeIds.value, [gameId]: completed }
    if (selectedNativeIds.value[gameId] && !completed.includes(selectedNativeIds.value[gameId])) {
      completedNativeIds.value = {
        ...completedNativeIds.value,
        [gameId]: [...completed, selectedNativeIds.value[gameId]],
      }
    }
  } catch {}
}

function parseNativePayload(puzzle) {
  try {
    return JSON.parse(puzzle.payloadJson)
  } catch {
    return null
  }
}

function loadNativePuzzle(gameId, puzzle) {
  selectedNativeIds.value = { ...selectedNativeIds.value, [gameId]: puzzle.id }
  nativeSubmitted.value = { ...nativeSubmitted.value, [gameId]: false }
  if ((completedNativeIds.value[gameId] ?? []).includes(puzzle.id)) return
  const payload = parseNativePayload(puzzle)
  if (!payload) return
  if (gameId === 'merburet') applyMerPuzzle(payload)
  if (gameId === 'latburet') applySongPuzzle(payload)
  if (gameId === 'kryssburet') applyCrossPuzzle(payload)
  if (gameId === 'tidsburet') applyTimePuzzle(payload)
}

// ——— MERBURET ——————————————————————————————————————————

const merRounds = ref([])
const merIndex = ref(0)
const merCorrect = ref(0)
const merDone = ref(false)
const merMessage = ref('')
const merCurrent = computed(() => merRounds.value[merIndex.value] ?? { metric: '', left: '', right: '', answer: 'left' })
const merScore = computed(() => merCorrect.value * 200)

function applyMerPuzzle(payload) {
  merRounds.value = Array.isArray(payload.rounds) ? payload.rounds : []
  merIndex.value = 0
  merCorrect.value = 0
  merDone.value = false
  merMessage.value = ''
}

function merChoose(choice) {
  if (merDone.value) return
  if (choice === merCurrent.value.answer) {
    merCorrect.value++
    merMessage.value = 'Riktig'
  } else {
    merMessage.value = 'Feil'
  }
  setTimeout(() => {
    merMessage.value = ''
    if (merIndex.value >= merRounds.value.length - 1) {
      merDone.value = true
      if (isAuthenticated.value) submitNativeScore('merburet', merScore.value)
      return
    }
    merIndex.value++
  }, 450)
}

// ——— LÅTBURET ——————————————————————————————————————————

const songPuzzle = ref({ answer: '', clues: [] })
const songGuess = ref('')
const songGuesses = ref([])
const songDone = ref(false)
const songMessage = ref('')
const songScore = computed(() => songDone.value ? Math.max(0, 1000 - ((songGuesses.value.length - 1) * 200)) : 0)

function applySongPuzzle(payload) {
  songPuzzle.value = { answer: payload.answer ?? '', clues: Array.isArray(payload.clues) ? payload.clues : [] }
  songGuess.value = ''
  songGuesses.value = []
  songDone.value = false
  songMessage.value = ''
}

function songSubmit() {
  if (songDone.value || !songGuess.value.trim()) return
  const guess = songGuess.value.trim().toUpperCase()
  songGuesses.value = [...songGuesses.value, guess]
  songGuess.value = ''
  if (guess === songPuzzle.value.answer || songGuesses.value.length >= songPuzzle.value.clues.length) {
    songDone.value = true
    if (isAuthenticated.value) submitNativeScore('latburet', guess === songPuzzle.value.answer ? songScore.value : 0)
  } else {
    songMessage.value = 'Nytt hint låst opp'
    setTimeout(() => { songMessage.value = '' }, 1200)
  }
}

// ——— KRYSSBURET ————————————————————————————————————————

const crossPuzzle = ref({ clues: [] })
const crossInputs = ref(['', '', '', ''])
const crossDone = ref(false)
const crossCorrect = ref(0)
const crossScore = computed(() => crossCorrect.value * 250)

function applyCrossPuzzle(payload) {
  crossPuzzle.value = { clues: Array.isArray(payload.clues) ? payload.clues : [] }
  crossInputs.value = crossPuzzle.value.clues.map(() => '')
  crossDone.value = false
  crossCorrect.value = 0
}

function crossSubmit() {
  if (crossDone.value) return
  crossCorrect.value = crossPuzzle.value.clues.filter((clue, idx) => (
    crossInputs.value[idx] || ''
  ).trim().toUpperCase() === clue.answer).length
  crossDone.value = true
  if (isAuthenticated.value) submitNativeScore('kryssburet', crossScore.value)
}

// ——— TIDSBURET ——————————————————————————————————————————

const timePuzzle = ref({ prompt: '', year: 2000, place: '', options: [] })
const timeYear = ref(2000)
const timePlace = ref('')
const timeDone = ref(false)
const timeScore = computed(() => {
  if (!timeDone.value) return 0
  const yearPoints = Math.max(0, 500 - Math.abs(Number(timeYear.value) - timePuzzle.value.year) * 20)
  const placePoints = timePlace.value === timePuzzle.value.place ? 500 : 0
  return yearPoints + placePoints
})

function applyTimePuzzle(payload) {
  timePuzzle.value = {
    prompt: payload.prompt ?? '',
    year: Number(payload.year) || 2000,
    place: payload.place ?? '',
    options: Array.isArray(payload.options) ? payload.options : [],
  }
  timeYear.value = 2000
  timePlace.value = ''
  timeDone.value = false
}

function timeSubmit() {
  if (timeDone.value || !timePlace.value) return
  timeDone.value = true
  if (isAuthenticated.value) submitNativeScore('tidsburet', timeScore.value)
}

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

// ——— NATIVE GAME CREATOR ———————————————————————————————

const nativeCreating = ref(false)
const nativeCreatorError = ref('')
const merCreatorRounds = ref([
  { metric: '', left: '', right: '', answer: 'left' },
  { metric: '', left: '', right: '', answer: 'left' },
  { metric: '', left: '', right: '', answer: 'left' },
  { metric: '', left: '', right: '', answer: 'left' },
  { metric: '', left: '', right: '', answer: 'left' },
])
const songCreator = ref({ answer: '', clues: ['', '', ''] })
const crossCreator = ref({
  clues: [
    { prompt: '', answer: '' },
    { prompt: '', answer: '' },
    { prompt: '', answer: '' },
    { prompt: '', answer: '' },
  ],
})
const timeCreator = ref({ prompt: '', year: new Date().getFullYear(), place: '', optionsText: '' })

function resetNativeCreator(gameId) {
  if (gameId === 'merburet') {
    merCreatorRounds.value = [
      { metric: '', left: '', right: '', answer: 'left' },
      { metric: '', left: '', right: '', answer: 'left' },
      { metric: '', left: '', right: '', answer: 'left' },
      { metric: '', left: '', right: '', answer: 'left' },
      { metric: '', left: '', right: '', answer: 'left' },
    ]
  }
  if (gameId === 'latburet') songCreator.value = { answer: '', clues: ['', '', ''] }
  if (gameId === 'kryssburet') {
    crossCreator.value = {
      clues: [
        { prompt: '', answer: '' },
        { prompt: '', answer: '' },
        { prompt: '', answer: '' },
        { prompt: '', answer: '' },
      ],
    }
  }
  if (gameId === 'tidsburet') timeCreator.value = { prompt: '', year: new Date().getFullYear(), place: '', optionsText: '' }
}

function buildNativeCreatorPayload(gameId) {
  if (gameId === 'merburet') {
    if (merCreatorRounds.value.some(r => !r.metric.trim() || !r.left.trim() || !r.right.trim())) return null
    return {
      rounds: merCreatorRounds.value.map(r => ({
        metric: r.metric.trim(),
        left: r.left.trim(),
        right: r.right.trim(),
        answer: r.answer,
      })),
    }
  }
  if (gameId === 'latburet') {
    if (!songCreator.value.answer.trim() || songCreator.value.clues.some(c => !c.trim())) return null
    return {
      answer: songCreator.value.answer.trim().toUpperCase(),
      clues: songCreator.value.clues.map(c => c.trim()),
    }
  }
  if (gameId === 'kryssburet') {
    if (crossCreator.value.clues.some(c => !c.prompt.trim() || !c.answer.trim())) return null
    return {
      clues: crossCreator.value.clues.map(c => ({
        prompt: c.prompt.trim(),
        answer: c.answer.trim().toUpperCase(),
      })),
    }
  }
  if (gameId === 'tidsburet') {
    const options = timeCreator.value.optionsText.split(',').map(o => o.trim()).filter(Boolean)
    if (!timeCreator.value.prompt.trim() || !timeCreator.value.place.trim() || !timeCreator.value.year || options.length < 2) return null
    if (!options.includes(timeCreator.value.place.trim())) options.unshift(timeCreator.value.place.trim())
    return {
      prompt: timeCreator.value.prompt.trim(),
      year: Number(timeCreator.value.year),
      place: timeCreator.value.place.trim(),
      options,
    }
  }
  return null
}

async function submitNativeCreator() {
  nativeCreatorError.value = ''
  const game = activeNativeGame.value
  if (!game) return
  const payload = buildNativeCreatorPayload(game.id)
  if (!payload) {
    nativeCreatorError.value = 'Fyll ut alle feltene.'
    return
  }
  nativeCreating.value = true
  try {
    const data = await puzzleApi.createNative(game.gameName, {
      createdBy: displayName.value || 'Anonym',
      payloadJson: JSON.stringify(payload),
    })
    nativePuzzles.value = {
      ...nativePuzzles.value,
      [game.id]: [data, ...(nativePuzzles.value[game.id] ?? [])],
    }
    loadNativePuzzle(game.id, data)
    resetNativeCreator(game.id)
    creatorOpen.value = false
  } catch {
    nativeCreatorError.value = 'Noe gikk galt. Prøv igjen.'
  } finally {
    nativeCreating.value = false
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
  const [connComp, wrdComp, ...nativeComp] = await Promise.allSettled([
    scoreApi.getCompleted(displayName.value, 'CONNECTIONS'),
    scoreApi.getCompleted(displayName.value, 'WORDLE'),
    ...nativeGames.map(game => scoreApi.getCompleted(displayName.value, game.gameName)),
  ])
  if (connComp.status === 'fulfilled') completedConnIds.value = connComp.value
  if (wrdComp.status === 'fulfilled')  completedWrdIds.value  = wrdComp.value
  const nextCompleted = { ...completedNativeIds.value }
  nativeComp.forEach((result, idx) => {
    if (result.status === 'fulfilled') nextCompleted[nativeGames[idx].id] = result.value
  })
  completedNativeIds.value = nextCompleted
}

async function loadPuzzleLibrary({ applyDaily = false } = {}) {
  const nativeRequests = nativeGames.flatMap(game => [
    puzzleApi.getDailyNative(game.gameName),
    puzzleApi.getAllNative(game.gameName),
  ])
  const [dailyConnResult, dailyWrdResult, allConnResult, allWrdResult, ...nativeResults] = await Promise.allSettled([
    puzzleApi.getDailyConnections(),
    puzzleApi.getDailyWordle(),
    puzzleApi.getAllConnections(),
    puzzleApi.getAllWordle(),
    ...nativeRequests,
  ])

  if (allConnResult.status === 'fulfilled') allConnPuzzles.value = allConnResult.value
  if (allWrdResult.status === 'fulfilled')  allWrdPuzzles.value  = allWrdResult.value
  const nextNativePuzzles = { ...nativePuzzles.value }
  const dailyNative = {}
  nativeGames.forEach((game, idx) => {
    const dailyResult = nativeResults[idx * 2]
    const allResult = nativeResults[idx * 2 + 1]
    if (allResult?.status === 'fulfilled') nextNativePuzzles[game.id] = allResult.value
    if (dailyResult?.status === 'fulfilled' && dailyResult.value?.id) {
      dailyNative[game.id] = dailyResult.value
      if (!nextNativePuzzles[game.id]?.some(p => p.id === dailyResult.value.id)) {
        nextNativePuzzles[game.id] = [dailyResult.value, ...(nextNativePuzzles[game.id] ?? [])]
      }
    }
  })
  nativePuzzles.value = nextNativePuzzles

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
  if (applyDaily) {
    nativeGames.forEach(game => {
      if (dailyNative[game.id]) loadNativePuzzle(game.id, dailyNative[game.id])
    })
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
  flex-wrap: wrap;
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
.source-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: fit-content;
  margin-top: 12px;
  padding: 9px 14px;
  border: 1px solid var(--line-soft);
  border-radius: 999px;
  background: var(--bg);
  font-family: var(--mono);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--ink-soft);
  transition: all 0.2s ease;
}
.source-button:hover {
  border-color: var(--accent-2);
  color: var(--accent-2);
  background: var(--paper);
}

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

.native-game-panel {
  text-align: center;
  padding: 48px 32px;
  background: var(--paper);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}
.native-game-icon {
  width: 56px;
  height: 56px;
  border: 1px solid var(--line-soft);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--serif);
  font-size: 32px;
  color: var(--accent-2);
}
.native-game-body {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}
.native-prompt {
  font-family: var(--serif);
  font-size: 24px;
  font-weight: 400;
  line-height: 1.25;
}
.choice-grid {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.choice-card {
  min-height: 96px;
  padding: 18px;
  background: var(--bg);
  border: 1.5px solid var(--line-soft);
  border-radius: var(--radius);
  font-family: var(--serif);
  font-size: 22px;
  transition: all 0.15s ease;
}
.choice-card:hover { border-color: var(--ink); background: var(--bg-soft); }
.clue-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}
.clue-pill {
  padding: 8px 12px;
  border: 1px solid var(--line-soft);
  border-radius: 999px;
  font-family: var(--mono);
  font-size: 11px;
  color: var(--ink-mute);
}
.native-form {
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 10px;
}
.native-input { text-align: center; }
.cross-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.cross-row {
  display: grid;
  grid-template-columns: 1fr minmax(160px, 220px);
  align-items: center;
  gap: 12px;
  text-align: left;
  font-size: 14px;
  color: var(--ink-soft);
}
.time-grid {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.compact-result { width: 100%; margin: 0; padding: 32px 24px; }

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
  .choice-grid, .time-grid { grid-template-columns: 1fr; }
  .native-form { flex-direction: column; }
  .cross-row { grid-template-columns: 1fr; }
  .native-game-panel { padding: 32px 18px; }
}
</style>
