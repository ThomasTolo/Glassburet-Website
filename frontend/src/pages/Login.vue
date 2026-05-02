<template>
  <section class="login-page">
    <div class="login-shell">
      <div class="login-copy">
        <span class="eyebrow">{{ mode === 'login' ? 'Medlemsinnlogging' : 'Opprett konto' }}</span>
        <h1 class="display" style="font-size: clamp(44px, 7vw, 92px); margin: 12px 0 18px;">
          {{ mode === 'login' ? 'Logg' : 'Registrer' }} <em>{{ mode === 'login' ? 'inn' : 'deg' }}</em>.
        </h1>
        <p class="lede">
          Alle kan lese, spille og lage spill. Bare admins kan publisere bilder, quotes, aktiviteter og one-liners.
        </p>
      </div>

      <div class="login-card">
        <p v-if="isAuthenticated" class="login-status">
          Logget inn som <strong>{{ displayName || 'ukjent' }}</strong>.
        </p>
        <form v-if="isAuthenticated" class="login-form password-reset-form" @submit.prevent="submitPasswordReset">
          <label class="login-field">
            <span>Nåværende passord</span>
            <div class="password-input-wrap">
              <input v-model="resetCurrentPassword" :type="showResetCurrentPassword ? 'text' : 'password'" autocomplete="current-password" placeholder="••••••••" required />
              <button class="password-toggle" type="button" @click="showResetCurrentPassword = !showResetCurrentPassword">
                {{ showResetCurrentPassword ? 'Skjul' : 'Vis' }}
              </button>
            </div>
          </label>
          <label class="login-field">
            <span>Nytt passord</span>
            <div class="password-input-wrap">
              <input v-model="resetNewPassword" :type="showResetNewPassword ? 'text' : 'password'" autocomplete="new-password" placeholder="••••••••" required />
              <button class="password-toggle" type="button" @click="showResetNewPassword = !showResetNewPassword">
                {{ showResetNewPassword ? 'Skjul' : 'Vis' }}
              </button>
            </div>
          </label>
          <label class="login-field">
            <span>Gjenta nytt passord</span>
            <div class="password-input-wrap">
              <input v-model="resetConfirmPassword" :type="showResetConfirmPassword ? 'text' : 'password'" autocomplete="new-password" placeholder="••••••••" required />
              <button class="password-toggle" type="button" @click="showResetConfirmPassword = !showResetConfirmPassword">
                {{ showResetConfirmPassword ? 'Skjul' : 'Vis' }}
              </button>
            </div>
          </label>
          <div class="login-actions">
            <button class="login-button" type="submit" :disabled="loading">
              {{ loading ? 'Lagrer…' : 'Bytt passord' }}
            </button>
          </div>
          <p v-if="resetMessage" class="login-success">{{ resetMessage }}</p>
        </form>

        <div class="login-tabs">
          <button :class="['login-tab', mode === 'login' && 'active']" type="button" @click="switchMode('login')">Logg inn</button>
          <button :class="['login-tab', mode === 'register' && 'active']" type="button" @click="switchMode('register')">Registrer deg</button>
        </div>

        <form v-if="mode === 'login'" class="login-form" @submit.prevent="submitLogin">
          <label class="login-field">
            <span>Navn</span>
            <input v-model="name" type="text" autocomplete="username" placeholder="Username" required />
          </label>

          <label class="login-field">
            <span>Passord</span>
            <div class="password-input-wrap">
              <input v-model="password" :type="showLoginPassword ? 'text' : 'password'" autocomplete="current-password" placeholder="••••••••" required />
              <button class="password-toggle" type="button" @click="showLoginPassword = !showLoginPassword">
                {{ showLoginPassword ? 'Skjul' : 'Vis' }}
              </button>
            </div>
          </label>

          <div class="login-actions">
            <button class="login-button" type="submit" :disabled="loading">
              {{ loading ? 'Logger inn…' : 'Logg inn' }}
            </button>
            <RouterLink class="login-back" to="/">Tilbake</RouterLink>
            <button v-if="isAuthenticated" class="login-back" type="button" @click="logout">Logg ut</button>
          </div>

          <p v-if="error" class="login-error">{{ error }}</p>
        </form>

        <form v-else class="login-form" @submit.prevent="submitRegister">
          <label class="login-field">
            <span>Navn</span>
            <input v-model="regName" type="text" autocomplete="username" placeholder="Username" required />
          </label>

          <label class="login-field">
            <span>Passord</span>
            <div class="password-input-wrap">
              <input v-model="regPassword" :type="showRegPassword ? 'text' : 'password'" autocomplete="new-password" placeholder="••••••••" required />
              <button class="password-toggle" type="button" @click="showRegPassword = !showRegPassword">
                {{ showRegPassword ? 'Skjul' : 'Vis' }}
              </button>
            </div>
          </label>

          <label class="login-field">
            <span>Gjenta passord</span>
            <div class="password-input-wrap">
              <input v-model="regConfirmPassword" :type="showRegConfirmPassword ? 'text' : 'password'" autocomplete="new-password" placeholder="••••••••" required />
              <button class="password-toggle" type="button" @click="showRegConfirmPassword = !showRegConfirmPassword">
                {{ showRegConfirmPassword ? 'Skjul' : 'Vis' }}
              </button>
            </div>
          </label>

          <div class="login-actions">
            <button class="login-button" type="submit" :disabled="loading">
              {{ loading ? 'Registrerer…' : 'Opprett konto' }}
            </button>
            <RouterLink class="login-back" to="/">Tilbake</RouterLink>
          </div>

          <p v-if="error" class="login-error">{{ error }}</p>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { auth } from '../services/api'
import { clearAuthToken, setAuthToken, displayName, isAuthenticated } from '../services/authState'

const API_URL = import.meta.env.VITE_API_URL || '/api'

const route = useRoute()
const router = useRouter()

const mode = ref('login')
const name = ref('')
const password = ref('')
const regName = ref('')
const regPassword = ref('')
const regConfirmPassword = ref('')
const resetCurrentPassword = ref('')
const resetNewPassword = ref('')
const resetConfirmPassword = ref('')
const showLoginPassword = ref(false)
const showRegPassword = ref(false)
const showRegConfirmPassword = ref(false)
const showResetCurrentPassword = ref(false)
const showResetNewPassword = ref(false)
const showResetConfirmPassword = ref(false)
const loading = ref(false)
const error = ref('')
const resetMessage = ref('')

function switchMode(next) {
  mode.value = next
  error.value = ''
  resetMessage.value = ''
}

const redirectTo = computed(() => {
  const target = route.query.redirect
  return typeof target === 'string' && target.startsWith('/') ? target : '/'
})

async function submitLogin() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(name.value.trim(), password.value)
    await router.push(redirectTo.value)
  } catch {
    error.value = 'Feil navn eller passord.'
  } finally {
    loading.value = false
  }
}

async function submitRegister() {
  error.value = ''
  resetMessage.value = ''

  if (regPassword.value.length < 8) {
    error.value = 'Passordet må være minst 8 tegn.'
    return
  }
  if (!/\d/.test(regPassword.value)) {
    error.value = 'Passordet må inneholde minst ett tall.'
    return
  }
  if (regPassword.value !== regConfirmPassword.value) {
    error.value = 'Passordene må være like.'
    return
  }

  loading.value = true
  try {
    const res = await fetch(`${API_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: regName.value.trim(), password: regPassword.value }),
    })
    const data = await res.json().catch(() => ({}))
    if (!res.ok) {
      if (data.error === 'Member exists') {
        error.value = 'Navnet er allerede i bruk.'
      } else if (data.error === 'Password must be at least 8 characters') {
        error.value = 'Passordet må være minst 8 tegn.'
      } else if (data.error === 'Password must contain at least one number') {
        error.value = 'Passordet må inneholde minst ett tall.'
      } else {
        error.value = 'Registrering mislyktes.'
      }
    } else {
      setAuthToken(data.token)
      await router.push(redirectTo.value)
    }
  } catch {
    error.value = 'Noe gikk galt. Prøv igjen.'
  } finally {
    loading.value = false
  }
}

async function submitPasswordReset() {
  error.value = ''
  resetMessage.value = ''

  if (resetNewPassword.value.length < 8) {
    error.value = 'Passordet må være minst 8 tegn.'
    return
  }
  if (!/\d/.test(resetNewPassword.value)) {
    error.value = 'Passordet må inneholde minst ett tall.'
    return
  }
  if (resetNewPassword.value !== resetConfirmPassword.value) {
    error.value = 'Passordene må være like.'
    return
  }

  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const res = await fetch(`${API_URL}/auth/password`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify({
        currentPassword: resetCurrentPassword.value,
        newPassword: resetNewPassword.value,
      }),
    })
    const data = await res.json().catch(() => ({}))
    if (!res.ok) {
      if (data.error === 'Current password is incorrect') {
        error.value = 'Nåværende passord er feil.'
      } else if (data.error === 'Password must be at least 8 characters') {
        error.value = 'Passordet må være minst 8 tegn.'
      } else if (data.error === 'Password must contain at least one number') {
        error.value = 'Passordet må inneholde minst ett tall.'
      } else {
        error.value = 'Kunne ikke bytte passord.'
      }
      return
    }
    setAuthToken(data.token)
    resetCurrentPassword.value = ''
    resetNewPassword.value = ''
    resetConfirmPassword.value = ''
    resetMessage.value = 'Passordet er oppdatert.'
  } catch {
    error.value = 'Noe gikk galt. Prøv igjen.'
  } finally {
    loading.value = false
  }
}

function logout() {
  clearAuthToken()
}
</script>

<style scoped>
.login-page {
  min-height: calc(100vh - 220px);
  display: grid;
  align-items: center;
  padding: 28px 0 64px;
}

.login-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(340px, 460px);
  gap: 32px;
  align-items: center;
}

.login-card {
  background: var(--paper);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
  box-shadow: var(--shadow-md);
  padding: 28px;
}

.login-status {
  margin-bottom: 18px;
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(13, 59, 46, 0.06);
  color: var(--ink-soft);
  font-size: 14px;
}

.login-form {
  display: grid;
  gap: 16px;
}

.login-field {
  display: grid;
  gap: 8px;
  font-size: 12px;
  color: var(--ink-mute);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.login-field input {
  width: 100%;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid var(--line-soft);
  background: var(--bg);
  color: var(--ink);
  font-family: var(--sans);
  font-size: 16px;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.password-input-wrap {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: stretch;
  gap: 8px;
}

.password-toggle {
  min-width: 64px;
  border: 1px solid var(--line-soft);
  border-radius: 12px;
  background: var(--bg);
  color: var(--ink-soft);
  font-family: var(--mono);
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  cursor: pointer;
}

.password-toggle:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.login-field input:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 4px rgba(13, 59, 46, 0.1);
}

.password-reset-form {
  margin-bottom: 22px;
  padding-bottom: 22px;
  border-bottom: 1px solid var(--line-soft);
}

.login-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 4px;
}

.login-button,
.login-back {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: 0 18px;
  border-radius: 999px;
  font-family: var(--mono);
  font-size: 11px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.login-button {
  background: var(--ink);
  color: var(--bg);
}

.login-button:disabled {
  opacity: 0.6;
  cursor: wait;
}

.login-back {
  border: 1px solid var(--line-soft);
  color: var(--ink-soft);
  background: transparent;
}

.login-tabs {
  display: flex;
  border-bottom: 1px solid var(--line-soft);
  margin-bottom: 20px;
}

.login-tab {
  padding: 10px 16px;
  font-family: var(--mono);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--ink-mute);
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: all 0.2s ease;
}
.login-tab:hover { color: var(--ink); }
.login-tab.active { color: var(--ink); border-bottom-color: var(--accent); }

.login-error {
  color: var(--accent-2);
  font-size: 14px;
}

.login-success {
  color: var(--accent);
  font-size: 14px;
}

@media (max-width: 980px) {
  .login-shell {
    grid-template-columns: 1fr;
  }
}
</style>
