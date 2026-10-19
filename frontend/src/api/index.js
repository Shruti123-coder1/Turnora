import api from './axiosConfig';

const data = (promise) => promise.then((res) => res.data);

export const authApi = {
  login: (body) => data(api.post('/auth/login', body)),
  register: (body) => data(api.post('/auth/register', body)),
  me: () => data(api.get('/auth/me')),
  updateProfile: (body) => data(api.put('/auth/profile', body)),
  changePassword: (body) => data(api.put('/auth/password', body)),
};

export const servicesApi = {
  list: () => data(api.get('/services')),
};

export const ticketApi = {
  create: (body) => data(api.post('/tickets', body)),
  mine: () => data(api.get('/tickets/my')),
  track: (token) => data(api.get(`/tickets/${encodeURIComponent(token)}`)),
  cancel: (id) => data(api.put(`/tickets/${id}/cancel`)),
};

export const notificationApi = {
  list: () => data(api.get('/notifications')),
  unreadCount: () => data(api.get('/notifications/unread-count')),
  markRead: (id) => data(api.put(`/notifications/${id}/read`)),
  markAllRead: () => data(api.put('/notifications/read-all')),
};

export const counterApi = {
  list: () => data(api.get('/counters')),
  state: (id) => data(api.get(`/counters/${id}/state`)),
  next: (id) => data(api.post(`/counters/${id}/next`)),
  start: (id) => data(api.put(`/counters/${id}/start`)),
  complete: (id) => data(api.put(`/counters/${id}/complete`)),
  noShow: (id) => data(api.put(`/counters/${id}/no-show`)),
  transfer: (id, serviceId) => data(api.put(`/counters/${id}/transfer`, { serviceId })),
  setStatus: (id, status) => data(api.put(`/counters/${id}/status`, { status })),
  board: () => data(api.get('/board')),
};

export const adminApi = {
  services: () => data(api.get('/admin/services')),
  createService: (body) => data(api.post('/admin/services', body)),
  updateService: (id, body) => data(api.put(`/admin/services/${id}`, body)),

  counters: () => data(api.get('/admin/counters')),
  createCounter: (body) => data(api.post('/admin/counters', body)),
  updateCounter: (id, body) => data(api.put(`/admin/counters/${id}`, body)),
  deleteCounter: (id) => data(api.delete(`/admin/counters/${id}`)),

  users: () => data(api.get('/admin/users')),
  createUser: (body) => data(api.post('/admin/users', body)),
  setUserActive: (id, active) => data(api.put(`/admin/users/${id}/active`, { active })),

  analytics: (date) => data(api.get('/admin/analytics', { params: date ? { date } : {} })),
  history: (params) => data(api.get('/admin/history', { params })),
  events: (ticketId) => data(api.get(`/admin/history/${ticketId}/events`)),
};
