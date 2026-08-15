import api, { unwrap } from './api';
import { toQuery } from '../utils/formatters';

const designationService = {
  async getAll(params) {
    const qs = toQuery(params);
    const res = await api.get(`/designations${qs ? `?${qs}` : ''}`);
    return unwrap(res);
  },
  async getActive(departmentId) {
    const res = await api.get(`/designations/active${departmentId ? `?departmentId=${departmentId}` : ''}`);
    return unwrap(res);
  },
  async getById(id) {
    const res = await api.get(`/designations/${id}`);
    return unwrap(res);
  },
  async create(payload) {
    const res = await api.post('/designations', payload);
    return unwrap(res);
  },
  async update(id, payload) {
    const res = await api.put(`/designations/${id}`, payload);
    return unwrap(res);
  },
  async toggleStatus(id) {
    const res = await api.patch(`/designations/${id}/status`);
    return unwrap(res);
  },
  async remove(id) {
    const res = await api.delete(`/designations/${id}`);
    return unwrap(res);
  },
};

export default designationService;