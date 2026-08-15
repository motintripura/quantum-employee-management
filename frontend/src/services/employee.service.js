import api, { unwrap } from './api';
import { toQuery } from '../utils/formatters';

const employeeService = {
  async getAll(params) {
    const qs = toQuery(params);
    const res = await api.get(`/employees${qs ? `?${qs}` : ''}`);
    return unwrap(res);
  },
  async getById(id) {
    const res = await api.get(`/employees/${id}`);
    return unwrap(res);
  },
  async generateCode() {
    const res = await api.get('/employees/code/generate');
    return unwrap(res);
  },
  async create(payload) {
    const res = await api.post('/employees', payload);
    return unwrap(res);
  },
  async update(id, payload) {
    const res = await api.put(`/employees/${id}`, payload);
    return unwrap(res);
  },
  async toggleStatus(id) {
    const res = await api.patch(`/employees/${id}/status`);
    return unwrap(res);
  },
  async remove(id) {
    const res = await api.delete(`/employees/${id}`);
    return unwrap(res);
  },
  async uploadImage(id, file) {
    const form = new FormData();
    form.append('file', file);
    const res = await api.post(`/employees/${id}/image`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return unwrap(res);
  },
};

export default employeeService;