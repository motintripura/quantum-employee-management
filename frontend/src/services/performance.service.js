import api, { unwrap } from './api';
import { toQuery } from '../utils/formatters';

const performanceService = {
  async getAll(params) {
    const qs = toQuery(params);
    const res = await api.get(`/performance${qs ? `?${qs}` : ''}`);
    return unwrap(res);
  },
  async getById(id) {
    const res = await api.get(`/performance/${id}`);
    return unwrap(res);
  },
  async getEmployeePerformance(employeeId) {
    const res = await api.get(`/performance/employee/${employeeId}`);
    return unwrap(res);
  },
  async create(payload) {
    const res = await api.post('/performance', payload);
    return unwrap(res);
  },
  async update(id, payload) {
    const res = await api.put(`/performance/${id}`, payload);
    return unwrap(res);
  },
  async remove(id) {
    const res = await api.delete(`/performance/${id}`);
    return unwrap(res);
  },
  async ratingReport() {
    const res = await api.get('/performance/report/rating');
    return unwrap(res);
  },
};

export default performanceService;