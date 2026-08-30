import api, { unwrap } from './api';

const BASE = '/reports';

const TYPE_MAP = {
  employee: 'employees',
  department: 'departments',
  designation: 'designations',
  leave: 'leaves',
};

const reportService = {
  async getEmployeeReport(params) {
    const res = await api.get(`${BASE}/employees`, { params });
    return unwrap(res);
  },
  async getEmployeeStatistics() {
    const res = await api.get(`${BASE}/employees/statistics`);
    return unwrap(res);
  },

  async getDepartmentReport(params) {
    const res = await api.get(`${BASE}/departments`, { params });
    return unwrap(res);
  },

  async getDesignationReport(params) {
    const res = await api.get(`${BASE}/designations`, { params });
    return unwrap(res);
  },

  async getAttendanceReport(params) {
    const res = await api.get(`${BASE}/attendance`, { params });
    return unwrap(res);
  },
  async getMonthlyAttendance(params) {
    const res = await api.get(`${BASE}/attendance/monthly`, { params });
    return unwrap(res);
  },
  async getAttendanceStatistics(params) {
    const res = await api.get(`${BASE}/attendance/statistics`, { params });
    return unwrap(res);
  },
  async getOvertimeReport(params) {
    const res = await api.get(`${BASE}/attendance/overtime`, { params });
    return unwrap(res);
  },

  async getLeaveReport(params) {
    const res = await api.get(`${BASE}/leaves`, { params });
    return unwrap(res);
  },
  async getLeaveStatistics(params) {
    const res = await api.get(`${BASE}/leaves/statistics`, { params });
    return unwrap(res);
  },
  async getLeaveBalance(params) {
    const res = await api.get(`${BASE}/leaves/balance`, { params });
    return unwrap(res);
  },

  async getPayrollReport(params) {
    const res = await api.get(`${BASE}/payroll`, { params });
    return unwrap(res);
  },
  async getPayrollStatistics(params) {
    const res = await api.get(`${BASE}/payroll/statistics`, { params });
    return unwrap(res);
  },
  async getDepartmentPayroll(params) {
    const res = await api.get(`${BASE}/payroll/department`, { params });
    return unwrap(res);
  },
  async getPaymentStatusReport(params) {
    const res = await api.get(`${BASE}/payroll/status`, { params });
    return unwrap(res);
  },

  async getPerformanceReport(params) {
    const res = await api.get(`${BASE}/performance`, { params });
    return unwrap(res);
  },
  async getPerformanceStatistics(params) {
    const res = await api.get(`${BASE}/performance/statistics`, { params });
    return unwrap(res);
  },

  async exportPdf(type, params) {
    return api.get(`${BASE}/${TYPE_MAP[type] || type}/export/pdf`, { params, responseType: 'blob' });
  },
  async exportExcel(type, params) {
    return api.get(`${BASE}/${TYPE_MAP[type] || type}/export/excel`, { params, responseType: 'blob' });
  },
  async exportCsv(type, params) {
    return api.get(`${BASE}/${TYPE_MAP[type] || type}/export/csv`, { params, responseType: 'blob' });
  },
};

export default reportService;
