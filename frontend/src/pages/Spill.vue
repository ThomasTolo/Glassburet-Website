<template>
  <section v-if="isAuthenticated">
    <div class="page-header">
      <div>
        <span class="eyebrow">Daglige spill · 7 tilgjengelig</span>
        <h1 class="display" style="font-size: clamp(40px, 6vw, 84px); margin: 12px 0 16px;">
          Dagens <em>spill</em>.
        </h1>
        <p class="lede">Daglige utfordringer for deg og vennegjengen. Hvem er egentlig skarpest?</p>
      </div>
      <div class="page-header-meta">
        {{ dateLabel }}<br />
        {{ semesterLabel }}<br />
        7 SPILL I DAG
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
        <div class="game-side-tools">
          <button class="ab-help-btn" type="button" @click="toggleGameHelp('connections')" :class="{ active: isGameHelpOpen('connections') }">?</button>
          <div class="conn-mistakes-box">
            <span class="eyebrow">Feil igjen</span>
            <div class="mistake-dots">
              <span v-for="i in 4" :key="i" :class="['mdot', i <= (4 - connMistakes) ? 'active' : '']" />
            </div>
          </div>
        </div>
      </div>
      <div v-if="isGameHelpOpen('connections')" class="ab-help-panel game-help-panel">
        <div v-for="row in gameHelp.connections" :key="row.title" class="ab-help-row">
          <span :class="['ab-attr', 'ab-help-tile', row.status && `ab-${row.status}`]">{{ row.title }}</span>
          <span class="ab-help-text">{{ row.text }}</span>
        </div>
      </div>

      <!-- Puzzle library -->
      <div class="puzzle-library">
        <div class="puzzle-library-head">
          <span class="eyebrow">Velg puzzle</span>
          <button v-if="allConnPuzzles.length > STANDARD_LIBRARY_COLLAPSED_LIMIT" type="button" class="library-more" @click="connLibraryExpanded = !connLibraryExpanded">
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
        <p>Fasit vises ikke i nettleseren før den er løst.</p>
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
        <div class="game-side-tools">
          <button class="ab-help-btn" type="button" @click="toggleGameHelp('wordle')" :class="{ active: isGameHelpOpen('wordle') }">?</button>
          <div class="wrd-legend">
            <div class="wrd-legend-item"><span class="wrd-sq correct" /> Riktig posisjon</div>
            <div class="wrd-legend-item"><span class="wrd-sq present" /> Feil posisjon</div>
            <div class="wrd-legend-item"><span class="wrd-sq absent" /> Ikke i ordet</div>
          </div>
        </div>
      </div>
      <div v-if="isGameHelpOpen('wordle')" class="ab-help-panel game-help-panel">
        <div v-for="row in gameHelp.wordle" :key="row.title" class="ab-help-row">
          <span :class="['ab-attr', 'ab-help-tile', row.status && `ab-${row.status}`]">{{ row.title }}</span>
          <span class="ab-help-text">{{ row.text }}</span>
        </div>
      </div>

      <!-- Puzzle library -->
      <div class="puzzle-library">
        <div class="puzzle-library-head">
          <span class="eyebrow">Velg ord</span>
          <button v-if="allWrdPuzzles.length > STANDARD_LIBRARY_COLLAPSED_LIMIT" type="button" class="library-more" @click="wrdLibraryExpanded = !wrdLibraryExpanded">
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
        <p>Du gjettet <strong>{{ wrdGuesses[wrdGuesses.length - 1] }}</strong> på {{ wrdGuesses.length }} forsøk! <strong>{{ wrdScore }} poeng</strong></p>
        <p v-if="wrdScoreSubmitted" class="score-saved">✓ Poeng registrert!</p>
        <p v-else-if="!isAuthenticated" class="score-saved" style="color:var(--ink-mute)">Logg inn for å registrere poeng</p>
      </div>

      <div v-if="wrdGameState === 'lost'" class="sp-result">
        <div class="sp-result-icon">😔</div>
        <h3>Bedre lykke neste gang!</h3>
        <p>Ordet vises ikke i nettleseren før spillet er ferdig validert.</p>
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
        <div class="native-title-row">
          <h2 class="section-title">{{ activeNativeGame.name }}</h2>
          <button class="ab-help-btn" type="button" @click="toggleGameHelp(activeGame)" :class="{ active: isGameHelpOpen(activeGame) }">?</button>
        </div>
        <p class="sp-sub">
          {{ activeNativeGame.description }}
        </p>
        <a class="source-button" :href="activeNativeGame.url" target="_blank" rel="noopener noreferrer">Inspirasjon</a>
        <div v-if="isGameHelpOpen(activeGame)" class="ab-help-panel game-help-panel native-help-panel">
          <div v-for="row in gameHelp[activeGame]" :key="row.title" class="ab-help-row">
            <span :class="['ab-attr', 'ab-help-tile', row.status && `ab-${row.status}`]">{{ row.title }}</span>
            <span class="ab-help-text">{{ row.text }}</span>
          </div>
        </div>

        <div class="puzzle-library native-library">
          <div class="puzzle-library-head">
            <span class="eyebrow">Velg spill</span>
            <button v-if="activeNativePuzzles.length > NATIVE_LIBRARY_COLLAPSED_LIMIT" type="button" class="library-more" @click="toggleNativeLibrary">
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
                :disabled="idx !== songStep"
                type="button"
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
                <div class="cw-grid" :style="{ '--cw-cols': cwVisibleCols }">
                  <div
                    v-for="cell in cwVisibleGrid.flat()"
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
                <button class="conn-btn cw-hint-btn" type="button" :disabled="!cwCurrentClue" @click="cwUseHint">Hint</button>
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
          <template v-if="activeNativeGameState !== 'completed'">
            <h3 class="native-prompt">{{ timePuzzle.prompt }}</h3>
            <p v-if="timeRoundTotal > 1" class="sp-sub">Runde {{ timeRoundIndex + 1 }} av {{ timeRoundTotal }}</p>
            <div class="time-play-layout">
              <div class="time-photo-col">
                <img v-if="timePuzzle.imageUrl" class="time-photo" :src="timePuzzle.imageUrl" alt="" />
                <p v-if="timeDone && timePuzzle.attribution" class="time-attribution">{{ timePuzzle.attribution }}</p>
              </div>
              <div class="time-control-col">
                <div ref="timeMapEl" class="time-map"></div>
                <template v-if="!timeDone">
                  <div class="time-grid">
                    <label class="creator-label">
                      År
                      <input v-model.number="timeYear" class="creator-input native-input" type="number" min="1800" max="2026" @input="saveTimeProgress" />
                    </label>
                  </div>
                  <button class="conn-btn conn-btn-primary" :disabled="!timeGuess" @click="timeSubmit">Send inn</button>
                </template>
                <div v-else class="time-result-card">
                  <div class="sp-result-icon">◷</div>
                  <h3>{{ timeResult.year }} · {{ timeResult.locationName }}</h3>
                  <p>{{ timeResult.distanceKm }} km unna</p>
                  <p>{{ timeResult.yearScore }} årspoeng · {{ timeResult.locationScore }} kartpoeng</p>
                  <p><strong>{{ timeResult.score || 0 }} poeng denne runden · {{ timeScore }} totalt</strong></p>
                  <button class="conn-btn conn-btn-primary" type="button" @click="finishTimeResult">
                    {{ timeRoundIndex < timeRoundTotal - 1 ? 'Neste' : 'Ferdig' }}
                  </button>
                </div>
              </div>
            </div>
          </template>
        </div>

        <div v-if="activeGame === 'artistburet'" class="native-game-body ab-body">
          <template v-if="activeNativeGameState !== 'completed'">

            <!-- Top bar with counter + help -->
            <div class="ab-top-bar">
              <p class="ab-counter">{{ MAX_ARTIST_GUESSES - artistGuesses.length }} av {{ MAX_ARTIST_GUESSES }} forsøk igjen</p>
            </div>

            <!-- Guess cards -->
            <div class="ab-guesses">
              <div v-for="(g, idx) in artistGuesses" :key="idx" class="ab-guess-card">
                <div class="ab-guess-header">
                  <div class="ab-avatar" :class="{ 'ab-avatar-won': g.correct }">{{ artistInitials(g.name) }}</div>
                  <span class="ab-artist-name">{{ g.name }}</span>
                  <span v-if="g.correct" class="ab-correct-badge">✓</span>
                </div>
                <div class="ab-attrs">
                  <div :class="['ab-attr', `ab-${g.debutStatus}`]">
                    <span class="ab-attr-label">ÅR</span>
                    <span class="ab-attr-val">{{ g.debut }}<span v-if="g.debutStatus !== 'match'" class="ab-dir">{{ g.debutDirection === 'up' ? ' ↑' : ' ↓' }}</span></span>
                  </div>
                  <div :class="['ab-attr', `ab-${g.membersStatus}`]">
                    <span class="ab-attr-label">ANTALL</span>
                    <span class="ab-attr-val">{{ g.members }}</span>
                  </div>
                  <div :class="['ab-attr', `ab-${g.popularityStatus}`]">
                    <span class="ab-attr-label">POPULARITET</span>
                    <span class="ab-attr-val">#{{ g.popularity }}<span v-if="g.popularityStatus !== 'match'" class="ab-dir">{{ g.popularityDirection === 'up' ? ' ↑' : ' ↓' }}</span></span>
                  </div>
                  <div :class="['ab-attr', `ab-${g.genderStatus}`]">
                    <span class="ab-attr-label">KJØNN</span>
                    <span class="ab-attr-val">{{ g.gender }}</span>
                  </div>
                  <div :class="['ab-attr', `ab-${g.genreStatus}`]">
                    <span class="ab-attr-label">SJANGER</span>
                    <span class="ab-attr-val">{{ g.genre }}</span>
                  </div>
                  <div :class="['ab-attr', `ab-${g.countryStatus}`]">
                    <span class="ab-attr-label">LAND</span>
                    <span class="ab-attr-val ab-flag">{{ flagEmoji(g.country) }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Input -->
            <div v-if="!artistDone" class="ab-input-wrap">
              <div class="ab-input-row">
                <div class="ab-input-container">
                  <input
                    v-model="artistInput"
                    class="creator-input ab-search-input"
                    placeholder="Skriv artistnavn..."
                    autocomplete="off"
                    @input="artistSearchSuggestions"
                    @keyup.enter.prevent="artistSubmit"
                    @blur="artistDeferHideSuggestions"
                  />
                  <div v-if="artistSuggestions.length" class="ab-suggestions">
                    <button
                      v-for="s in artistSuggestions"
                      :key="s.name"
                      type="button"
                      class="ab-suggestion"
                      @mousedown.prevent="artistSelectSuggestion(s)"
                    >
                      <span class="ab-sug-initials">{{ artistInitials(s.name) }}</span>
                      <span>{{ s.name }}</span>
                    </button>
                  </div>
                </div>
                <button class="conn-btn conn-btn-primary" type="button" @click="artistSubmit">Gjett</button>
              </div>
              <p v-if="artistAlreadyGuessed" class="wrd-message">Allerede gjettet!</p>
              <p v-if="artistNotFound" class="wrd-message">Fant ikke artisten i listen.</p>
            </div>

            <!-- Done -->
            <div v-if="artistDone" class="sp-result compact-result">
              <div class="sp-result-icon">{{ artistWon ? '♪' : '✕' }}</div>
              <h3>{{ artistWon ? `${artistAnswer.name}!` : `Svaret var: ${artistAnswer.name}` }}</h3>
              <p><strong>{{ artistScore }} poeng</strong></p>
              <p v-if="nativeSubmitted.artistburet" class="score-saved">✓ Poeng registrert!</p>
              <p v-else-if="!isAuthenticated" class="score-saved" style="color:var(--ink-mute)">Logg inn for å registrere poeng</p>
            </div>
          </template>

          <div v-if="activeNativeGameState === 'completed'" class="sp-result compact-result">
            <div class="sp-result-icon">✓</div>
            <h3>Allerede fullført</h3>
            <p>Du har allerede spilt dette spillet i dag.</p>
          </div>
        </div>

        <div v-if="activeGame !== 'artistburet'" class="creator-wrap native-creator">
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
              <input v-model="timeCreator.imageUrl" class="creator-input" placeholder="Bilde-URL" required />
              <div class="creator-words-row">
                <input v-model.number="timeCreator.year" class="creator-input" type="number" placeholder="År" required />
                <input v-model="timeCreator.locationName" class="creator-input" placeholder="Stedsnavn" required />
              </div>
              <div class="creator-words-row">
                <input v-model.number="timeCreator.lat" class="creator-input" type="number" step="0.0001" placeholder="Latitude" required />
                <input v-model.number="timeCreator.lng" class="creator-input" type="number" step="0.0001" placeholder="Longitude" required />
              </div>
              <input v-model="timeCreator.attribution" class="creator-input" placeholder="Fotokreditering / lisens" required />
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
  <section v-else class="sp-login-required">
    <h1 class="section-title">Logg inn for å spille</h1>
    <p class="sp-sub">Spillene er kun tilgjengelige for medlemmer.</p>
    <RouterLink class="conn-btn conn-btn-primary" to="/login">Logg inn</RouterLink>
  </section>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { puzzleApi, scoreApi } from '../services/api'
import { useLiveDateInfo } from '../composables/useLiveDateInfo'
import { isAuthenticated, displayName, isAdminOrAbove } from '../services/authState'
import { subscribeToUpdates } from '../services/realtime'

const activeGame = ref('connections')
const { dateLabel, semesterLabel } = useLiveDateInfo({ intervalMs: 60000 })
const gameHelpOpen = ref({})
const gameHelp = {
  connections: [
    { title: 'MÅL', text: 'Finn fire grupper med fire ord som hører sammen.' },
    { title: 'SPILL', text: 'Velg nøyaktig fire ord og send inn. Riktige grupper låses og fjernes fra brettet.' },
    { title: 'FEIL', text: 'Feil gruppe koster ett forsøk. Fire feil avslutter spillet.' },
    { title: 'POENG', text: 'Du får 1000 poeng minus 125 for hver feil. Tapt spill gir 0 poeng.' },
  ],
  wordle: [
    { title: 'MÅL', text: 'Gjett det fem bokstaver lange ordet på maks seks forsøk.' },
    { title: 'GRØNN', status: 'match', text: 'Riktig bokstav på riktig plass.' },
    { title: 'GUL', status: 'close', text: 'Riktig bokstav, men feil plass.' },
    { title: 'GRÅ', status: 'no-match', text: 'Bokstaven er ikke i ordet.' },
    { title: 'ORD', text: 'Gjetningen må være et gyldig ord i ordlisten.' },
    { title: 'POENG', text: 'Du får 1000 poeng minus 125 for hvert forsøk brukt før riktig svar. Seks feil gir 0 poeng.' },
  ],
  merburet: [
    { title: 'MÅL', text: 'Velg siden som har riktig størst tallverdi i hver duell.' },
    { title: 'RUNDER', text: 'Du spiller alle rundene i valgt spill. Etter hvert svar går spillet automatisk videre.' },
    { title: 'POENG', text: 'Hvert riktig svar gir 200 poeng. Feil svar gir 0 poeng for den runden.' },
  ],
  latburet: [
    { title: 'MÅL', text: 'Finn sangtittelen ved å lytte til korte klipp.' },
    { title: 'KLIPP', text: 'Du starter med 0,5 sekunder. Feil svar eller hopp over låser opp lengre klipp: 1, 2, 4, 8 og 10 sekunder.' },
    { title: 'SVAR', text: 'Skriv eller velg sangtittel fra forslagene. Bare tittelen sjekkes.' },
    { title: 'POENG', text: 'Riktig svar gir opptil 1000 poeng, minus 150 for hvert lengre klipp som er låst opp. Bom på siste nivå gir 0 poeng.' },
  ],
  kryssburet: [
    { title: 'MÅL', text: 'Fyll inn hele musikk-kryssordet med riktige sangtitler.' },
    { title: 'SPILL', text: 'Klikk på en rute eller ledetråd for å velge ord. Spill av lydklippet når det finnes.' },
    { title: 'HINT', text: 'Hint fyller inn aktiv bokstav i valgt ord.' },
    { title: 'POENG', text: 'Et komplett riktig kryssord gir 1000 poeng. Innsending med feil gir 0 poeng.' },
  ],
  tidsburet: [
    { title: 'MÅL', text: 'Gjett både årstall og sted for bildet eller hendelsen.' },
    { title: 'KART', text: 'Sett markøren på kartet og skriv inn år før du sender inn.' },
    { title: 'POENG', text: 'Hver runde gir opptil 500 årspoeng og 500 kartpoeng. År mister 20 poeng per år unna, kart mister 1 poeng per 10 km unna.' },
    { title: 'TOTALT', text: 'Har spillet flere runder, summeres poengene fra alle rundene.' },
  ],
  artistburet: [
    { title: 'MÅL', text: 'Gjett dagens artist eller gruppe på maks 10 forsøk.' },
    { title: 'HINT', text: 'Etter hver gjetning får du treff på debutår, antall medlemmer, popularitet, kjønn, sjanger og land.' },
    { title: 'GRØNN', status: 'match', text: 'Attributtet matcher riktig artist.' },
    { title: 'GUL', status: 'close', text: 'Nær match: debutår innen ±5 år eller popularitet innen ±50 plasser.' },
    { title: 'GRÅ', status: 'no-match', text: 'Attributtet matcher ikke riktig artist.' },
    { title: 'PILER', text: 'Pil opp eller ned viser retningen mot riktig debutår eller popularitetsrangering.' },
    { title: 'POENG', text: 'Riktig på første forsøk gir 1000 poeng. Hvert ekstra forsøk trekker 100 poeng. Ingen riktig gjetning på 10 forsøk gir 0 poeng.' },
  ],
}
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
  {
    id: 'artistburet',
    name: 'Artistburet',
    description: 'Gjett dagens artist på 10 forsøk ved hjelp av hint.',
    gameName: 'ARTISTBURET',
    url: 'https://spotle.io',
    icon: '♪',
  },
]
const activeNativeGame = computed(() => nativeGames.find(game => game.id === activeGame.value))
const nativePuzzles = ref({ merburet: [], latburet: [], kryssburet: [], tidsburet: [], artistburet: [] })
const selectedNativeIds = ref({})
const completedNativeIds = ref({ merburet: [], latburet: [], kryssburet: [], tidsburet: [], artistburet: [] })
const nativeSubmitted = ref({})
const activeNativePuzzles = computed(() => nativePuzzles.value[activeGame.value] ?? [])
const activeNativeCompletedIds = computed(() => completedNativeIds.value[activeGame.value] ?? [])
const STANDARD_LIBRARY_COLLAPSED_LIMIT = 4
const NATIVE_LIBRARY_COLLAPSED_LIMIT = 3
const connLibraryExpanded = ref(false)
const wrdLibraryExpanded = ref(false)
const nativeLibraryExpanded = ref({})
const visibleConnPuzzles = computed(() => connLibraryExpanded.value ? allConnPuzzles.value : allConnPuzzles.value.slice(0, STANDARD_LIBRARY_COLLAPSED_LIMIT))
const visibleWrdPuzzles = computed(() => wrdLibraryExpanded.value ? allWrdPuzzles.value : allWrdPuzzles.value.slice(0, STANDARD_LIBRARY_COLLAPSED_LIMIT))
const visibleNativePuzzles = computed(() => nativeLibraryExpanded.value[activeGame.value] ? activeNativePuzzles.value : activeNativePuzzles.value.slice(0, NATIVE_LIBRARY_COLLAPSED_LIMIT))
const activeNativeGameState = computed(() => {
  const id = selectedNativeIds.value[activeGame.value]
  return id && activeNativeCompletedIds.value.includes(id) ? 'completed' : 'playing'
})

function toggleGameHelp(gameId) {
  gameHelpOpen.value = {
    ...gameHelpOpen.value,
    [gameId]: !gameHelpOpen.value[gameId],
  }
}

function isGameHelpOpen(gameId) {
  return Boolean(gameHelpOpen.value[gameId])
}

const PROGRESS_KEY_PREFIX = 'glassburet:spill-progress'

function progressKey(gameId, puzzleId) {
  if (!displayName.value || !puzzleId) return null
  return `${PROGRESS_KEY_PREFIX}:${displayName.value}:${gameId}:${puzzleId}`
}

function loadProgress(gameId, puzzleId) {
  const key = progressKey(gameId, puzzleId)
  if (!key) return null
  try {
    return JSON.parse(localStorage.getItem(key))
  } catch {
    return null
  }
}

function saveProgress(gameId, puzzleId, data) {
  const key = progressKey(gameId, puzzleId)
  if (key) localStorage.setItem(key, JSON.stringify(data))
}

function clearProgress(gameId, puzzleId) {
  const key = progressKey(gameId, puzzleId)
  if (key) localStorage.removeItem(key)
}

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
    clearProgress(gameId, selectedNativeIds.value[gameId])
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
  if (gameId === 'artistburet') applyArtistPuzzle(payload)
}

function saveNativeProgress(gameId, data) {
  saveProgress(gameId, selectedNativeIds.value[gameId], data)
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
  const saved = loadProgress('merburet', selectedNativeIds.value.merburet)
  if (saved) {
    merIndex.value = Math.min(Number(saved.index) || 0, Math.max(0, merRounds.value.length - 1))
    merCorrect.value = Number(saved.correct) || 0
    merDone.value = Boolean(saved.done)
  }
}

async function merChoose(choice) {
  if (merDone.value) return
  const result = await puzzleApi.validateNative('MORE_OR_LESS', selectedNativeIds.value.merburet, { index: merIndex.value, choice })
  if (result.correct) {
    merCorrect.value++
    merMessage.value = 'Riktig'
  } else {
    merMessage.value = 'Feil'
  }
  setTimeout(() => {
    merMessage.value = ''
    if (merIndex.value >= merRounds.value.length - 1) {
      merDone.value = true
      saveNativeProgress('merburet', { index: merIndex.value, correct: merCorrect.value, done: merDone.value })
      if (isAuthenticated.value) submitNativeScore('merburet', merScore.value)
      return
    }
    merIndex.value++
    saveNativeProgress('merburet', { index: merIndex.value, correct: merCorrect.value, done: merDone.value })
  }, 450)
}

// ——— LÅTBURET ——————————————————————————————————————————

const songDurations = [0.5, 1, 2, 4, 8, 10]
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
  const saved = loadProgress('latburet', selectedNativeIds.value.latburet)
  if (saved) {
    songGuess.value = saved.guess || ''
    songGuesses.value = Array.isArray(saved.guesses) ? saved.guesses : []
    songStep.value = Number(saved.step) || 0
    songDone.value = Boolean(saved.done)
    songWon.value = Boolean(saved.won)
  }
}

async function songSubmit() {
  if (songDone.value || !songGuess.value.trim()) return
  const guess = songGuess.value.trim()
  songGuesses.value = [...songGuesses.value, guess]
  songGuess.value = ''
  songSuggestions.value = []
  const result = await puzzleApi.validateNative('SONGLESS', selectedNativeIds.value.latburet, { guess })
  if (result.correct) {
    songPuzzle.value = { ...songPuzzle.value, answer: result.answer || guess }
    songWon.value = true
    songDone.value = true
    songStopPreview()
    saveNativeProgress('latburet', { guess: songGuess.value, guesses: songGuesses.value, step: songStep.value, done: songDone.value, won: songWon.value })
    if (isAuthenticated.value) submitNativeScore('latburet', songScore.value)
    return
  }
  if (songStep.value >= songDurations.length - 1) {
    songDone.value = true
    songStopPreview()
    saveNativeProgress('latburet', { guess: songGuess.value, guesses: songGuesses.value, step: songStep.value, done: songDone.value, won: songWon.value })
    if (isAuthenticated.value) submitNativeScore('latburet', 0)
    return
  }
  songStep.value++
  saveNativeProgress('latburet', { guess: songGuess.value, guesses: songGuesses.value, step: songStep.value, done: songDone.value, won: songWon.value })
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
    saveNativeProgress('latburet', { guess: songGuess.value, guesses: songGuesses.value, step: songStep.value, done: songDone.value, won: songWon.value })
    if (isAuthenticated.value) submitNativeScore('latburet', 0)
    return
  }
  songStep.value++
  saveNativeProgress('latburet', { guess: songGuess.value, guesses: songGuesses.value, step: songStep.value, done: songDone.value, won: songWon.value })
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
const cwVisibleGrid = computed(() => cropCwGrid(cwGrid.value))
const cwVisibleCols = computed(() => cwVisibleGrid.value[0]?.length || cwPuzzle.value?.cols || 9)
const cwSelectedCellKeys = computed(() => {
  const clue = cwCurrentClue.value
  if (!clue) return new Set()
  const keys = new Set()
  const length = clue.answer?.length || clue.length || 0
  for (let i = 0; i < length; i++) {
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
    const length = clue.answer?.length || clue.length || 0
    for (let i = 0; i < length; i++) {
      const r = clue.dir === 'D' ? clue.row + i : clue.row
      const c = clue.dir === 'A' ? clue.col + i : clue.col
      if (r >= 0 && r < rows && c >= 0 && c < cols) {
        grid[r][c].black = false
        grid[r][c].answer = clue.answer?.[i] || ''
        grid[r][c].clueRefs.push({ n: clue.n, dir: clue.dir })
      }
    }
    if (clue.row >= 0 && clue.row < rows && clue.col >= 0 && clue.col < cols) {
      grid[clue.row][clue.col].number = clue.n
    }
  }
  return grid
}

function cropCwGrid(grid) {
  const filled = grid.flat().filter(cell => !cell.black)
  if (!filled.length) return grid
  const minRow = Math.max(0, Math.min(...filled.map(cell => cell.row)) - 1)
  const maxRow = Math.min(grid.length - 1, Math.max(...filled.map(cell => cell.row)) + 1)
  const minCol = Math.max(0, Math.min(...filled.map(cell => cell.col)) - 1)
  const maxCol = Math.min((grid[0]?.length || 1) - 1, Math.max(...filled.map(cell => cell.col)) + 1)
  return grid.slice(minRow, maxRow + 1).map(row => row.slice(minCol, maxCol + 1))
}

function getCluePositions(clue) {
  const positions = []
  const length = clue.answer?.length || clue.length || 0
  for (let i = 0; i < length; i++) {
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
  const saved = loadProgress('kryssburet', selectedNativeIds.value.kryssburet)
  if (saved) {
    cwInput.value = saved.input || {}
    cwSelected.value = saved.selected || cwSelected.value
    cwActiveCell.value = saved.activeCell || null
    cwDone.value = Boolean(saved.done)
    cwWon.value = Boolean(saved.won)
    if (!cwActiveCell.value) cwPrimeActiveCell()
  }
}

function normalizeCwAudioTitle(value) {
  return String(value || '').toUpperCase().replace(/[^A-Z0-9]/g, '')
}

function cwPrimeActiveCell() {
  if (!cwCurrentClue.value) return null
  const positions = getCluePositions(cwCurrentClue.value)
  if (cwActiveCell.value) {
    const active = cwGrid.value[cwActiveCell.value.row]?.[cwActiveCell.value.col]
    if (active && !active.black && positions.some(p => p.row === cwActiveCell.value.row && p.col === cwActiveCell.value.col)) {
      return cwActiveCell.value
    }
  }
  cwActiveCell.value = positions.find(p => !cwInput.value[`${p.row},${p.col}`]) || positions[0] || null
  return cwActiveCell.value
}

watch(cwSelected, (sel) => {
  if (!sel || !cwPuzzle.value) return
  const clue = cwPuzzle.value.clues.find(c => c.n === sel.n && c.dir === sel.dir)
  if (!clue) return
  const positions = getCluePositions(clue)
  const firstUnfilled = positions.find(p => !cwInput.value[`${p.row},${p.col}`])
  cwActiveCell.value = firstUnfilled || positions[0] || null
})

watch(activeGame, (game) => {
  if (game === 'tidsburet') nextTick(initTimeMap)
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
    if (sameDir) {
      if (cwSelected.value.n !== sameDir.n || cwSelected.value.dir !== sameDir.dir) cwStopPreview()
      cwSelected.value = { n: sameDir.n, dir: sameDir.dir }
      return
    }
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

function cwMoveByArrow(key) {
  if (cwDone.value) return
  if (!cwPrimeActiveCell()) return
  const moves = {
    ArrowLeft: [0, -1, 'A'],
    ArrowRight: [0, 1, 'A'],
    ArrowUp: [-1, 0, 'D'],
    ArrowDown: [1, 0, 'D'],
  }
  const move = moves[key]
  if (!move) return
  const [rowStep, colStep, preferredDir] = move
  let nextRow = cwActiveCell.value.row + rowStep
  let nextCol = cwActiveCell.value.col + colStep
  let nextCell = cwGrid.value[nextRow]?.[nextCol]
  while (nextCell && nextCell.black) {
    nextRow += rowStep
    nextCol += colStep
    nextCell = cwGrid.value[nextRow]?.[nextCol]
  }
  if (!nextCell) return
  const refs = nextCell.clueRefs || []
  const prevSelection = cwSelected.value
  const sameSelection = prevSelection && refs.some(r => r.n === prevSelection.n && r.dir === prevSelection.dir)
  if (!sameSelection) cwStopPreview()
  cwActiveCell.value = { row: nextRow, col: nextCol }
  if (!refs.length) return
  const preferred = refs.find(r => r.dir === preferredDir)
  const sameDir = prevSelection && refs.find(r => r.dir === prevSelection.dir)
  const nextSelection = preferred || sameDir || refs[0]
  cwSelected.value = { n: nextSelection.n, dir: nextSelection.dir }
}

async function cwCheckWin() {
  const allCells = cwGrid.value.flatMap(r => r).filter(c => !c.black)
  if (!allCells.every(c => cwInput.value[`${c.row},${c.col}`])) return
  const result = await puzzleApi.validateNative('CROSSTUNES', selectedNativeIds.value.kryssburet, { input: cwInput.value })
  if (result.correct) {
    cwDone.value = true
    cwWon.value = true
    saveNativeProgress('kryssburet', { input: cwInput.value, selected: cwSelected.value, activeCell: cwActiveCell.value, done: cwDone.value, won: cwWon.value })
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

async function cwSubmit() {
  if (cwDone.value) return
  const allCells = cwGrid.value.flatMap(r => r).filter(c => !c.black)
  if (!allCells.every(c => cwInput.value[`${c.row},${c.col}`])) {
    cwMessage.value = 'Fyll ut hele rutenettet før du sender inn.'
    setTimeout(() => { cwMessage.value = '' }, 1600)
    return
  }
  const result = await puzzleApi.validateNative('CROSSTUNES', selectedNativeIds.value.kryssburet, { input: cwInput.value })
  cwWon.value = Boolean(result.correct)
  cwDone.value = true
  cwStopPreview()
  saveNativeProgress('kryssburet', { input: cwInput.value, selected: cwSelected.value, activeCell: cwActiveCell.value, done: cwDone.value, won: cwWon.value })
  if (isAuthenticated.value) submitNativeScore('kryssburet', cwScore.value)
}

function cwUseHint() {
  if (!cwPrimeActiveCell()) return
  const cell = cwGrid.value[cwActiveCell.value.row]?.[cwActiveCell.value.col]
  if (!cell || cell.black || !cell.answer) return
  cwStopPreview()
  cwInput.value = {
    ...cwInput.value,
    [`${cell.row},${cell.col}`]: cell.answer,
  }
  saveNativeProgress('kryssburet', { input: cwInput.value, selected: cwSelected.value, activeCell: cwActiveCell.value, done: cwDone.value, won: cwWon.value })
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

const timePuzzle = ref({ prompt: '', imageUrl: '', attribution: '' })
const timeRounds = ref([])
const timeRoundIndex = ref(0)
const timeRoundInputs = ref([])
const timeRoundResults = ref([])
const timeYear = ref(2000)
const timeGuess = ref(null)
const timeDone = ref(false)
const timeResult = ref({ year: '', locationName: '', distanceKm: 0, yearScore: 0, locationScore: 0 })
const timeMapEl = ref(null)
let _timeMap = null
let _timeGuessMarker = null
let _timeAnswerMarker = null
const timeRoundTotal = computed(() => Math.max(1, timeRounds.value.length))
const timeScore = computed(() => timeRoundResults.value.reduce((sum, result) => sum + (Number(result?.score) || 0), 0))

function applyTimePuzzle(payload) {
  timeRounds.value = Array.isArray(payload.rounds) && payload.rounds.length ? payload.rounds : [payload]
  timeRoundIndex.value = 0
  timeRoundInputs.value = []
  timeRoundResults.value = []
  const saved = loadProgress('tidsburet', selectedNativeIds.value.tidsburet)
  if (saved) {
    timeRoundIndex.value = Math.min(Number(saved.roundIndex) || 0, timeRounds.value.length - 1)
    timeRoundInputs.value = Array.isArray(saved.inputs) ? saved.inputs : [{ year: saved.year, guess: saved.guess }]
    timeRoundResults.value = Array.isArray(saved.results) ? saved.results : (saved.result ? [saved.result] : [])
  }
  applyTimeRound()
  nextTick(initTimeMap)
}

async function timeSubmit() {
  if (timeDone.value || !timeGuess.value) return
  const result = await puzzleApi.validateNative('TIMEGUESSR', selectedNativeIds.value.tidsburet, {
    roundIndex: timeRoundIndex.value,
    year: timeYear.value,
    lat: timeGuess.value.lat,
    lng: timeGuess.value.lng,
  })
  timeResult.value = result
  timeDone.value = true
  timeRoundResults.value = setAt(timeRoundResults.value, timeRoundIndex.value, result)
  timeRoundInputs.value = setAt(timeRoundInputs.value, timeRoundIndex.value, { year: timeYear.value, guess: timeGuess.value })
  showTimeAnswerMarker()
  saveTimeProgress()
}

function saveTimeProgress() {
  timeRoundInputs.value = setAt(timeRoundInputs.value, timeRoundIndex.value, { year: timeYear.value, guess: timeGuess.value })
  saveNativeProgress('tidsburet', {
    roundIndex: timeRoundIndex.value,
    inputs: timeRoundInputs.value,
    results: timeRoundResults.value,
  })
}

function finishTimeResult() {
  if (timeRoundIndex.value < timeRoundTotal.value - 1) {
    timeRoundIndex.value += 1
    applyTimeRound()
    saveTimeProgress()
    return
  }
  if (isAuthenticated.value && !nativeSubmitted.value.tidsburet) {
    submitNativeScore('tidsburet', timeScore.value)
  }
}

function applyTimeRound() {
  const round = timeRounds.value[timeRoundIndex.value] || {}
  const input = timeRoundInputs.value[timeRoundIndex.value] || {}
  const result = timeRoundResults.value[timeRoundIndex.value] || null
  timePuzzle.value = {
    prompt: round.prompt ?? '',
    imageUrl: round.imageUrl ?? '',
    attribution: round.attribution ?? '',
  }
  timeYear.value = Number(input.year) || 2000
  timeGuess.value = input.guess || null
  timeDone.value = Boolean(result)
  timeResult.value = result || { year: '', locationName: '', distanceKm: 0, yearScore: 0, locationScore: 0, score: 0 }
  if (_timeGuessMarker) { _timeGuessMarker.remove(); _timeGuessMarker = null }
  if (_timeAnswerMarker) { _timeAnswerMarker.remove(); _timeAnswerMarker = null }
  nextTick(initTimeMap)
}

function setAt(items, index, value) {
  const next = [...items]
  next[index] = value
  return next
}

function initTimeMap() {
  if (activeGame.value !== 'tidsburet' || !timeMapEl.value) return
  if (!_timeMap) {
    _timeMap = L.map(timeMapEl.value, { worldCopyJump: true }).setView([20, 0], 2)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(_timeMap)
    _timeMap.on('click', event => {
      if (timeDone.value) return
      timeGuess.value = { lat: event.latlng.lat, lng: event.latlng.lng }
      setTimeGuessMarker()
      saveTimeProgress()
    })
  }
  setTimeout(() => _timeMap?.invalidateSize(), 50)
  setTimeGuessMarker()
  if (timeDone.value) showTimeAnswerMarker()
}

function setTimeGuessMarker() {
  if (!_timeMap) return
  if (_timeGuessMarker) _timeGuessMarker.remove()
  if (timeGuess.value) {
    _timeGuessMarker = L.circleMarker([timeGuess.value.lat, timeGuess.value.lng], {
      radius: 7,
      color: '#c64a3f',
      fillColor: '#c64a3f',
      fillOpacity: 0.85,
    }).addTo(_timeMap)
  }
}

function showTimeAnswerMarker() {
  if (!_timeMap || !Number.isFinite(Number(timeResult.value.lat)) || !Number.isFinite(Number(timeResult.value.lng))) return
  if (_timeAnswerMarker) _timeAnswerMarker.remove()
  _timeAnswerMarker = L.circleMarker([timeResult.value.lat, timeResult.value.lng], {
    radius: 8,
    color: '#0d3b2e',
    fillColor: '#0d3b2e',
    fillOpacity: 0.85,
  }).addTo(_timeMap)
}

// ——— ARTISTBURET ——————————————————————————————————————

const artistPool = ref([])
const artistAnswer = ref({})
const artistGuesses = ref([])
const artistDone = ref(false)
const artistWon = ref(false)
const artistInput = ref('')
const artistSuggestions = ref([])
const artistAlreadyGuessed = ref(false)
const artistNotFound = ref(false)
const MAX_ARTIST_GUESSES = 10
let _artistSuggestTimer = null

const artistScore = computed(() => {
  if (!artistWon.value) return 0
  return Math.max(0, 1000 - ((artistGuesses.value.length - 1) * 100))
})

function artistInitials(name) {
  if (!name) return '?'
  return name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()
}

function flagEmoji(countryCode) {
  if (!countryCode || countryCode.length !== 2) return ''
  return String.fromCodePoint(...countryCode.toUpperCase().split('').map(c => c.charCodeAt(0) + 127397))
}

function applyArtistPuzzle(payload) {
  artistPool.value = Array.isArray(payload.artists) ? payload.artists : []
  artistAnswer.value = {
    name: payload.answer,
    debut: payload.debut,
    members: payload.members,
    gender: payload.gender,
    genre: payload.genre,
    country: payload.country,
    popularity: payload.popularity,
  }
  artistGuesses.value = []
  artistDone.value = false
  artistWon.value = false
  artistInput.value = ''
  artistSuggestions.value = []
  artistAlreadyGuessed.value = false
  artistNotFound.value = false
  const saved = loadProgress('artistburet', selectedNativeIds.value.artistburet)
  if (saved) {
    artistGuesses.value = Array.isArray(saved.guesses) ? saved.guesses : []
    artistDone.value = Boolean(saved.done)
    artistWon.value = Boolean(saved.won)
  }
}

function artistSearchSuggestions() {
  if (_artistSuggestTimer) clearTimeout(_artistSuggestTimer)
  const query = artistInput.value.trim().toLowerCase()
  if (query.length < 1) { artistSuggestions.value = []; return }
  _artistSuggestTimer = setTimeout(() => {
    const used = new Set(artistGuesses.value.map(g => g.name?.toLowerCase()))
    artistSuggestions.value = artistPool.value
      .filter(a => a.name.toLowerCase().includes(query) && !used.has(a.name.toLowerCase()))
      .slice(0, 6)
  }, 80)
}

function artistSelectSuggestion(artist) {
  artistInput.value = artist.name
  artistSuggestions.value = []
}

function artistDeferHideSuggestions() {
  setTimeout(() => { artistSuggestions.value = [] }, 180)
}

async function artistSubmit() {
  if (artistDone.value) return
  const name = artistInput.value.trim()
  if (!name) return
  artistAlreadyGuessed.value = false
  artistNotFound.value = false
  if (artistGuesses.value.some(g => g.name?.toLowerCase() === name.toLowerCase())) {
    artistAlreadyGuessed.value = true
    return
  }
  artistInput.value = ''
  artistSuggestions.value = []
  const result = await puzzleApi.validateNative('ARTISTBURET', selectedNativeIds.value.artistburet, { guess: name })
  if (result.notFound) {
    artistNotFound.value = true
    return
  }
  artistGuesses.value = [...artistGuesses.value, result]
  saveNativeProgress('artistburet', { guesses: artistGuesses.value, done: artistDone.value, won: artistWon.value })
  if (result.correct) {
    artistWon.value = true
    artistDone.value = true
    saveNativeProgress('artistburet', { guesses: artistGuesses.value, done: artistDone.value, won: artistWon.value })
    if (isAuthenticated.value) submitNativeScore('artistburet', artistScore.value)
    return
  }
  if (artistGuesses.value.length >= MAX_ARTIST_GUESSES) {
    artistDone.value = true
    saveNativeProgress('artistburet', { guesses: artistGuesses.value, done: artistDone.value, won: artistWon.value })
    if (isAuthenticated.value) submitNativeScore('artistburet', 0)
  }
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
  const all = connPuzzle.value.words?.length ? connPuzzle.value.words : connPuzzle.value.groups.flatMap(g => g.words)
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
  saveConnProgress()
}

function connToggle(word) {
  if (connSelected.value.includes(word)) {
    connSelected.value = connSelected.value.filter(w => w !== word)
  } else if (connSelected.value.length < 4) {
    connSelected.value = [...connSelected.value, word]
  }
  saveConnProgress()
}

async function connSubmit() {
  if (connSelected.value.length !== 4) return
  const sel = connSelected.value
  const validation = await puzzleApi.validateConnections(selectedConnId.value, sel)
  const match = validation.groups?.[0]
  if (match) {
    connSolved.value    = [...connSolved.value, match]
    connShuffled.value  = connShuffled.value.filter(w => !match.words.includes(w))
    connSelected.value  = []
    if (connSolved.value.length === 4) {
      connGameState.value = 'won'
      saveConnProgress()
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
        saveConnProgress()
        if (isAuthenticated.value && !connScoreSubmitted.value) submitConnScore()
      }
    }, 600)
  }
  saveConnProgress()
}

function applyConnectionsPuzzle(data) {
  connPuzzle.value = {
    createdBy: data.createdBy,
    title: data.title,
    groups: (data.groups || []).map(g => ({
      category:   g.category,
      words:      g.words,
      difficulty: g.difficulty,
    })),
    words: data.words || [],
  }
  connInit()
  restoreConnProgress()
}

function loadConnPuzzle(p) {
  selectedConnId.value = p.id
  if (completedConnIds.value.includes(p.id)) {
    connGameState.value = 'completed'
    return
  }
  applyConnectionsPuzzle(p)
}

function saveConnProgress() {
  saveProgress('connections', selectedConnId.value, {
    shuffled: connShuffled.value,
    selected: connSelected.value,
    solvedCategories: connSolved.value.map(group => group.category),
    mistakes: connMistakes.value,
    gameState: connGameState.value,
  })
}

function restoreConnProgress() {
  const saved = loadProgress('connections', selectedConnId.value)
  if (!saved) return
  const groupsByCategory = new Map(connPuzzle.value.groups.map(group => [group.category, group]))
  connShuffled.value = Array.isArray(saved.shuffled) ? saved.shuffled : connShuffled.value
  connSelected.value = Array.isArray(saved.selected) ? saved.selected : []
  connSolved.value = Array.isArray(saved.solvedCategories)
    ? saved.solvedCategories.map(category => groupsByCategory.get(category)).filter(Boolean)
    : []
  connMistakes.value = Number(saved.mistakes) || 0
  connGameState.value = saved.gameState || 'playing'
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
    clearProgress('connections', selectedConnId.value)
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
const wrdGuessStates = ref({})
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
  return wrdGuessStates.value[guess] || Array(guess.length).fill('absent')
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

async function wrdKeyPress(key) {
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
    let validation
    try {
      validation = await puzzleApi.validateWordle(selectedWrdId.value, guess)
    } catch {
      wrdMessage.value = 'Ikke i ordlisten'
      setTimeout(() => { wrdMessage.value = '' }, 1500)
      return
    }
    if (validation.validWord === false) {
      wrdMessage.value = validation.message || 'Ikke i ordlisten'
      setTimeout(() => { wrdMessage.value = '' }, 1500)
      return
    }
    wrdGuessStates.value = { ...wrdGuessStates.value, [guess]: validation.states || [] }
    wrdGuesses.value = [...wrdGuesses.value, guess]
    wrdCurrent.value = ''
    if (validation.correct) {
      wrdGameState.value = 'won'
      saveWrdProgress()
      if (isAuthenticated.value && !wrdScoreSubmitted.value) submitWrdScore()
    } else if (wrdGuesses.value.length >= 6) {
      wrdGameState.value = 'lost'
      saveWrdProgress()
      if (isAuthenticated.value && !wrdScoreSubmitted.value) submitWrdScore()
    }
  } else if (/^[A-ZÆØÅ]$/.test(key) && wrdCurrent.value.length < 5) {
    wrdCurrent.value += key
  }
  saveWrdProgress()
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
  wrdGuessStates.value = {}
  wrdCurrent.value   = ''
  wrdGameState.value = 'playing'
  wrdMessage.value   = ''
  wrdScoreSubmitted.value = false
  restoreWrdProgress()
}

function saveWrdProgress() {
  saveProgress('wordle', selectedWrdId.value, {
    guesses: wrdGuesses.value,
    states: wrdGuessStates.value,
    current: wrdCurrent.value,
    gameState: wrdGameState.value,
  })
}

function restoreWrdProgress() {
  const saved = loadProgress('wordle', selectedWrdId.value)
  if (!saved) return
  wrdGuesses.value = Array.isArray(saved.guesses) ? saved.guesses : []
  wrdGuessStates.value = saved.states || {}
  wrdCurrent.value = saved.current || ''
  wrdGameState.value = saved.gameState || 'playing'
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
    clearProgress('wordle', selectedWrdId.value)
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
    wrdCreatorError.value = 'Ordet må finnes i ordlisten.'
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
const timeCreator = ref({ prompt: '', imageUrl: '', year: new Date().getFullYear(), locationName: '', lat: 0, lng: 0, attribution: '' })

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
  if (gameId === 'tidsburet') timeCreator.value = { prompt: '', imageUrl: '', year: new Date().getFullYear(), locationName: '', lat: 0, lng: 0, attribution: '' }
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
    if (
      !timeCreator.value.prompt.trim() ||
      !timeCreator.value.imageUrl.trim() ||
      !timeCreator.value.locationName.trim() ||
      !timeCreator.value.year ||
      !Number.isFinite(Number(timeCreator.value.lat)) ||
      !Number.isFinite(Number(timeCreator.value.lng))
    ) return null
    return {
      prompt: timeCreator.value.prompt.trim(),
      imageUrl: timeCreator.value.imageUrl.trim(),
      year: Number(timeCreator.value.year),
      lat: Number(timeCreator.value.lat),
      lng: Number(timeCreator.value.lng),
      locationName: timeCreator.value.locationName.trim(),
      attribution: timeCreator.value.attribution.trim(),
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
      imageUrl: payload.imageUrl || '',
      year: payload.year || new Date().getFullYear(),
      locationName: payload.locationName || payload.place || '',
      lat: payload.lat || 0,
      lng: payload.lng || 0,
      attribution: payload.attribution || '',
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
  } else if (activeGame.value === 'kryssburet' && !cwDone.value) {
    if (k === 'ArrowLeft' || k === 'ArrowRight' || k === 'ArrowUp' || k === 'ArrowDown') {
      e.preventDefault()
      cwMoveByArrow(k)
    }
    if (k === 'Backspace') {
      e.preventDefault()
      if (!cwPrimeActiveCell()) return
      const key = `${cwActiveCell.value.row},${cwActiveCell.value.col}`
      if (cwInput.value[key]) {
        const next = { ...cwInput.value }
        delete next[key]
        cwInput.value = next
        saveNativeProgress('kryssburet', { input: cwInput.value, selected: cwSelected.value, activeCell: cwActiveCell.value, done: cwDone.value, won: cwWon.value })
      } else {
        cwMoveCursor(-1)
      }
    } else if (/^[a-zA-ZæøåÆØÅ0-9]$/.test(k)) {
      e.preventDefault()
      if (!cwPrimeActiveCell()) return
      cwInput.value = { ...cwInput.value, [`${cwActiveCell.value.row},${cwActiveCell.value.col}`]: k.toUpperCase() }
      saveNativeProgress('kryssburet', { input: cwInput.value, selected: cwSelected.value, activeCell: cwActiveCell.value, done: cwDone.value, won: cwWon.value })
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

  if (applyDaily && dailyConnResult.status === 'fulfilled' && dailyConnResult.value?.id) {
    loadConnPuzzle(dailyConnResult.value)
  }
  if (applyDaily && dailyWrdResult.status === 'fulfilled' && dailyWrdResult.value?.id) {
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
  if (!isAuthenticated.value) return
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
  if (_timeMap) { _timeMap.remove(); _timeMap = null }
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
.sp-login-required {
  display: grid;
  justify-items: start;
  gap: 16px;
  padding: 72px 0 160px;
}
.sp-login-required .sp-sub {
  margin: 0;
}
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
.game-side-tools {
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  gap: 14px;
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
.native-title-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
.native-title-row .section-title {
  margin: 0;
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
  grid-template-columns: minmax(0, 1fr) minmax(150px, 176px);
  gap: 22px;
  align-items: start;
  width: 100%;
  text-align: left;
}
.cw-grid-wrap {
  display: flex;
  justify-content: center;
  width: 100%;
  min-width: 0;
  overflow: hidden;
}
.cw-grid {
  display: grid;
  grid-template-columns: repeat(var(--cw-cols, 9), minmax(0, 1fr));
  gap: 2px;
  border: 2px solid var(--ink);
  background: var(--ink);
  width: min(100%, calc(var(--cw-cols, 9) * clamp(60px, 4.8vw, 76px)));
}
.cw-cell {
  aspect-ratio: 1;
  min-width: 0;
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
  display: flex; flex-direction: column; gap: 16px;
  max-height: 520px; overflow-y: auto;
}
.cw-clues-section { display: flex; flex-direction: column; gap: 3px; }
.cw-clues-heading {
  font-family: var(--mono); font-size: 10px; text-transform: uppercase;
  letter-spacing: 0.12em; color: var(--ink-mute); margin-bottom: 4px; font-weight: 600;
}
.cw-clue-row {
  display: flex; align-items: center; gap: 8px;
  padding: 4px 6px; border-radius: var(--radius);
  cursor: pointer; transition: background 0.15s;
}
.cw-clue-row:hover { background: var(--paper); }
.cw-clue-row.cw-clue-active { background: var(--paper); }
.cw-badge {
  flex-shrink: 0; min-width: 30px; height: 20px; border-radius: 4px;
  font-family: var(--mono); font-size: 10px; font-weight: 700;
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
.cw-clue-text { font-size: 12px; color: var(--ink-soft); line-height: 1.2; }

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
  grid-template-columns: minmax(0, 1fr);
  gap: 12px;
}
.time-play-layout {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(260px, 0.85fr);
  align-items: end;
  gap: 22px;
}
.time-photo-col,
.time-control-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.time-control-col {
  align-items: stretch;
}
.time-photo {
  width: 100%;
  max-height: min(58vh, 520px);
  object-fit: contain;
  border-radius: var(--radius);
  border: 1px solid var(--line-soft);
  background: var(--paper);
}
.time-attribution {
  align-self: flex-start;
  margin: -8px 0 0;
  font-family: var(--mono);
  font-size: 10px;
  color: var(--ink-mute);
}
.time-map {
  width: 100%;
  height: 260px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
  overflow: hidden;
}
.time-result-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
  background: var(--paper);
  text-align: center;
}
.time-result-card h3,
.time-result-card p {
  margin: 0;
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
  .time-play-layout { grid-template-columns: 1fr; }
  .time-map { height: 320px; }
  .cw-layout { grid-template-columns: 1fr; }
  .cw-letter { font-size: 14px; }
  .cw-clues { max-height: none; }
  .cw-creator-layout { grid-template-columns: 1fr; }
  .cw-creator-preview { position: static; }
  .cw-creator-position-row { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

/* ── Artistburet ─────────────────────────────── */
.ab-body { padding-top: 4px; }

.ab-top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}
.ab-counter {
  flex: 1;
  font-family: var(--mono);
  font-size: 12px;
  color: var(--ink-mute);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}
.ab-help-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1.5px solid var(--line-soft);
  background: var(--bg);
  font-family: var(--mono);
  font-size: 13px;
  font-weight: 700;
  color: var(--ink-mute);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
  flex-shrink: 0;
}
.ab-help-btn:hover, .ab-help-btn.active { border-color: var(--accent); color: var(--accent); }

.ab-help-panel {
  background: var(--paper);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
  padding: 16px 20px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.game-help-panel {
  width: 100%;
  max-width: 720px;
  text-align: left;
}
.native-help-panel {
  margin-top: 4px;
  margin-bottom: 4px;
}
.ab-help-row { display: flex; align-items: center; gap: 12px; }
.ab-help-tile {
  min-width: 68px;
  padding: 6px 10px;
  font-size: 11px;
  font-weight: 700;
  text-align: center;
  flex-shrink: 0;
}
.ab-help-text { font-size: 13px; color: var(--ink-mute); }
.ab-help-note {
  font-family: var(--mono);
  font-size: 11px;
  color: var(--ink-mute);
  margin-top: 4px;
  letter-spacing: 0.06em;
}

/* Guess cards */
.ab-guesses { display: flex; flex-direction: column; gap: 12px; margin-bottom: 24px; }
.ab-guess-card {
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
  overflow: hidden;
}
.ab-guess-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--line-soft);
  background: var(--paper);
}
.ab-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--bg-soft);
  border: 2px solid var(--line-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--mono);
  font-size: 12px;
  font-weight: 700;
  color: var(--ink-mute);
  flex-shrink: 0;
}
.ab-avatar.ab-avatar-won { border-color: #1e7a4a; color: #1e7a4a; }
.ab-artist-name {
  font-size: 15px;
  font-weight: 700;
  flex: 1;
}
.ab-correct-badge {
  font-family: var(--mono);
  font-size: 12px;
  color: #1e7a4a;
  font-weight: 700;
}

/* Attribute grid */
.ab-attrs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0;
}
.ab-attr {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 14px 8px;
  border-right: 1px solid rgba(0,0,0,0.06);
  border-bottom: 1px solid rgba(0,0,0,0.06);
  background: var(--bg-soft);
  text-align: center;
}
.ab-attr:nth-child(3n) { border-right: 0; }
.ab-attr:nth-last-child(-n+3) { border-bottom: 0; }
.ab-attr-label {
  font-family: var(--mono);
  font-size: 9px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--ink-mute);
  opacity: 0.75;
}
.ab-attr-val {
  font-family: var(--mono);
  font-size: 14px;
  font-weight: 700;
  color: var(--ink);
  line-height: 1.2;
}
.ab-flag { font-size: 22px; line-height: 1; }
.ab-dir { font-size: 12px; }

/* Color states */
.ab-match { background: #1e7a4a !important; }
.ab-match .ab-attr-label { color: rgba(255,255,255,0.7); opacity: 1; }
.ab-match .ab-attr-val { color: #fff; }
.ab-close { background: #b87d1c !important; }
.ab-close .ab-attr-label { color: rgba(255,255,255,0.7); opacity: 1; }
.ab-close .ab-attr-val { color: #fff; }
.ab-no-match { background: var(--bg-soft); }

/* Input */
.ab-input-wrap { margin-bottom: 8px; }
.ab-input-row { display: flex; gap: 10px; align-items: flex-start; }
.ab-input-container { position: relative; flex: 1; }
.ab-search-input { font-size: 15px; padding: 12px 16px; }
.ab-suggestions {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: var(--bg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
  z-index: 20;
  overflow: hidden;
  box-shadow: 0 6px 20px rgba(0,0,0,0.1);
}
.ab-suggestion {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 11px 14px;
  border: 0;
  background: transparent;
  font-family: var(--sans);
  font-size: 14px;
  color: var(--ink);
  text-align: left;
  cursor: pointer;
  transition: background 0.1s;
}
.ab-suggestion:hover { background: var(--paper); }
.ab-sug-initials {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--bg-soft);
  border: 1px solid var(--line-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--mono);
  font-size: 10px;
  font-weight: 700;
  flex-shrink: 0;
}

@media (max-width: 600px) {
  .ab-attrs { grid-template-columns: repeat(3, 1fr); }
  .ab-attr { padding: 10px 4px; }
  .ab-attr-val { font-size: 12px; }
  .ab-flag { font-size: 18px; }
  .ab-artist-name { font-size: 13px; }
}
</style>
