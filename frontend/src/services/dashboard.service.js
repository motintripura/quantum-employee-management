import api, { unwrap } from './api';

const dashboardService = {
  async statistics() {
    const res = await api.get('/dashboard/statistics');
    return unwrap(res);
  },
  async departmentDistribution() {
    const res = await api.get('/dashboard/department');
    return unwrap(res);
  },
  async attendance() {
    const res = await api.get('/dashboard/attendance');
    return unwrap(res);
  },
  async leave() {
    const res = await api.get('/dashboard/leave');
    return unwrap(res);
  },
  async payroll(year) {
    const res = await api.get(`/dashboard/payroll${year ? `?year=${year}` : ''}`);
    return unwrap(res);
  },
  async performance() {
    const res = await api.get('/dashboard/performance');
    return unwrap(res);
  },
  async employeeStatus() {
    const res = await api.get('/dashboard/employee-status');
    return unwrap(res);
  },
  async recentActivities(limit = 10) {
    const res = await api.get(`/dashboard/recent-activities?limit=${limit}`);
    return unwrap(res);
  },
};

export default dashboardService;