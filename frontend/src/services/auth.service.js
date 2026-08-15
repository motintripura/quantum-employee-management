import api, { unwrap } from './api';

const authService = {
  async login(credentials) {
    const res = await api.post('/auth/login', credentials);
    return unwrap(res);
  },
  async register(payload) {
    const res = await api.post('/auth/register', payload);
    return unwrap(res);
  },
  async me() {
    const res = await api.get('/auth/me');
    return unwrap(res);
  },
  async profile() {
    const res = await api.get('/auth/profile');
    return unwrap(res);
  },
};

export default authService;