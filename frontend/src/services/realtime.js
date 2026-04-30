const API_URL = import.meta.env.VITE_API_URL || '/api'

const listeners = new Map()
let socket = null
let reconnectTimer = null

function websocketUrl() {
  if (API_URL.startsWith('http')) {
    return API_URL.replace(/^http/, 'ws').replace(/\/api\/?$/, '/ws/updates')
  }
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}/ws/updates`
}

function connect() {
  if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
    return
  }

  socket = new WebSocket(websocketUrl())
  socket.addEventListener('message', (event) => {
    try {
      const data = JSON.parse(event.data)
      const callbacks = listeners.get(data.type)
      if (callbacks) callbacks.forEach(callback => callback(data))
    } catch {}
  })
  socket.addEventListener('close', () => {
    socket = null
    if (listeners.size > 0 && !reconnectTimer) {
      reconnectTimer = window.setTimeout(() => {
        reconnectTimer = null
        connect()
      }, 2000)
    }
  })
}

export function subscribeToUpdates(type, callback) {
  if (!listeners.has(type)) listeners.set(type, new Set())
  listeners.get(type).add(callback)
  connect()

  return () => {
    const callbacks = listeners.get(type)
    if (!callbacks) return
    callbacks.delete(callback)
    if (callbacks.size === 0) listeners.delete(type)
  }
}
