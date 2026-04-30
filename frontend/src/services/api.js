import { clearAuthToken, setAuthToken } from './authState'

const API_URL = import.meta.env.VITE_API_URL || '/api';

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
    const response = await fetch(url, { ...defaultOptions, ...options });
    if (!response.ok) {
      throw new Error(`API error: ${response.status} ${response.statusText}`);
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
  incrementSaidCount: (id) => apiCall(`/liners/${id}/said`, { method: 'PUT' }),
};

export const eventApi = {
  getUpcoming: () => apiCall('/events'),
  getByCategory: (category) => apiCall(`/events/category/${category}`),
  create: (eventDto) => apiCall('/events', { method: 'POST', body: JSON.stringify(eventDto) }),
  toggleAttendance: (eventId, memberName) => apiCall(`/events/${eventId}/attendance/${memberName}`, { method: 'PUT' }),
  delete: (id) => apiCall(`/events/${id}`, { method: 'DELETE' }),
};

export const scoreApi = {
  submit: (scoreSubmitDto) => apiCall('/scores', { method: 'POST', body: JSON.stringify(scoreSubmitDto) }),
  getLeaderboard: (period = 'weekly') => apiCall(`/scores/leaderboard?period=${period}`),
  getScoresForDate: (date) => apiCall(`/scores/daily/${date}`),
};

export const galleryApi = {
  getAll: (category = null) => {
    const query = category ? `?category=${category}` : '';
    return apiCall(`/gallery${query}`);
  },
  upload: (photoDto) => apiCall('/gallery', { method: 'POST', body: JSON.stringify(photoDto) }),
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
  getLatestConnections: () => apiCall('/puzzles/connections/latest'),
  createConnections:    (dto) => apiCall('/puzzles/connections', { method: 'POST', body: JSON.stringify(dto) }),
  getLatestWordle:      () => apiCall('/puzzles/wordle/latest'),
  createWordle:         (dto) => apiCall('/puzzles/wordle', { method: 'POST', body: JSON.stringify(dto) }),
};
