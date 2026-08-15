import api, { unwrap } from './api';
import { toQuery } from '../utils/formatters';

const payrollService = {
  async getAll(params) {
    const qs = toQuery(params);
    const res = await api.get(`/payroll${qs ? `?${qs}` : ''}`);
    return unwrap(res);
  },
  async getById(id) {
    const res = await api.get(`/payroll/${id}`);
    return unwrap(res);
  },
  async getEmployeePayroll(employeeId) {
    const res = await api.get(`/payroll/employee/${employeeId}`);
    return unwrap(res);
  },
  async getPayslip(id) {
    const res = await api.get(`/payroll/${id}/payslip`);
    return unwrap(res);
  },
  async create(payload) {
    const res = await api.post('/payroll', payload);
    return unwrap(res);
  },
  async update(id, payload) {
    const res = await api.put(`/payroll/${id}`, payload);
    return unwrap(res);
  },
  async markPaid(id) {
    const res = await api.patch(`/payroll/${id}/pay`);
    return unwrap(res);
  },
};

export default payrollService;