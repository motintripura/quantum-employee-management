import api, { unwrap } from './api';
import { toQuery } from '../utils/formatters';

const departmentService = {
  async getAll(params) {
    const qs = toQuery(params);
    const res = await api.get(`/departments${qs ? `?${qs}` : ''}`);
    return unwrap(res);
  },
  async getActive() {
    const res = await api.get('/departments/active');
    return unwrap(res);
  },
  async getById(id) {
    const res = await api.get(`/departments/${id}`);
    return unwrap(res);
  },
  async create(payload) {
    const res = await api.post('/departments', payload);
    return unwrap(res);
  },
  async update(id, payload) {
    const res = await api.put(`/departments/${id}`, payload);
    return unwrap(res);
  },
  async toggleStatus(id) {
    const res = await api.patch(`/departments/${id}/status`);
    return unwrap(res);
  },
  async remove(id) {
    const res = await api.delete(`/departments/${id}`);
    return unwrap(res);
  },
};

export default departmentService;