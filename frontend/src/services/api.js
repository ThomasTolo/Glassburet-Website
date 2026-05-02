import { clearAuthToken, setAuthToken } from './authState'

const API_URL = import.meta.env.VITE_API_URL || '/api';
const BACKEND_ORIGIN = API_URL.startsWith('http') ? API_URL.replace(/\/api\/?$/, '') : '';
export const resolveMediaUrl = (url) => (url && BACKEND_ORIGIN && url.startsWith('/uploads/')) ? BACKEND_ORIGIN + url : url;

const apiCall = async (endpoint, options = {}) => {
  const url = `${API_URL}${endpoint}`;
  const defaultOptions = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // attach stored Bearer token if present
  const token = localStorage.getItem('token');
  if (token) {
    defaultOptions.headers['Authorization'] = `Bearer ${token}`;
  }

  try {
    // If caller passed FormData, let the browser set Content-Type (boundary)
    if (options.body instanceof FormData) {
      delete defaultOptions.headers['Content-Type'];
    }
    const response = await fetch(url, { ...defaultOptions, ...options });
    if (!response.ok) {
      if ((response.status === 401 || response.status === 403) && (endpoint.startsWith('/puzzles') || endpoint.startsWith('/scores/completed') || (endpoint === '/scores' && options.method === 'POST'))) {
        clearAuthToken();
      }
      throw new Error(`API error: ${response.status} ${response.statusText}`);
    }
    if (response.status === 204) {
      return null;
    }
    return await response.json();
  } catch (error) {
    console.error(`API call failed: ${endpoint}`, error);
    throw error;
  }
};

export const quoteApi = {
  getAll: () => apiCall('/quotes'),
  getFeatured: () => apiCall('/quotes/featured'),
  create: (quoteDto) => apiCall('/quotes', { method: 'POST', body: JSON.stringify(quoteDto) }),
  update: (id, quoteDto) => apiCall(`/quotes/${id}`, { method: 'PUT', body: JSON.stringify(quoteDto) }),
  like: (id) => apiCall(`/quotes/${id}/like`, { method: 'PUT' }),
  delete: (id) => apiCall(`/quotes/${id}`, { method: 'DELETE' }),
};

export const auth = {
  login: async (name, password) => {
    const res = await fetch(`${API_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, password }),
    });
    if (!res.ok) throw new Error('Login failed');
    const data = await res.json();
    setAuthToken(data.token);
    return data.token;
  },
  setToken: (token) => setAuthToken(token),
  clearAuth: () => clearAuthToken(),
  getAuth: () => localStorage.getItem('token'),
};

export const linerApi = {
  getAll: () => apiCall('/liners'),
  create: (linerDto) => apiCall('/liners', { method: 'POST', body: JSON.stringify(linerDto) }),
  update: (id, linerDto) => apiCall(`/liners/${id}`, { method: 'PUT', body: JSON.stringify(linerDto) }),
  delete: (id) => apiCall(`/liners/${id}`, { method: 'DELETE' }),
  incrementSaidCount: (id) => apiCall(`/liners/${id}/said`, { method: 'PUT' }),
  like: (id) => apiCall(`/liners/${id}/like`, { method: 'PUT' }),
};

export const eventApi = {
  getUpcoming: () => apiCall('/events'),
  getByCategory: (category) => apiCall(`/events/category/${category}`),
  create: (eventDto) => apiCall('/events', { method: 'POST', body: JSON.stringify(eventDto) }),
  update: (id, eventDto) => apiCall(`/events/${id}`, { method: 'PUT', body: JSON.stringify(eventDto) }),
  toggleAttendance: (eventId, memberName) => apiCall(`/events/${eventId}/attendance/${encodeURIComponent(memberName)}`, { method: 'PUT' }),
  delete: (id) => apiCall(`/events/${id}`, { method: 'DELETE' }),
};

export const scoreApi = {
  submit: (scoreSubmitDto) => apiCall('/scores', { method: 'POST', body: JSON.stringify(scoreSubmitDto) }),
  getLeaderboard: (period = 'weekly', game = '') => apiCall(`/scores/leaderboard?period=${period}${game ? `&game=${encodeURIComponent(game)}` : ''}`),
  getScoresForDate: (date) => apiCall(`/scores/daily/${date}`),
  getCompleted: (memberName, gameName) => apiCall(`/scores/completed?memberName=${encodeURIComponent(memberName)}&gameName=${encodeURIComponent(gameName)}`),
};

export const galleryApi = {
  getAll: (category = null) => {
    const query = category ? `?category=${category}` : '';
    return apiCall(`/gallery${query}`);
  },
  upload: (photoDto) => apiCall('/gallery', { method: 'POST', body: JSON.stringify(photoDto) }),
  uploadFile: (formData) => apiCall('/gallery/upload', { method: 'POST', body: formData }),
  update: (id, photoDto) => apiCall(`/gallery/${id}`, { method: 'PUT', body: JSON.stringify(photoDto) }),
  like: (id) => apiCall(`/gallery/${id}/like`, { method: 'PUT' }),
  delete: (id) => apiCall(`/gallery/${id}`, { method: 'DELETE' }),
};

export const statsApi = {
  getStats: () => apiCall('/stats'),
};

export const memberApi = {
  getAll: () => apiCall('/auth/members'),
  updateRole: (name, role) => apiCall(`/auth/members/${encodeURIComponent(name)}/role`, {
    method: 'PUT',
    body: JSON.stringify({ role }),
  }),
};

export const puzzleApi = {
  getAllConnections:     () => apiCall('/puzzles/connections'),
  getDailyConnections:  () => apiCall('/puzzles/connections/daily'),
  getLatestConnections: () => apiCall('/puzzles/connections/latest'),
  createConnections:    (dto) => apiCall('/puzzles/connections', { method: 'POST', body: JSON.stringify(dto) }),
  updateConnections:    (id, dto) => apiCall(`/puzzles/connections/${id}`, { method: 'PUT', body: JSON.stringify(dto) }),
  deleteConnections:    (id) => apiCall(`/puzzles/connections/${id}`, { method: 'DELETE' }),
  validateConnections:  (id, words) => apiCall(`/puzzles/connections/${id}/validate`, { method: 'POST', body: JSON.stringify({ words }) }),
  getAllWordle:          () => apiCall('/puzzles/wordle'),
  getDailyWordle:       () => apiCall('/puzzles/wordle/daily'),
  getLatestWordle:      () => apiCall('/puzzles/wordle/latest'),
  createWordle:         (dto) => apiCall('/puzzles/wordle', { method: 'POST', body: JSON.stringify(dto) }),
  updateWordle:         (id, dto) => apiCall(`/puzzles/wordle/${id}`, { method: 'PUT', body: JSON.stringify(dto) }),
  deleteWordle:         (id) => apiCall(`/puzzles/wordle/${id}`, { method: 'DELETE' }),
  validateWordle:       (id, guess) => apiCall(`/puzzles/wordle/${id}/validate`, { method: 'POST', body: JSON.stringify({ guess }) }),
  getAllNative:         (gameName) => apiCall(`/puzzles/native/${encodeURIComponent(gameName)}`),
  getDailyNative:       (gameName) => apiCall(`/puzzles/native/${encodeURIComponent(gameName)}/daily`),
  createNative:         (gameName, dto) => apiCall(`/puzzles/native/${encodeURIComponent(gameName)}`, { method: 'POST', body: JSON.stringify(dto) }),
  updateNative:         (gameName, id, dto) => apiCall(`/puzzles/native/${encodeURIComponent(gameName)}/${id}`, { method: 'PUT', body: JSON.stringify(dto) }),
  deleteNative:         (gameName, id) => apiCall(`/puzzles/native/${encodeURIComponent(gameName)}/${id}`, { method: 'DELETE' }),
  validateNative:       (gameName, id, body) => apiCall(`/puzzles/native/${encodeURIComponent(gameName)}/${id}/validate`, { method: 'POST', body: JSON.stringify(body) }),
};
