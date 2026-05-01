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
        <div class="puzzle-library-head">
          <span class="eyebrow">Velg puzzle</span>
          <button v-if="allConnPuzzles.length > LIBRARY_LIMIT" type="button" class="library-more" @click="connLibraryExpanded = !connLibraryExpanded">
            {{ connLibraryExpanded ? 'Vis færre' : 'Vis mer' }}
          </button>
        </div>
        <div class="puzzle-list">
          <div v-for="p in visibleConnPuzzles" :key="p.id" class="puzzle-pick-card">
            <button
              :class="['puzzle-pick', selectedConnId === p.id && 'active', completedConnIds.includes(p.id) && 'done']"
              @click="loadConnPuzzle(p)"
            >
              <span class="puzzle-pick-title">{{ puzzleDisplayTitle(p, 'Tankeburet') }}</span>
              <span class="puzzle-pick-by">{{ puzzleCreatorLabel(p) }}</span>
              <span class="puzzle-pick-date">{{ formatPuzzleDate(p.createdAt) }}</span>
              <span v-if="completedConnIds.includes(p.id)" class="puzzle-pick-done">✓</span>
            </button>
            <div v-if="canManagePuzzle(p)" class="puzzle-manage">
              <button type="button" @click="editConnPuzzle(p)">Rediger</button>
              <button type="button" @click="deleteConnPuzzle(p)">Slett</button>
            </div>
          </div>
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
        <div v-if="connMessage" class="conn-message">{{ connMessage }}</div>
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
          <button class="creator-toggle" @click="toggleCreator">
            <span class="creator-toggle-icon">{{ creatorOpen ? '−' : '+' }}</span>
            Lag ny Tankeburet-puzzle
          </button>
        </template>
        <RouterLink v-else class="creator-login-hint" to="/login">Logg inn for å lage puzzle →</RouterLink>

        <form v-if="creatorOpen && isAuthenticated" class="creator-form" @submit.prevent="submitConnCreator">
          <div class="creator-author-row">
            <label class="creator-label">Tittel</label>
            <input v-model="connCreatorTitle" class="creator-input" placeholder="Navn på spillet" required />
          </div>
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
            <button type="button" class="conn-btn" @click="cancelCreator">Avbryt</button>
            <button type="submit" class="conn-btn conn-btn-primary" :disabled="connCreating">
              {{ connCreating ? 'Lagrer…' : (editingConnId ? 'Oppdater puzzle' : 'Publiser puzzle') }}
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
        <div class="puzzle-library-head">
          <span class="eyebrow">Velg ord</span>
          <button v-if="allWrdPuzzles.length > LIBRARY_LIMIT" type="button" class="library-more" @click="wrdLibraryExpanded = !wrdLibraryExpanded">
            {{ wrdLibraryExpanded ? 'Vis færre' : 'Vis mer' }}
          </button>
        </div>
        <div class="puzzle-list">
          <div v-for="p in visibleWrdPuzzles" :key="p.id" class="puzzle-pick-card">
            <button
              :class="['puzzle-pick', selectedWrdId === p.id && 'active', completedWrdIds.includes(p.id) && 'done']"
              @click="loadWrdPuzzle(p)"
            >
              <span class="puzzle-pick-title">{{ puzzleDisplayTitle(p, 'Ordburet') }}</span>
              <span class="puzzle-pick-by">{{ puzzleCreatorLabel(p) }}</span>
              <span class="puzzle-pick-date">{{ formatPuzzleDate(p.createdAt) }}</span>
              <span v-if="completedWrdIds.includes(p.id)" class="puzzle-pick-done">✓</span>
            </button>
            <div v-if="canManagePuzzle(p)" class="puzzle-manage">
              <button type="button" @click="editWrdPuzzle(p)">Rediger</button>
              <button type="button" @click="deleteWrdPuzzle(p)">Slett</button>
            </div>
          </div>
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
          <button class="creator-toggle" @click="toggleCreator">
            <span class="creator-toggle-icon">{{ creatorOpen ? '−' : '+' }}</span>
            Lag nytt Ordburet-ord
          </button>
        </template>
        <RouterLink v-else class="creator-login-hint" to="/login">Logg inn for å lage ord →</RouterLink>

        <form v-if="creatorOpen && isAuthenticated" class="creator-form" @submit.prevent="submitWrdCreator">
          <div class="creator-author-row">
            <label class="creator-label">Tittel</label>
            <input v-model="wrdCreatorTitle" class="creator-input" placeholder="Navn på spillet" required />
          </div>
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
            <button type="button" class="conn-btn" @click="cancelCreator">Avbryt</button>
            <button type="submit" class="conn-btn conn-btn-primary" :disabled="wrdCreating || wrdCreatorWord.length !== 5">
              {{ wrdCreating ? 'Lagrer…' : (editingWrdId ? 'Oppdater ord' : 'Publiser ord') }}
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
          <div class="puzzle-library-head">
            <span class="eyebrow">Velg spill</span>
            <button v-if="activeNativePuzzles.length > LIBRARY_LIMIT" type="button" class="library-more" @click="toggleNativeLibrary">
              {{ nativeLibraryExpanded[activeGame] ? 'Vis færre' : 'Vis mer' }}
            </button>
          </div>
          <div class="puzzle-list">
            <div v-for="p in visibleNativePuzzles" :key="p.id" class="puzzle-pick-card">
              <button
                :class="['puzzle-pick', selectedNativeIds[activeGame] === p.id && 'active', activeNativeCompletedIds.includes(p.id) && 'done']"
                @click="loadNativePuzzle(activeGame, p)"
              >
                <span class="puzzle-pick-title">{{ nativePuzzleDisplayTitle(p) }}</span>
                <span class="puzzle-pick-by">{{ puzzleCreatorLabel(p) }}</span>
                <span class="puzzle-pick-date">{{ formatPuzzleDate(p.createdAt) }}</span>
                <span v-if="activeNativeCompletedIds.includes(p.id)" class="puzzle-pick-done">✓</span>
              </button>
              <div v-if="canManagePuzzle(p)" class="puzzle-manage">
                <button type="button" @click="editNativePuzzle(p)">Rediger</button>
                <button type="button" @click="deleteNativePuzzle(p)">Slett</button>
              </div>
            </div>
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
            <div class="songless-steps">
              <button
                v-for="(duration, idx) in songDurations"
                :key="duration"
                :class="['songless-step', idx === songStep && 'active']"
                :disabled="idx > songStep"
                type="button"
                @click="songStep = idx"
              >
                {{ songDurationLabel(duration) }}
              </button>
            </div>
            <button class="songless-play" type="button" :disabled="!songPuzzle.previewUrl || songStarting" @click="songTogglePreview">
              {{ songPlaying ? '⏸' : '▶' }} {{ songDurationLabel(songCurrentDuration) }}
            </button>
            <p v-if="!songPuzzle.previewUrl" class="wrd-message">Ingen forhåndsvisning tilgjengelig for denne sangen</p>
            <form class="songless-form" @submit.prevent="songSubmit">
              <div class="songless-guess-wrap">
                <input v-model="songGuess" class="creator-input native-input" placeholder="Tittel på sangen..." @input="songSearchSuggestions" />
                <div v-if="songSuggestions.length" class="songless-suggestions">
                  <button
                    v-for="suggestion in songSuggestions"
                    :key="suggestion.trackId"
                    type="button"
                    class="songless-suggestion"
                    @click="songSelectSuggestion(suggestion)"
                  >
                    <strong>{{ suggestion.trackName }}</strong>
                    <span>{{ suggestion.artistName }}</span>
                  </button>
                </div>
              </div>
              <div class="songless-actions">
                <button class="conn-btn" type="button" @click="songSkip">Hopp over</button>
                <button class="conn-btn conn-btn-primary" type="submit">Gjett</button>
              </div>
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
          <template v-if="cwPuzzle && !cwDone && activeNativeGameState !== 'completed'">
            <div class="cw-layout">
              <!-- Grid -->
              <div class="cw-grid-wrap">
                <div class="cw-grid" :style="{ gridTemplateColumns: `repeat(${cwPuzzle.cols}, 1fr)`, '--cw-cols': cwPuzzle.cols }">
                  <div
                    v-for="cell in cwGrid.flat()"
                    :key="`${cell.row},${cell.col}`"
                    :class="['cw-cell',
                      cell.black && 'cw-black',
                      !cell.black && cwSelectedCellKeys.has(`${cell.row},${cell.col}`) && 'cw-highlighted',
                      !cell.black && cwActiveCell && cwActiveCell.row === cell.row && cwActiveCell.col === cell.col && 'cw-active',
                    ]"
                    @click="cwClickCell(cell.row, cell.col)"
                  >
                    <span v-if="cell.number" class="cw-num">{{ cell.number }}</span>
                    <span v-if="!cell.black" class="cw-letter">{{ cwInput[`${cell.row},${cell.col}`] || '' }}</span>
                  </div>
                </div>
              </div>

              <!-- Clue lists -->
              <div class="cw-clues">
                <div class="cw-clues-section">
                  <h4 class="cw-clues-heading">Across</h4>
                  <div
                    v-for="clue in cwAcross"
                    :key="`A${clue.n}`"
                    :class="['cw-clue-row', cwSelected && cwSelected.n === clue.n && cwSelected.dir === 'A' && 'cw-clue-active']"
                    @click="cwSelectClue(clue.n, 'A')"
                  >
                    <span :class="['cw-badge', `cw-badge-${(clue.n - 1) % 8}`]">{{ clue.n }}A</span>
                    <span class="cw-clue-text">{{ clue.clue }}</span>
                  </div>
                </div>
                <div class="cw-clues-section">
                  <h4 class="cw-clues-heading">Down</h4>
                  <div
                    v-for="clue in cwDown"
                    :key="`D${clue.n}`"
                    :class="['cw-clue-row', cwSelected && cwSelected.n === clue.n && cwSelected.dir === 'D' && 'cw-clue-active']"
                    @click="cwSelectClue(clue.n, 'D')"
                  >
                    <span :class="['cw-badge', `cw-badge-${(clue.n - 1) % 8}`]">{{ clue.n }}D</span>
                    <span class="cw-clue-text">{{ clue.clue }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Player bar -->
            <div v-if="cwCurrentClue" class="cw-player-bar">
              <div class="cw-player-clue">
                <span :class="['cw-badge', `cw-badge-${(cwCurrentClue.n - 1) % 8}`]">{{ cwCurrentClue.n }}{{ cwCurrentClue.dir }}</span>
                <span class="cw-player-text">{{ cwCurrentClue.clue }}</span>
              </div>
              <div class="cw-player-controls">
                <button class="cw-ctrl-btn" @click="cwPrevClue" title="Forrige">⏮</button>
                <button class="cw-ctrl-btn cw-play-btn" :disabled="!cwCurrentClue.previewUrl" @click="cwTogglePreview">
                  {{ cwPlaying ? '⏸' : '▶' }}
                </button>
                <button class="cw-ctrl-btn" @click="cwNextClue" title="Neste">⏭</button>
                <button class="conn-btn cw-hint-btn" type="button" :disabled="!cwActiveCell" @click="cwUseHint">Hint</button>
              </div>
            </div>
            <div class="cw-submit-row">
              <p v-if="cwMessage" class="wrd-message">{{ cwMessage }}</p>
              <button class="conn-btn conn-btn-primary" type="button" @click="cwSubmit">Send inn</button>
            </div>
          </template>

          <div v-if="cwDone && activeNativeGameState !== 'completed'" class="sp-result compact-result">
            <div class="sp-result-icon">{{ cwWon ? '+' : '✕' }}</div>
            <h3>{{ cwWon ? 'Løst!' : 'Ferdig' }}</h3>
            <p><strong>{{ cwScore }} poeng</strong></p>
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
            <button class="creator-toggle" @click="toggleCreator">
              <span class="creator-toggle-icon">{{ creatorOpen ? '−' : '+' }}</span>
              Lag ny {{ activeNativeGame.name }}
            </button>
          </template>
          <RouterLink v-else class="creator-login-hint" to="/login">Logg inn for å lage spill →</RouterLink>

          <form v-if="creatorOpen && isAuthenticated" class="creator-form" @submit.prevent="submitNativeCreator">
            <div v-if="activeGame !== 'kryssburet'" class="creator-author-row">
              <label class="creator-label">Tittel</label>
              <input v-model="nativeCreatorTitle" class="creator-input" placeholder="Navn på spillet" required />
            </div>
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
              <input v-model="songCreator.searchQuery" class="creator-input" placeholder="Søk sang" @keyup.enter.prevent="songCreatorSearch" />
              <button type="button" class="conn-btn" @click="songCreatorSearch">Søk</button>
              <div v-if="songCreator.searchResults.length" class="music-search-results">
                <button type="button" v-for="r in songCreator.searchResults" :key="r.trackId" class="music-search-result" @click="songCreatorSelectTrack(r)">
                  <strong>{{ r.artistName }}</strong> – {{ r.trackName }}
                </button>
              </div>
              <p v-if="songCreator.trackId" style="font-size:12px;color:var(--accent);margin:0;">✓ {{ songCreator.artist }} – {{ songCreator.title }}</p>
            </template>

            <template v-if="activeGame === 'kryssburet'">
              <div class="cw-creator-layout">
                <div class="cw-creator-fields">
                  <div class="creator-group">
                    <input v-model="cwCreatorForm.title" class="creator-input" placeholder="Puslespilltittel" required />
                    <div class="creator-words-row" style="grid-template-columns:1fr 1fr;">
                      <label class="creator-label" style="display:flex;flex-direction:column;gap:4px;">
                        Rader
                        <input v-model.number="cwCreatorForm.rows" class="creator-input" type="number" min="3" max="15" />
                      </label>
                      <label class="creator-label" style="display:flex;flex-direction:column;gap:4px;">
                        Kolonner
                        <input v-model.number="cwCreatorForm.cols" class="creator-input" type="number" min="3" max="15" />
                      </label>
                    </div>
                  </div>
                  <div v-for="(clue, idx) in cwCreatorForm.clues" :key="idx" class="creator-group">
                    <div class="creator-group-label">Ledetråd {{ idx + 1 }}</div>
                    <div class="cw-creator-position-row">
                      <input v-model.number="clue.n" class="creator-input" type="number" min="1" placeholder="Nr" />
                      <select v-model="clue.dir" class="creator-input">
                        <option value="A">Across</option>
                        <option value="D">Down</option>
                      </select>
                      <input v-model.number="clue.row" class="creator-input" type="number" min="0" placeholder="Rad" />
                      <input v-model.number="clue.col" class="creator-input" type="number" min="0" placeholder="Kol" />
                    </div>
                    <input v-model="clue.answer" class="creator-input" placeholder="SVAR" @input="clue.answer = clue.answer.toUpperCase()" required />
                    <input v-model="clue.clue" class="creator-input" placeholder="Hinttekst" required />
                    <div class="creator-words-row" style="grid-template-columns:1fr auto;">
                      <input v-model="clue.searchQuery" class="creator-input" placeholder="Søk sang (for preview)" @keyup.enter.prevent="cwCreatorSearch(idx)" />
                      <button type="button" class="conn-btn" @click="cwCreatorSearch(idx)">Søk</button>
                    </div>
                    <div v-if="clue.searchResults && clue.searchResults.length" class="music-search-results">
                      <button type="button" v-for="r in clue.searchResults" :key="r.trackId" class="music-search-result" @click="cwCreatorSelectTrack(idx, r)">
                        <strong>{{ r.artistName }}</strong> – {{ r.trackName }}
                      </button>
                    </div>
                    <p v-if="clue.trackId" style="font-size:12px;color:var(--accent);margin:0;">✓ {{ clue.artist }} – {{ clue.title }}</p>
                    <button type="button" class="conn-btn" style="align-self:flex-start;" @click="cwCreatorForm.clues.splice(idx, 1)">Fjern</button>
                  </div>
                  <button type="button" class="conn-btn" @click="cwCreatorAddClue">+ Legg til ledetråd</button>
                </div>
                <div class="cw-creator-preview">
                  <div class="cw-creator-preview-head">
                    <span class="eyebrow">Forhåndsvisning</span>
                    <span>{{ cwCreatorFilledCells }} felt</span>
                  </div>
                  <div class="cw-creator-grid-wrap">
                    <div class="cw-creator-grid" :style="{ gridTemplateColumns: `repeat(${cwCreatorCols}, 1fr)`, '--cw-cols': cwCreatorCols }">
                      <div
                        v-for="cell in cwCreatorGrid.flat()"
                        :key="`preview-${cell.row},${cell.col}`"
                        :class="['cw-creator-cell', cell.black && 'cw-black']"
                      >
                        <span v-if="cell.number" class="cw-num">{{ cell.number }}</span>
                        <span v-if="!cell.black" class="cw-letter">{{ cell.answer }}</span>
                      </div>
                    </div>
                  </div>
                </div>
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
              <button type="button" class="conn-btn" @click="cancelCreator">Avbryt</button>
              <button type="submit" class="conn-btn conn-btn-primary" :disabled="nativeCreating">
                {{ nativeCreating ? 'Lagrer…' : (editingNativeId ? 'Oppdater spill' : 'Publiser spill') }}
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
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { puzzleApi, scoreApi } from '../services/api'
import { useLiveDateInfo } from '../composables/useLiveDateInfo'
import { isAuthenticated, displayName, isAdminOrAbove } from '../services/authState'
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
const LIBRARY_LIMIT = 6
const connLibraryExpanded = ref(false)
const wrdLibraryExpanded = ref(false)
const nativeLibraryExpanded = ref({})
const visibleConnPuzzles = computed(() => connLibraryExpanded.value ? allConnPuzzles.value : allConnPuzzles.value.slice(0, LIBRARY_LIMIT))
const visibleWrdPuzzles = computed(() => wrdLibraryExpanded.value ? allWrdPuzzles.value : allWrdPuzzles.value.slice(0, LIBRARY_LIMIT))
const visibleNativePuzzles = computed(() => nativeLibraryExpanded.value[activeGame.value] ? activeNativePuzzles.value : activeNativePuzzles.value.slice(0, LIBRARY_LIMIT))
const activeNativeGameState = computed(() => {
  const id = selectedNativeIds.value[activeGame.value]
  return id && activeNativeCompletedIds.value.includes(id) ? 'completed' : 'playing'
})

function toggleNativeLibrary() {
  nativeLibraryExpanded.value = {
    ...nativeLibraryExpanded.value,
    [activeGame.value]: !nativeLibraryExpanded.value[activeGame.value],
  }
}

function puzzleDisplayTitle(puzzle, fallback) {
  return puzzle?.title?.trim() || (puzzle?.isDaily ? `Daglig ${fallback}` : fallback)
}

function puzzleCreatorLabel(puzzle) {
  return `Laget av ${puzzle?.createdBy || 'Anonym'}`
}

function nativePuzzleDisplayTitle(puzzle) {
  const payload = parseNativePayload(puzzle)
  if (puzzle?.isDaily) {
    return puzzle?.title?.trim() || `Daglig ${activeNativeGame.value?.name || 'Spill'}`
  }
  return puzzleDisplayTitle(puzzle, payload?.title || activeNativeGame.value?.name || 'Spill')
}

function canManagePuzzle(puzzle) {
  if (!isAuthenticated.value || !puzzle || puzzle.isDaily) return false
  return isAdminOrAbove.value || puzzle.createdBy === displayName.value
}

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
  if (gameId === 'kryssburet') applyCwPuzzle(payload)
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

const songDurations = [0.1, 0.5, 1, 2, 4, 8]
const songPuzzle = ref({ answer: '', artist: '', title: '', trackId: null, previewUrl: null })
const songGuess = ref('')
const songGuesses = ref([])
const songSuggestions = ref([])
const songStep = ref(0)
const songDone = ref(false)
const songWon = ref(false)
const songMessage = ref('')
const songPlaying = ref(false)
const songStarting = ref(false)
let _songAudio = null
let _songTimer = null
let _songPlayToken = 0
let _songSuggestTimer = null

const songCurrentDuration = computed(() => songDurations[Math.min(songStep.value, songDurations.length - 1)])
const songScore = computed(() => songWon.value ? Math.max(0, 1000 - (songStep.value * 150)) : 0)

async function applySongPuzzle(payload) {
  songStopPreview()
  const answer = (payload.answer ?? payload.title ?? '').toUpperCase()
  let previewUrl = payload.previewUrl || null
  if (!previewUrl && payload.trackId) {
    try {
      const res = await fetch(`https://itunes.apple.com/lookup?id=${payload.trackId}`)
      const data = await res.json()
      previewUrl = data.results?.[0]?.previewUrl || null
    } catch {}
  }
  if (!previewUrl && answer) {
    try {
      const term = encodeURIComponent(`${payload.artist ?? ''} ${answer}`.trim())
      const res = await fetch(`https://itunes.apple.com/search?term=${term}&media=music&limit=10`)
      const data = await res.json()
      const exact = (data.results || []).find(track => normalizeCwAudioTitle(track.trackName) === normalizeCwAudioTitle(answer))
      previewUrl = exact?.previewUrl || null
    } catch {}
  }
  songPuzzle.value = {
    answer,
    artist: payload.artist ?? '',
    title: payload.title ?? answer,
    trackId: payload.trackId || null,
    previewUrl,
  }
  songGuess.value = ''
  songGuesses.value = []
  songSuggestions.value = []
  songStep.value = 0
  songDone.value = false
  songWon.value = false
  songMessage.value = ''
}

function songSubmit() {
  if (songDone.value || !songGuess.value.trim()) return
  const guess = songGuess.value.trim()
  songGuesses.value = [...songGuesses.value, guess]
  songGuess.value = ''
  songSuggestions.value = []
  if (normalizeCwAudioTitle(guess) === normalizeCwAudioTitle(songPuzzle.value.answer)) {
    songWon.value = true
    songDone.value = true
    songStopPreview()
    if (isAuthenticated.value) submitNativeScore('latburet', songScore.value)
    return
  }
  if (songStep.value >= songDurations.length - 1) {
    songDone.value = true
    songStopPreview()
    if (isAuthenticated.value) submitNativeScore('latburet', 0)
    return
  }
  songStep.value++
  songMessage.value = 'Lengre klipp låst opp'
  setTimeout(() => { songMessage.value = '' }, 1200)
}

function songSkip() {
  if (songDone.value) return
  songGuesses.value = [...songGuesses.value, '']
  songSuggestions.value = []
  if (songStep.value >= songDurations.length - 1) {
    songDone.value = true
    songStopPreview()
    if (isAuthenticated.value) submitNativeScore('latburet', 0)
    return
  }
  songStep.value++
}

function songSearchSuggestions() {
  if (_songSuggestTimer) clearTimeout(_songSuggestTimer)
  const query = songGuess.value.trim()
  if (query.length < 2) {
    songSuggestions.value = []
    return
  }
  _songSuggestTimer = setTimeout(async () => {
    try {
      const res = await fetch(`https://itunes.apple.com/search?term=${encodeURIComponent(query)}&media=music&limit=6`)
      const data = await res.json()
      songSuggestions.value = data.results || []
    } catch {
      songSuggestions.value = []
    }
  }, 180)
}

function songSelectSuggestion(suggestion) {
  songGuess.value = suggestion.trackName
  songSuggestions.value = []
}

async function songTogglePreview() {
  if (!songPuzzle.value.previewUrl || songStarting.value) return
  if (_songAudio && songPlaying.value) {
    songStopPreview()
    return
  }
  songStopPreview()
  songStarting.value = true
  const token = ++_songPlayToken
  const audio = new Audio(songPuzzle.value.previewUrl)
  _songAudio = audio
  audio.currentTime = 0
  const stopCurrent = () => {
    if (token === _songPlayToken && _songAudio === audio) songStopPreview()
  }
  audio.addEventListener('ended', stopCurrent, { once: true })
  try {
    await audio.play()
    if (token !== _songPlayToken || _songAudio !== audio) {
      audio.pause()
      return
    }
    songPlaying.value = true
    _songTimer = setTimeout(stopCurrent, songCurrentDuration.value * 1000)
  } catch {
    if (token === _songPlayToken && _songAudio === audio) songStopPreview()
  } finally {
    if (token === _songPlayToken) songStarting.value = false
  }
}

function songStopPreview() {
  _songPlayToken++
  if (_songTimer) {
    clearTimeout(_songTimer)
    _songTimer = null
  }
  if (_songAudio) {
    _songAudio.pause()
    _songAudio.currentTime = 0
    _songAudio = null
  }
  songStarting.value = false
  songPlaying.value = false
}

function songDurationLabel(duration) {
  return `${duration % 1 === 0 ? duration.toFixed(0) : duration.toFixed(1)}s`
}

// ——— KRYSSBURET (CROSSWORD) ————————————————————————————

const cwPuzzle     = ref(null)
const cwGrid       = ref([])
const cwInput      = ref({})
const cwSelected   = ref(null)   // { n, dir }
const cwActiveCell = ref(null)   // { row, col }
const cwPlaying    = ref(false)
const cwDone       = ref(false)
const cwWon        = ref(false)
const cwMessage    = ref('')
let _cwAudio = null

const cwAcross = computed(() => (cwPuzzle.value?.clues ?? []).filter(c => c.dir === 'A').sort((a, b) => a.n - b.n))
const cwDown   = computed(() => (cwPuzzle.value?.clues ?? []).filter(c => c.dir === 'D').sort((a, b) => a.n - b.n))
const cwCurrentClue = computed(() => {
  if (!cwSelected.value || !cwPuzzle.value) return null
  return cwPuzzle.value.clues.find(c => c.n === cwSelected.value.n && c.dir === cwSelected.value.dir) ?? null
})
const cwScore = computed(() => (cwWon.value ? 1000 : 0))
const cwSelectedCellKeys = computed(() => {
  const clue = cwCurrentClue.value
  if (!clue) return new Set()
  const keys = new Set()
  for (let i = 0; i < clue.answer.length; i++) {
    const r = clue.dir === 'D' ? clue.row + i : clue.row
    const c = clue.dir === 'A' ? clue.col + i : clue.col
    keys.add(`${r},${c}`)
  }
  return keys
})

function buildCwGrid(puzzle) {
  const { rows, cols, clues } = puzzle
  const grid = Array.from({ length: rows }, (_, r) =>
    Array.from({ length: cols }, (_, c) => ({ row: r, col: c, black: true, number: null, answer: '', clueRefs: [] }))
  )
  for (const clue of clues) {
    for (let i = 0; i < clue.answer.length; i++) {
      const r = clue.dir === 'D' ? clue.row + i : clue.row
      const c = clue.dir === 'A' ? clue.col + i : clue.col
      if (r >= 0 && r < rows && c >= 0 && c < cols) {
        grid[r][c].black = false
        grid[r][c].answer = clue.answer[i]
        grid[r][c].clueRefs.push({ n: clue.n, dir: clue.dir })
      }
    }
    if (clue.row >= 0 && clue.row < rows && clue.col >= 0 && clue.col < cols) {
      grid[clue.row][clue.col].number = clue.n
    }
  }
  return grid
}

function getCluePositions(clue) {
  const positions = []
  for (let i = 0; i < clue.answer.length; i++) {
    positions.push({
      row: clue.dir === 'D' ? clue.row + i : clue.row,
      col: clue.dir === 'A' ? clue.col + i : clue.col,
    })
  }
  return positions
}

async function applyCwPuzzle(payload) {
  if (_cwAudio) { _cwAudio.pause(); _cwAudio = null }
  cwDone.value = false
  cwWon.value = false
  cwInput.value = {}
  cwSelected.value = null
  cwActiveCell.value = null
  cwPlaying.value = false
  cwMessage.value = ''
  const clues = await Promise.all((payload.clues || []).map(async clue => {
    let previewUrl = clue.previewUrl || null
    if (!previewUrl && clue.trackId) {
      try {
        const res = await fetch(`https://itunes.apple.com/lookup?id=${clue.trackId}`)
        const data = await res.json()
        previewUrl = data.results?.[0]?.previewUrl || null
      } catch {}
    }
    if (!previewUrl && clue.answer) {
      try {
        const res = await fetch(`https://itunes.apple.com/search?term=${encodeURIComponent(clue.answer)}&media=music&limit=10`)
        const data = await res.json()
        const exact = (data.results || []).find(track => normalizeCwAudioTitle(track.trackName) === normalizeCwAudioTitle(clue.answer))
        previewUrl = exact?.previewUrl || null
      } catch {}
    }
    return { ...clue, previewUrl }
  }))
  cwPuzzle.value = { ...payload, clues }
  cwGrid.value = buildCwGrid(cwPuzzle.value)
  if (clues.length > 0) cwSelected.value = { n: clues[0].n, dir: clues[0].dir }
}

function normalizeCwAudioTitle(value) {
  return String(value || '').toUpperCase().replace(/[^A-Z0-9]/g, '')
}

watch(cwSelected, (sel) => {
  if (!sel || !cwPuzzle.value) return
  const clue = cwPuzzle.value.clues.find(c => c.n === sel.n && c.dir === sel.dir)
  if (!clue) return
  const positions = getCluePositions(clue)
  const firstUnfilled = positions.find(p => !cwInput.value[`${p.row},${p.col}`])
  cwActiveCell.value = firstUnfilled || positions[0] || null
})

function cwSelectClue(n, dir) {
  if (!cwSelected.value || cwSelected.value.n !== n || cwSelected.value.dir !== dir) cwStopPreview()
  cwSelected.value = { n, dir }
}

function cwClickCell(row, col) {
  const cell = cwGrid.value[row]?.[col]
  if (!cell || cell.black) return
  if (cwActiveCell.value?.row === row && cwActiveCell.value?.col === col) {
    const refs = cell.clueRefs
    if (refs.length > 1 && cwSelected.value) {
      const idx = refs.findIndex(r => r.n === cwSelected.value.n && r.dir === cwSelected.value.dir)
      cwStopPreview()
      cwSelected.value = { n: refs[(idx + 1) % refs.length].n, dir: refs[(idx + 1) % refs.length].dir }
    }
    return
  }
  cwActiveCell.value = { row, col }
  const refs = cell.clueRefs
  if (!refs.length) return
  if (cwSelected.value) {
    const sameDir = refs.find(r => r.dir === cwSelected.value.dir)
    if (sameDir) { cwSelected.value = { n: sameDir.n, dir: sameDir.dir }; return }
  }
  cwStopPreview()
  cwSelected.value = { n: refs[0].n, dir: refs[0].dir }
}

function cwMoveCursor(delta) {
  if (!cwCurrentClue.value || !cwActiveCell.value) return
  const positions = getCluePositions(cwCurrentClue.value)
  const idx = positions.findIndex(p => p.row === cwActiveCell.value.row && p.col === cwActiveCell.value.col)
  const next = positions[idx + delta]
  if (next) {
    cwStopPreview()
    cwActiveCell.value = { row: next.row, col: next.col }
  }
}

function cwCheckWin() {
  const allCells = cwGrid.value.flatMap(r => r).filter(c => !c.black)
  if (!allCells.every(c => cwInput.value[`${c.row},${c.col}`])) return
  if (allCells.every(c => cwInput.value[`${c.row},${c.col}`] === c.answer)) {
    cwDone.value = true
    cwWon.value = true
    if (isAuthenticated.value) submitNativeScore('kryssburet', 1000)
  }
}

function cwStopPreview() {
  if (_cwAudio) {
    _cwAudio.pause()
    _cwAudio = null
  }
  cwPlaying.value = false
}

function cwTogglePreview() {
  const clue = cwCurrentClue.value
  if (!clue?.previewUrl) return
  if (_cwAudio && cwPlaying.value) {
    cwStopPreview()
    return
  }
  cwStopPreview()
  _cwAudio = new Audio(clue.previewUrl)
  _cwAudio.play()
  cwPlaying.value = true
  _cwAudio.addEventListener('ended', () => { cwPlaying.value = false; _cwAudio = null })
}

function cwSubmit() {
  if (cwDone.value) return
  const allCells = cwGrid.value.flatMap(r => r).filter(c => !c.black)
  if (!allCells.every(c => cwInput.value[`${c.row},${c.col}`])) {
    cwMessage.value = 'Fyll ut hele rutenettet før du sender inn.'
    setTimeout(() => { cwMessage.value = '' }, 1600)
    return
  }
  cwWon.value = allCells.every(c => cwInput.value[`${c.row},${c.col}`] === c.answer)
  cwDone.value = true
  cwStopPreview()
  if (isAuthenticated.value) submitNativeScore('kryssburet', cwScore.value)
}

function cwUseHint() {
  if (!cwActiveCell.value) return
  const cell = cwGrid.value[cwActiveCell.value.row]?.[cwActiveCell.value.col]
  if (!cell || cell.black || !cell.answer) return
  cwStopPreview()
  cwInput.value = {
    ...cwInput.value,
    [`${cell.row},${cell.col}`]: cell.answer,
  }
  cwCheckWin()
}

function cwPrevClue() {
  if (!cwPuzzle.value || !cwSelected.value) return
  const all = cwPuzzle.value.clues
  const idx = all.findIndex(c => c.n === cwSelected.value.n && c.dir === cwSelected.value.dir)
  if (idx > 0) { cwStopPreview(); cwSelected.value = { n: all[idx - 1].n, dir: all[idx - 1].dir } }
}

function cwNextClue() {
  if (!cwPuzzle.value || !cwSelected.value) return
  const all = cwPuzzle.value.clues
  const idx = all.findIndex(c => c.n === cwSelected.value.n && c.dir === cwSelected.value.dir)
  if (idx < all.length - 1) { cwStopPreview(); cwSelected.value = { n: all[idx + 1].n, dir: all[idx + 1].dir } }
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
const connMessage   = ref('')
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
  connMessage.value   = ''
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
    if (connPuzzle.value.groups.some(g =>
      !connSolved.value.some(s => s.category === g.category) &&
      sel.filter(w => g.words.includes(w)).length === 3
    )) {
      connMessage.value = 'Én unna'
      setTimeout(() => { connMessage.value = '' }, 3000)
    }
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
    title: data.title,
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
    const states = wrdEvaluateGuess(guess)
    for (let i = 0; i < guess.length; i++) {
      if (guess[i] !== letter) continue
      if (states[i] === 'correct') { best = 'correct'; break }
      if (states[i] === 'present' && best !== 'correct') best = 'present'
      if (states[i] === 'absent' && best === '') best = 'absent'
    }
    if (best === 'correct') break
  }
  return best
}

function wrdEvaluateGuess(guess) {
  const states = Array(guess.length).fill('absent')
  const remaining = {}
  for (let i = 0; i < wrdTarget.value.length; i++) {
    if (guess[i] === wrdTarget.value[i]) {
      states[i] = 'correct'
    } else {
      remaining[wrdTarget.value[i]] = (remaining[wrdTarget.value[i]] || 0) + 1
    }
  }
  for (let i = 0; i < guess.length; i++) {
    if (states[i] === 'correct') continue
    const letter = guess[i]
    if (remaining[letter] > 0) {
      states[i] = 'present'
      remaining[letter]--
    }
  }
  return states
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
    return wrdEvaluateGuess(wrdGuesses.value[row])[col]
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

function toggleCreator() {
  if (creatorOpen.value) {
    cancelCreator()
    return
  }
  resetCurrentCreator()
  creatorOpen.value = true
}

function cancelCreator() {
  creatorOpen.value = false
  resetCurrentCreator()
}

function resetCurrentCreator() {
  if (activeGame.value === 'connections') resetConnCreator()
  else if (activeGame.value === 'wordle') resetWrdCreator()
  else if (activeNativeGame.value) resetNativeCreator(activeGame.value)
}

// ——— CONNECTIONS CREATOR ———————————————————————————————

const connCreatorForm = ref([
  { category: '', words: ['', '', '', ''] },
  { category: '', words: ['', '', '', ''] },
  { category: '', words: ['', '', '', ''] },
  { category: '', words: ['', '', '', ''] },
])
const connCreatorTitle = ref('')
const editingConnId = ref(null)
const connCreating     = ref(false)
const connCreatorError = ref('')

function resetConnCreator() {
  editingConnId.value = null
  connCreatorTitle.value = ''
  connCreatorForm.value = [
    { category: '', words: ['', '', '', ''] },
    { category: '', words: ['', '', '', ''] },
    { category: '', words: ['', '', '', ''] },
    { category: '', words: ['', '', '', ''] },
  ]
}

async function submitConnCreator() {
  connCreatorError.value = ''
  if (!connCreatorTitle.value.trim()) { connCreatorError.value = 'Tittel må fylles ut.'; return }
  for (const g of connCreatorForm.value) {
    if (!g.category.trim()) { connCreatorError.value = 'Alle kategorier må fylles ut.'; return }
    if (g.words.some(w => !w.trim())) { connCreatorError.value = 'Alle 16 ord må fylles ut.'; return }
  }
  connCreating.value = true
  try {
    const dto = {
      createdBy: displayName.value || 'Anonym',
      title: connCreatorTitle.value.trim(),
      groups: connCreatorForm.value.map(g => ({
        category: g.category.trim(),
        words:    g.words.map(w => w.trim().toUpperCase()),
      })),
    }
    const data = editingConnId.value
      ? await puzzleApi.updateConnections(editingConnId.value, dto)
      : await puzzleApi.createConnections(dto)
    allConnPuzzles.value = editingConnId.value
      ? allConnPuzzles.value.map(p => p.id === data.id ? data : p)
      : [data, ...allConnPuzzles.value]
    selectedConnId.value = data.id
    applyConnectionsPuzzle(data)
    creatorOpen.value = false
    resetConnCreator()
  } catch {
    connCreatorError.value = 'Noe gikk galt. Prøv igjen.'
  } finally {
    connCreating.value = false
  }
}

function editConnPuzzle(p) {
  if (!canManagePuzzle(p)) return
  editingConnId.value = p.id
  connCreatorTitle.value = puzzleDisplayTitle(p, 'Tankeburet')
  connCreatorForm.value = p.groups.map(g => ({
    category: g.category,
    words: [...g.words],
  }))
  creatorOpen.value = true
}

async function deleteConnPuzzle(p) {
  if (!canManagePuzzle(p) || !window.confirm('Slette dette spillet?')) return
  await puzzleApi.deleteConnections(p.id)
  allConnPuzzles.value = allConnPuzzles.value.filter(item => item.id !== p.id)
  if (selectedConnId.value === p.id && allConnPuzzles.value[0]) loadConnPuzzle(allConnPuzzles.value[0])
}

// ——— WORDLE CREATOR ————————————————————————————————————

const wrdCreatorWord  = ref('')
const wrdCreatorTitle = ref('')
const editingWrdId = ref(null)
const wrdCreating     = ref(false)
const wrdCreatorError = ref('')

function resetWrdCreator() {
  editingWrdId.value = null
  wrdCreatorTitle.value = ''
  wrdCreatorWord.value = ''
}

async function submitWrdCreator() {
  wrdCreatorError.value = ''
  if (!wrdCreatorTitle.value.trim()) { wrdCreatorError.value = 'Tittel må fylles ut.'; return }
  const word = wrdCreatorWord.value.trim().toUpperCase()
  if (word.length !== 5) { wrdCreatorError.value = 'Ordet må ha nøyaktig 5 bokstaver.'; return }
  wrdCreating.value = true
  try {
    const dto = {
      createdBy: displayName.value || 'Anonym',
      title: wrdCreatorTitle.value.trim(),
      word,
    }
    const data = editingWrdId.value
      ? await puzzleApi.updateWordle(editingWrdId.value, dto)
      : await puzzleApi.createWordle(dto)
    allWrdPuzzles.value = editingWrdId.value
      ? allWrdPuzzles.value.map(p => p.id === data.id ? data : p)
      : [data, ...allWrdPuzzles.value]
    loadWrdPuzzle(data)
    creatorOpen.value  = false
    resetWrdCreator()
  } catch {
    wrdCreatorError.value = 'Noe gikk galt. Prøv igjen.'
  } finally {
    wrdCreating.value = false
  }
}

function editWrdPuzzle(p) {
  if (!canManagePuzzle(p)) return
  editingWrdId.value = p.id
  wrdCreatorTitle.value = puzzleDisplayTitle(p, 'Ordburet')
  wrdCreatorWord.value = p.word
  creatorOpen.value = true
}

async function deleteWrdPuzzle(p) {
  if (!canManagePuzzle(p) || !window.confirm('Slette dette ordet?')) return
  await puzzleApi.deleteWordle(p.id)
  allWrdPuzzles.value = allWrdPuzzles.value.filter(item => item.id !== p.id)
  if (selectedWrdId.value === p.id && allWrdPuzzles.value[0]) loadWrdPuzzle(allWrdPuzzles.value[0])
}

// ——— NATIVE GAME CREATOR ———————————————————————————————

const nativeCreating = ref(false)
const nativeCreatorError = ref('')
const nativeCreatorTitle = ref('')
const editingNativeId = ref(null)
const merCreatorRounds = ref([
  { metric: '', left: '', right: '', answer: 'left' },
  { metric: '', left: '', right: '', answer: 'left' },
  { metric: '', left: '', right: '', answer: 'left' },
  { metric: '', left: '', right: '', answer: 'left' },
  { metric: '', left: '', right: '', answer: 'left' },
])
const songCreator = ref({ answer: '', artist: '', title: '', trackId: null, previewUrl: null, searchQuery: '', searchResults: [] })
const cwCreatorForm = ref({
  title: '', rows: 7, cols: 7,
  clues: [{ n: 1, dir: 'D', row: 0, col: 0, answer: '', clue: '', searchQuery: '', searchResults: [], trackId: null, artist: '', title: '', previewUrl: null }],
})
const cwCreatorRows = computed(() => Math.min(15, Math.max(3, Number(cwCreatorForm.value.rows) || 7)))
const cwCreatorCols = computed(() => Math.min(15, Math.max(3, Number(cwCreatorForm.value.cols) || 7)))
const cwCreatorGrid = computed(() => buildCwGrid({
  rows: cwCreatorRows.value,
  cols: cwCreatorCols.value,
  clues: cwCreatorForm.value.clues
    .map(clue => ({
      n: Number(clue.n) || 0,
      dir: clue.dir === 'D' ? 'D' : 'A',
      row: Number(clue.row) || 0,
      col: Number(clue.col) || 0,
      answer: (clue.answer || '').trim().toUpperCase(),
    }))
    .filter(clue => clue.n > 0 && clue.answer),
}))
const cwCreatorFilledCells = computed(() => cwCreatorGrid.value.flat().filter(cell => !cell.black).length)

async function songCreatorSearch() {
  if (!songCreator.value.searchQuery.trim()) return
  const q = encodeURIComponent(songCreator.value.searchQuery.trim())
  try {
    const res = await fetch(`https://itunes.apple.com/search?term=${q}&media=music&limit=5`)
    const data = await res.json()
    songCreator.value.searchResults = data.results || []
  } catch {}
}

function songCreatorSelectTrack(track) {
  songCreator.value = {
    ...songCreator.value,
    answer: track.trackName.toUpperCase(),
    artist: track.artistName,
    title: track.trackName.toUpperCase(),
    trackId: track.trackId,
    previewUrl: track.previewUrl || null,
    searchResults: [],
  }
}

async function cwCreatorSearch(idx) {
  const clue = cwCreatorForm.value.clues[idx]
  if (!clue?.searchQuery?.trim()) return
  const q = encodeURIComponent(clue.searchQuery.trim())
  try {
    const res = await fetch(`https://itunes.apple.com/search?term=${q}&media=music&limit=5`)
    const data = await res.json()
    clue.searchResults = data.results || []
  } catch {}
}

function cwCreatorSelectTrack(idx, track) {
  const clue = cwCreatorForm.value.clues[idx]
  clue.trackId = track.trackId
  clue.artist = track.artistName
  clue.title = track.trackName.toUpperCase()
  clue.previewUrl = track.previewUrl || null
  clue.searchResults = []
}

function cwCreatorAddClue() {
  cwCreatorForm.value.clues.push({
    n: cwCreatorForm.value.clues.length + 1, dir: 'A', row: 0, col: 0,
    answer: '', clue: '', searchQuery: '', searchResults: [], trackId: null, artist: '', title: '', previewUrl: null,
  })
}
const timeCreator = ref({ prompt: '', year: new Date().getFullYear(), place: '', optionsText: '' })

function resetNativeCreator(gameId) {
  nativeCreatorTitle.value = ''
  editingNativeId.value = null
  if (gameId === 'merburet') {
    merCreatorRounds.value = [
      { metric: '', left: '', right: '', answer: 'left' },
      { metric: '', left: '', right: '', answer: 'left' },
      { metric: '', left: '', right: '', answer: 'left' },
      { metric: '', left: '', right: '', answer: 'left' },
      { metric: '', left: '', right: '', answer: 'left' },
    ]
  }
  if (gameId === 'latburet') songCreator.value = { answer: '', artist: '', title: '', trackId: null, previewUrl: null, searchQuery: '', searchResults: [] }
  if (gameId === 'kryssburet') {
    cwCreatorForm.value = {
      title: '', rows: 7, cols: 7,
      clues: [{ n: 1, dir: 'D', row: 0, col: 0, answer: '', clue: '', searchQuery: '', searchResults: [], trackId: null, artist: '', title: '', previewUrl: null }],
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
    if (!songCreator.value.answer.trim()) return null
    return {
      answer: songCreator.value.answer.trim().toUpperCase(),
      artist: songCreator.value.artist.trim(),
      title: songCreator.value.title.trim().toUpperCase(),
      trackId: songCreator.value.trackId || null,
      previewUrl: songCreator.value.previewUrl || null,
    }
  }
  if (gameId === 'kryssburet') {
    const { title, rows, cols, clues } = cwCreatorForm.value
    if (!title.trim()) return null
    const cleanClues = clues.map(c => ({
      n: c.n, dir: c.dir, row: c.row, col: c.col,
      answer: c.answer.trim().toUpperCase(), clue: c.clue.trim(),
      trackId: c.trackId || null, previewUrl: c.previewUrl || null,
    }))
    if (cleanClues.some(c => !c.answer || !c.clue)) return null
    return { title: title.trim(), rows: Number(rows), cols: Number(cols), clues: cleanClues }
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
  const gameTitle = game.id === 'kryssburet' ? cwCreatorForm.value.title.trim() : nativeCreatorTitle.value.trim()
  if (!gameTitle) {
    nativeCreatorError.value = 'Tittel må fylles ut.'
    return
  }
  const payload = buildNativeCreatorPayload(game.id)
  if (!payload) {
    nativeCreatorError.value = 'Fyll ut alle feltene.'
    return
  }
  nativeCreating.value = true
  try {
    const dto = {
      createdBy: displayName.value || 'Anonym',
      title: gameTitle,
      payloadJson: JSON.stringify(payload),
    }
    const data = editingNativeId.value
      ? await puzzleApi.updateNative(game.gameName, editingNativeId.value, dto)
      : await puzzleApi.createNative(game.gameName, dto)
    nativePuzzles.value = {
      ...nativePuzzles.value,
      [game.id]: editingNativeId.value
        ? (nativePuzzles.value[game.id] ?? []).map(p => p.id === data.id ? data : p)
        : [data, ...(nativePuzzles.value[game.id] ?? [])],
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

function editNativePuzzle(p) {
  if (!canManagePuzzle(p)) return
  const payload = parseNativePayload(p)
  if (!payload) return
  editingNativeId.value = p.id
  nativeCreatorTitle.value = puzzleDisplayTitle(p, activeNativeGame.value?.name || 'Spill')
  if (activeGame.value === 'merburet') {
    merCreatorRounds.value = (payload.rounds || []).map(r => ({
      metric: r.metric || '',
      left: r.left || '',
      right: r.right || '',
      answer: r.answer === 'right' ? 'right' : 'left',
    }))
  }
  if (activeGame.value === 'latburet') {
    songCreator.value = {
      answer: payload.answer || payload.title || '',
      artist: payload.artist || '',
      title: payload.title || payload.answer || '',
      trackId: payload.trackId || null,
      previewUrl: payload.previewUrl || null,
      searchQuery: `${payload.artist || ''} ${payload.title || payload.answer || ''}`.trim(),
      searchResults: [],
    }
  }
  if (activeGame.value === 'kryssburet') {
    cwCreatorForm.value = {
      title: p.title || payload.title || '',
      rows: payload.rows || 7,
      cols: payload.cols || 7,
      clues: (payload.clues || []).map(c => ({
        n: c.n,
        dir: c.dir,
        row: c.row,
        col: c.col,
        answer: c.answer || '',
        clue: c.clue || '',
        searchQuery: '',
        searchResults: [],
        trackId: c.trackId || null,
        artist: '',
        title: '',
        previewUrl: c.previewUrl || null,
      })),
    }
  }
  if (activeGame.value === 'tidsburet') {
    timeCreator.value = {
      prompt: payload.prompt || '',
      year: payload.year || new Date().getFullYear(),
      place: payload.place || '',
      optionsText: (payload.options || []).join(', '),
    }
  }
  creatorOpen.value = true
}

async function deleteNativePuzzle(p) {
  const game = activeNativeGame.value
  if (!game || !canManagePuzzle(p) || !window.confirm('Slette dette spillet?')) return
  await puzzleApi.deleteNative(game.gameName, p.id)
  nativePuzzles.value = {
    ...nativePuzzles.value,
    [game.id]: (nativePuzzles.value[game.id] ?? []).filter(item => item.id !== p.id),
  }
  if (selectedNativeIds.value[game.id] === p.id && nativePuzzles.value[game.id]?.[0]) {
    loadNativePuzzle(game.id, nativePuzzles.value[game.id][0])
  }
}

// ——— KEYBOARD HANDLER ——————————————————————————————————

function handleKeydown(e) {
  if (creatorOpen.value) return
  const k = e.key
  if (activeGame.value === 'wordle') {
    if (k === 'Backspace')                { e.preventDefault(); wrdKeyPress('DEL') }
    else if (k === 'Enter')               { e.preventDefault(); wrdKeyPress('ENTER') }
    else if (/^[a-zA-ZæøåÆØÅ]$/.test(k)) { e.preventDefault(); wrdKeyPress(k.toUpperCase()) }
  } else if (activeGame.value === 'kryssburet' && cwActiveCell.value && !cwDone.value) {
    if (k === 'Backspace') {
      e.preventDefault()
      const key = `${cwActiveCell.value.row},${cwActiveCell.value.col}`
      if (cwInput.value[key]) {
        const next = { ...cwInput.value }
        delete next[key]
        cwInput.value = next
      } else {
        cwMoveCursor(-1)
      }
    } else if (/^[a-zA-ZæøåÆØÅ]$/.test(k)) {
      e.preventDefault()
      cwInput.value = { ...cwInput.value, [`${cwActiveCell.value.row},${cwActiveCell.value.col}`]: k.toUpperCase() }
      cwCheckWin()
      cwMoveCursor(1)
    }
  }
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
  songStopPreview()
  if (_cwAudio) { _cwAudio.pause(); _cwAudio = null }
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
.puzzle-library-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}
.library-more {
  border: 0;
  background: transparent;
  color: var(--accent);
  font-family: var(--mono);
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  cursor: pointer;
}
.puzzle-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.puzzle-pick-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.puzzle-pick {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-width: 148px;
  min-height: 78px;
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
.puzzle-pick-title {
  max-width: 160px;
  color: var(--ink);
  font-weight: 800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.puzzle-pick-by {
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.puzzle-pick-date { font-size: 10px; opacity: 0.6; margin-top: 2px; }
.puzzle-pick-done { font-size: 10px; color: var(--accent); margin-top: 2px; }
.puzzle-manage {
  display: flex;
  gap: 6px;
}
.puzzle-manage button {
  border: 0;
  background: transparent;
  color: var(--ink-mute);
  cursor: pointer;
  font-family: var(--mono);
  font-size: 10px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}
.puzzle-manage button:hover { color: var(--accent-2); }

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
.conn-message {
  width: fit-content;
  margin: 14px auto 0;
  padding: 10px 18px;
  border: 1.5px solid var(--accent-2);
  border-radius: 999px;
  background: var(--ink);
  color: var(--bg);
  font-family: var(--mono);
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  box-shadow: 0 12px 28px rgba(18, 35, 29, 0.18);
}
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
.songless-steps {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}
.songless-step {
  min-width: 44px;
  padding: 8px 10px;
  border: 1px solid var(--line-soft);
  border-radius: 6px;
  font-family: var(--mono);
  color: var(--ink-mute);
}
.songless-step.active {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--bg-soft);
}
.songless-step:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}
.songless-play {
  min-width: 150px;
  min-height: 72px;
  padding: 18px 28px;
  border: 2px solid var(--line-soft);
  border-radius: 999px;
  font-family: var(--mono);
  font-size: 18px;
  color: var(--ink-soft);
}
.songless-play:not(:disabled):hover {
  border-color: var(--accent);
  color: var(--accent);
}
.songless-play:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.songless-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.songless-guess-wrap {
  position: relative;
  width: 100%;
}
.songless-suggestions {
  position: absolute;
  z-index: 5;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  overflow: hidden;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
  background: var(--bg);
  box-shadow: 0 16px 34px rgba(18, 35, 29, 0.16);
}
.songless-suggestion {
  width: 100%;
  padding: 10px 14px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  text-align: left;
  color: var(--ink);
}
.songless-suggestion:hover {
  background: var(--paper);
}
.songless-suggestion span {
  color: var(--ink-mute);
  white-space: nowrap;
}
.songless-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
.native-form {
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 10px;
}
.native-input { text-align: center; }

/* ── Crossword ───────────────────────────────── */
.cw-layout {
  display: grid;
  grid-template-columns: minmax(280px, auto) minmax(240px, 1fr);
  gap: 28px;
  align-items: start;
  width: 100%;
  text-align: left;
}
.cw-grid-wrap {
  display: flex;
  justify-content: center;
  width: 100%;
  overflow-x: auto;
}
.cw-grid {
  display: grid;
  gap: 2px;
  border: 2px solid var(--ink);
  background: var(--ink);
  width: min(100%, calc(var(--cw-cols, 9) * 46px));
}
.cw-cell {
  aspect-ratio: 1;
  min-width: 28px;
  background: var(--bg);
  position: relative;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  user-select: none;
}
.cw-cell.cw-black { background: var(--ink); cursor: default; }
.cw-cell.cw-highlighted { background: #d4edff; }
.cw-cell.cw-active { background: #a8d8f0; outline: 2px solid var(--accent); outline-offset: -2px; }
.cw-num {
  position: absolute; top: 2px; left: 3px;
  font-family: var(--mono); font-size: 9px; font-weight: 600;
  color: var(--ink-mute); line-height: 1;
}
.cw-letter {
  font-family: var(--sans); font-size: 18px; font-weight: 700;
  color: var(--ink); text-transform: uppercase;
}
.cw-clues {
  display: flex; flex-direction: column; gap: 20px;
  max-height: 360px; overflow-y: auto;
}
.cw-clues-section { display: flex; flex-direction: column; gap: 4px; }
.cw-clues-heading {
  font-family: var(--mono); font-size: 11px; text-transform: uppercase;
  letter-spacing: 0.12em; color: var(--ink-mute); margin-bottom: 4px; font-weight: 600;
}
.cw-clue-row {
  display: flex; align-items: center; gap: 10px;
  padding: 5px 8px; border-radius: var(--radius);
  cursor: pointer; transition: background 0.15s;
}
.cw-clue-row:hover { background: var(--paper); }
.cw-clue-row.cw-clue-active { background: var(--paper); }
.cw-badge {
  flex-shrink: 0; min-width: 34px; height: 22px; border-radius: 4px;
  font-family: var(--mono); font-size: 11px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; padding: 0 4px;
}
.cw-badge-0 { background: #f59e0b; color: #fff; }
.cw-badge-1 { background: #fbbf24; color: #fff; }
.cw-badge-2 { background: #38bdf8; color: #fff; }
.cw-badge-3 { background: #f87171; color: #fff; }
.cw-badge-4 { background: #4ade80; color: #fff; }
.cw-badge-5 { background: #2dd4bf; color: #fff; }
.cw-badge-6 { background: #a78bfa; color: #fff; }
.cw-badge-7 { background: #fb7185; color: #fff; }
.cw-clue-text { font-size: 13px; color: var(--ink-soft); }

.cw-player-bar {
  width: 100%; margin-top: 20px;
  padding: 14px 20px;
  background: var(--bg); border: 1px solid var(--line-soft); border-radius: var(--radius);
  display: flex; justify-content: space-between; align-items: center; gap: 16px;
}
.cw-player-clue { display: flex; align-items: center; gap: 12px; flex: 1; min-width: 0; }
.cw-player-text { font-size: 14px; color: var(--ink); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.cw-player-controls { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.cw-ctrl-btn {
  width: 38px; height: 38px; border-radius: 50%;
  border: 1.5px solid var(--line-soft); background: transparent;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; cursor: pointer; transition: all 0.15s;
}
.cw-ctrl-btn:hover { border-color: var(--ink); background: var(--paper); }
.cw-play-btn {
  width: 46px; height: 46px;
  background: var(--ink); color: var(--bg); border-color: var(--ink); font-size: 16px;
}
.cw-play-btn:hover:not(:disabled) { background: var(--accent); border-color: var(--accent); }
.cw-play-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.cw-hint-btn {
  padding: 10px 14px;
  font-size: 11px;
}
.cw-submit-row {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}

@media (max-width: 760px) {
  .cw-layout { grid-template-columns: 1fr; }
  .cw-clues { max-height: none; }
  .cw-player-bar { flex-direction: column; align-items: stretch; }
  .cw-player-controls { justify-content: center; }
  .cw-submit-row { flex-direction: column; align-items: stretch; }
}

.music-search-results {
  display: flex; flex-direction: column; gap: 4px; margin-top: 8px;
}
.music-search-result {
  text-align: left; padding: 8px 12px;
  border: 1px solid var(--line-soft); border-radius: var(--radius);
  font-size: 13px; cursor: pointer; background: var(--bg); transition: background 0.15s;
}
.music-search-result:hover { background: var(--paper); }
.cw-creator-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 420px);
  gap: 18px;
  align-items: start;
}
.cw-creator-fields {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}
.cw-creator-position-row {
  display: grid;
  grid-template-columns: 60px 84px 60px 60px;
  gap: 8px;
}
.cw-creator-preview {
  position: sticky;
  top: 18px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
  padding: 14px;
  background: var(--bg);
}
.cw-creator-preview-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  font-family: var(--mono);
  font-size: 11px;
  color: var(--ink-mute);
}
.cw-creator-grid-wrap {
  width: 100%;
  overflow-x: auto;
}
.cw-creator-grid {
  display: grid;
  gap: 1px;
  width: min(100%, calc(var(--cw-cols, 7) * 34px));
  background: var(--ink);
  border: 2px solid var(--ink);
}
.cw-creator-cell {
  aspect-ratio: 1;
  min-width: 22px;
  background: var(--bg);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cw-creator-cell.cw-black { background: var(--ink); }
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
  .songless-actions { flex-direction: column; }
  .native-game-panel { padding: 32px 18px; }
  .cw-layout { grid-template-columns: 1fr; }
  .cw-cell { width: 36px; height: 36px; }
  .cw-letter { font-size: 14px; }
  .cw-clues { max-height: none; }
  .cw-creator-layout { grid-template-columns: 1fr; }
  .cw-creator-preview { position: static; }
  .cw-creator-position-row { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>
