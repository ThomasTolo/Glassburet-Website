import { computed, ref } from 'vue'

const STORAGE_KEY = 'token'

function readStoredToken() {
  if (typeof window === 'undefined') {
    return null
  }
  return localStorage.getItem(STORAGE_KEY)
}

function decodeTokenPayload(token) {
  if (!token) {
    return null
  }

  try {
    const [, payload] = token.split('.')
    if (!payload) {
      return null
    }

    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')
    const json = atob(padded)
    return JSON.parse(json)
  } catch {
    return null
  }
}

const token = ref(readStoredToken())

const currentUser = computed(() => decodeTokenPayload(token.value))
const isAuthenticated = computed(() => Boolean(token.value))
const isAdmin = computed(() => currentUser.value?.role === 'ROLE_ADMIN')
const isOwner = computed(() => currentUser.value?.role === 'ROLE_OWNER')
const displayName = computed(() => currentUser.value?.sub || '')

function setAuthToken(nextToken) {
  token.value = nextToken
  localStorage.setItem(STORAGE_KEY, nextToken)
}

function clearAuthToken() {
  token.value = null
  localStorage.removeItem(STORAGE_KEY)
}

export {
  token,
  currentUser,
  isAuthenticated,
  isAdmin,
  isOwner,
  displayName,
  setAuthToken,
  clearAuthToken,
}