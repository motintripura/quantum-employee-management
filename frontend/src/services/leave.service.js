import api, { unwrap } from './api';
import { toQuery } from '../utils/formatters';

const leaveService = {
  async getAll(params) {
    const qs = toQuery(params);
    const res = await api.get(`/leaves${qs ? `?${qs}` : ''}`);
    return unwrap(res);
  },
  async getById(id) {
    const res = await api.get(`/leaves/${id}`);
    return unwrap(res);
  },
  async getEmployeeLeaves(employeeId) {
    const res = await api.get(`/leaves/employee/${employeeId}`);
    return unwrap(res);
  },
  async apply(payload) {
    const res = await api.post('/leaves', payload);
    return unwrap(res);
  },
  async approve(id) {
    const res = await api.put(`/leaves/${id}/approve`);
    return unwrap(res);
  },
  async reject(id) {
    const res = await api.put(`/leaves/${id}/reject`);
    return unwrap(res);
  },
  async cancel(id) {
    const res = await api.put(`/leaves/${id}/cancel`);
    return unwrap(res);
  },
  async reportByStatus() {
    const res = await api.get('/leaves/report/status');
    return unwrap(res);
  },
};

const leaveTypeService = {
  async getAll() {
    const res = await api.get('/leave-types');
    return unwrap(res);
  },
  async getActive() {
    const res = await api.get('/leave-types/active');
    return unwrap(res);
  },
  async create(payload) {
    const res = await api.post('/leave-types', payload);
    return unwrap(res);
  },
  async update(id, payload) {
    const res = await api.put(`/leave-types/${id}`, payload);
    return unwrap(res);
  },
  async toggleStatus(id) {
    const res = await api.patch(`/leave-types/${id}/status`);
    return unwrap(res);
  },
  async remove(id) {
    const res = await api.delete(`/leave-types/${id}`);
    return unwrap(res);
  },
};

const leaveBalanceService = {
  async getBalances(employeeId, year) {
    const qs = toQuery({ employeeId, year });
    const res = await api.get(`/leave-balances${qs ? `?${qs}` : ''}`);
    return unwrap(res);
  },
  async initialize(employeeId, year) {
    const qs = toQuery({ employeeId, year });
    const res = await api.post(`/leave-balances/initialize${qs ? `?${qs}` : ''}`);
    return unwrap(res);
  },
};

export { leaveTypeService, leaveBalanceService };
export default leaveService;