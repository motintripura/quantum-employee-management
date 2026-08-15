import axios from 'axios';
import { API_BASE_URL, TOKEN_KEY, USER_KEY } from '../utils/constants';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401 && !window.location.pathname.startsWith('/login')) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export function getErrorMessage(error) {
  const data = error && error.response ? error.response.data : null;
  if (data && data.message) return data.message;
  if (error && error.message) return error.message;
  return 'Something went wrong. Please try again.';
}

export function getFieldErrors(error) {
  const data = error && error.response ? error.response.data : null;
  return (data && data.errors) || {};
}

export function unwrap(response) {
  return response.data.data;
}

export default api;