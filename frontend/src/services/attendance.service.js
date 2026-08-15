import api, { unwrap } from './api';
import { toQuery } from '../utils/formatters';

const attendanceService = {
  async getAll(params) {
    const qs = toQuery(params);
    const res = await api.get(`/attendance${qs ? `?${qs}` : ''}`);
    return unwrap(res);
  },
  async getById(id) {
    const res = await api.get(`/attendance/${id}`);
    return unwrap(res);
  },
  async getEmployeeAttendance(employeeId, from, to) {
    const qs = toQuery({ from, to });
    const res = await api.get(`/attendance/employee/${employeeId}${qs ? `?${qs}` : ''}`);
    return unwrap(res);
  },
  async monthly(month, year) {
    const qs = toQuery({ month, year });
    const res = await api.get(`/attendance/monthly${qs ? `?${qs}` : ''}`);
    return unwrap(res);
  },
  async record(payload) {
    const res = await api.post('/attendance', payload);
    return unwrap(res);
  },
  async checkIn(payload = {}) {
    const res = await api.post('/attendance/check-in', payload);
    return unwrap(res);
  },
  async checkOut(payload = {}) {
    const res = await api.post('/attendance/check-out', payload);
    return unwrap(res);
  },
};

export default attendanceService;