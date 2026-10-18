import axios from 'axios';
import { API_URL, TOKEN_KEY, USER_KEY } from '../utils/constants';

const api = axios.create({ baseURL: `${API_URL}/api`, timeout: 15000 });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const url = err.config?.url || '';
    const isAuthCall = url.startsWith('/auth/login') || url.startsWith('/auth/register');
    // Expired or invalid token: clear the session and go to login
    if (err.response?.status === 401 && localStorage.getItem(TOKEN_KEY) && !isAuthCall) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      if (!window.location.pathname.startsWith('/login')) {
        window.location.assign('/login');
      }
    }
    return Promise.reject(err);
  }
);

// Message to show the user for any failed request
export const errorMessage = (err, fallback = 'Something went wrong. Please try again.') => {
  if (err?.response?.data?.message) return err.response.data.message;
  if (err?.request && !err.response) return 'Cannot reach the server. Check that the backend is running.';
  return fallback;
};

export default api;