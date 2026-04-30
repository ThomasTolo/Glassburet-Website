import { computed, onMounted, onUnmounted, ref } from 'vue'

const dayNames = ['SØNDAG', 'MANDAG', 'TIRSDAG', 'ONSDAG', 'TORSDAG', 'FREDAG', 'LØRDAG']
const monthNames = ['JAN', 'FEB', 'MAR', 'APR', 'MAI', 'JUN', 'JUL', 'AUG', 'SEP', 'OKT', 'NOV', 'DES']
const semesterStart = new Date(2025, 11, 9)

function stripTime(date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate())
}

function getSemesterDayNumber(now) {
  return Math.max(1, Math.floor((stripTime(now) - semesterStart) / 86400000) + 1)
}

function formatDateLabel(now) {
  const dayName = dayNames[now.getDay()]
  const day = String(now.getDate()).padStart(2, '0')
  const month = monthNames[now.getMonth()]
  return `${dayName} ${day} ${month} ${now.getFullYear()}`
}

function formatClockLabel(now) {
  const dayName = dayNames[now.getDay()]
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  return `${dayName} · ${hours}:${minutes}`
}

export function useLiveDateInfo(options = {}) {
  const intervalMs = options.intervalMs ?? 30000
  const now = ref(new Date())
  let intervalId = null

  const syncNow = () => {
    now.value = new Date()
  }

  onMounted(() => {
    syncNow()
    intervalId = setInterval(syncNow, intervalMs)
  })

  onUnmounted(() => {
    if (intervalId) clearInterval(intervalId)
  })

  const dateLabel = computed(() => formatDateLabel(now.value))
  const clockLabel = computed(() => formatClockLabel(now.value))
  const semesterDayNumber = computed(() => getSemesterDayNumber(now.value))
  const semesterLabel = computed(() => `DAG ${semesterDayNumber.value} / SEMESTERET`)
  const issueLabel = computed(() => `Utgave ${semesterDayNumber.value} · Vår 2026`)

  return {
    now,
    dateLabel,
    clockLabel,
    semesterDayNumber,
    semesterLabel,
    issueLabel,
  }
}