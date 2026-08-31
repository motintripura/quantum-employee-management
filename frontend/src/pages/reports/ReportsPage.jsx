import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import reportService from '../../services/report.service';
import departmentService from '../../services/department.service';
import designationService from '../../services/designation.service';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { getErrorMessage } from '../../services/api';
import { formatMoney, formatDate, formatDateTime, fullName } from '../../utils/formatters';
import { ATTENDANCE_STATUSES, LEAVE_STATUSES, PAYMENT_STATUSES, GENDERS } from '../../utils/constants';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorAlert from '../../components/common/ErrorAlert';
import EmptyState from '../../components/common/EmptyState';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import ReportFilter from '../../components/reports/ReportFilter';
import ReportSummary from '../../components/reports/ReportSummary';
import ReportTable from '../../components/reports/ReportTable';
import ExportButtons from '../../components/reports/ExportButtons';


const PALETTE = ['#2563eb', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#ec4899', '#64748b'];

const COLOR_BY_NAME = {
  PRESENT: '#10b981',
  ABSENT: '#ef4444',
  LATE: '#f59e0b',
  HALF_DAY: '#06b6d4',
  LEAVE: '#8b5cf6',
  PENDING: '#f59e0b',
  APPROVED: '#10b981',
  REJECTED: '#ef4444',
  CANCELLED: '#64748b',
  PAID: '#10b981',
};

const REPORT_TYPES = [
  { value: 'employee', label: 'Employee Report', icon: 'bi-people' },
  { value: 'department', label: 'Department Report', icon: 'bi-diagram-3' },
  { value: 'designation', label: 'Designation Report', icon: 'bi-briefcase' },
  { value: 'attendance', label: 'Attendance Report', icon: 'bi-calendar-check' },
  { value: 'leave', label: 'Leave Report', icon: 'bi-calendar2-week' },
  { value: 'payroll', label: 'Payroll Report', icon: 'bi-cash-stack' },
  { value: 'performance', label: 'Performance Report', icon: 'bi-graph-up' },
];

const downloadBlob = (response, filename) => {
  const url = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', filename);
  document.body.appendChild(link);
  link.click();
  link.remove();
};

function ChartCard({ title, subtitle, children }) {
  return (
    <div className="card h-100">
      <div className="card-body">
        <h6 className="card-title mb-0 fw-semibold">{title}</h6>
        {subtitle && <small className="text-muted">{subtitle}</small>}
        <div className="mt-3" style={{ height: 260 }}>
          {children}
        </div>
      </div>
    </div>
  );
}

function getReportFilters(type, departments, designations) {
  const deptOpts = departments.map((d) => ({ value: d.id || d.name, label: d.name }));
  const desigOpts = designations.map((d) => ({ value: d.id || d.name, label: d.name }));

  switch (type) {
    case 'employee':
      return [
        { key: 'departmentId', label: 'Department', type: 'select', options: deptOpts },
        { key: 'designationId', label: 'Designation', type: 'select', options: desigOpts },
        { key: 'status', label: 'Status', type: 'select', options: [
          { value: 'ACTIVE', label: 'Active' },
          { value: 'INACTIVE', label: 'Inactive' },
        ]},
        { key: 'gender', label: 'Gender', type: 'select', options: GENDERS.map((g) => ({ value: g, label: g })) },
        { key: 'fromDate', label: 'From', type: 'date' },
        { key: 'toDate', label: 'To', type: 'date' },
      ];
    case 'department':
      return [
        { key: 'status', label: 'Status', type: 'select', options: [
          { value: 'ACTIVE', label: 'Active' },
          { value: 'INACTIVE', label: 'Inactive' },
        ]},
      ];
    case 'designation':
      return [
        { key: 'departmentId', label: 'Department', type: 'select', options: deptOpts },
        { key: 'status', label: 'Status', type: 'select', options: [
          { value: 'ACTIVE', label: 'Active' },
          { value: 'INACTIVE', label: 'Inactive' },
        ]},
      ];
    case 'attendance':
      return [
        { key: 'employeeName', label: 'Employee', type: 'text', placeholder: 'Search employee...' },
        { key: 'departmentId', label: 'Department', type: 'select', options: deptOpts },
        { key: 'status', label: 'Status', type: 'select', options: ATTENDANCE_STATUSES.map((s) => ({ value: s, label: s })) },
        { key: 'fromDate', label: 'From', type: 'date' },
        { key: 'toDate', label: 'To', type: 'date' },
      ];
    case 'leave':
      return [
        { key: 'employeeName', label: 'Employee', type: 'text', placeholder: 'Search employee...' },
        { key: 'departmentId', label: 'Department', type: 'select', options: deptOpts },
        { key: 'leaveType', label: 'Leave Type', type: 'text' },
        { key: 'status', label: 'Status', type: 'select', options: LEAVE_STATUSES.map((s) => ({ value: s, label: s })) },
        { key: 'fromDate', label: 'From', type: 'date' },
        { key: 'toDate', label: 'To', type: 'date' },
      ];
    case 'payroll':
      return [
        { key: 'employeeName', label: 'Employee', type: 'text', placeholder: 'Search employee...' },
        { key: 'departmentId', label: 'Department', type: 'select', options: deptOpts },
        { key: 'month', label: 'Month', type: 'month' },
        { key: 'year', label: 'Year', type: 'year' },
        { key: 'paymentStatus', label: 'Payment Status', type: 'select', options: PAYMENT_STATUSES.map((s) => ({ value: s, label: s })) },
      ];
    case 'performance':
      return [
        { key: 'employeeName', label: 'Employee', type: 'text', placeholder: 'Search employee...' },
        { key: 'departmentId', label: 'Department', type: 'select', options: deptOpts },
        { key: 'rating', label: 'Rating', type: 'select', options: [
          { value: '1', label: '1 - Poor' },
          { value: '2', label: '2 - Needs Improvement' },
          { value: '3', label: '3 - Average' },
          { value: '4', label: '4 - Good' },
          { value: '5', label: '5 - Excellent' },
        ]},
        { key: 'fromDate', label: 'From', type: 'date' },
        { key: 'toDate', label: 'To', type: 'date' },
      ];
    default:
      return [];
  }
}

function getReportSummaryItems(type, summary) {
  if (!summary) return [];
  switch (type) {
    case 'employee':
      return [
        { label: 'Total Employees', value: summary.totalEmployees ?? 0, icon: 'bi-people', color: 'primary' },
        { label: 'Active', value: summary.activeEmployees ?? 0, icon: 'bi-person-check', color: 'success' },
        { label: 'Inactive', value: summary.inactiveEmployees ?? 0, icon: 'bi-person-x', color: 'secondary' },
        { label: 'New This Month', value: summary.newEmployeesThisMonth ?? 0, icon: 'bi-person-plus', color: 'info' },
      ];
    case 'department':
      return [
        { label: 'Total Departments', value: summary.totalDepartments ?? 0, icon: 'bi-diagram-3', color: 'primary' },
        { label: 'Active', value: summary.activeDepartments ?? 0, icon: 'bi-check-circle', color: 'success' },
        { label: 'Inactive', value: summary.inactiveDepartments ?? 0, icon: 'bi-x-circle', color: 'secondary' },
      ];
    case 'designation':
      return [
        { label: 'Total Designations', value: summary.totalDesignations ?? 0, icon: 'bi-briefcase', color: 'primary' },
        { label: 'Employees Assigned', value: summary.totalEmployees ?? 0, icon: 'bi-people', color: 'info' },
        { label: 'Avg Salary', value: formatMoney(summary.avgSalary), icon: 'bi-cash', color: 'success' },
      ];
    case 'attendance':
      return [
        { label: 'Working Days', value: summary.totalWorkingDays ?? 0, icon: 'bi-calendar3', color: 'primary' },
        { label: 'Present', value: summary.present ?? 0, icon: 'bi-check-circle', color: 'success' },
        { label: 'Absent', value: summary.absent ?? 0, icon: 'bi-x-circle', color: 'danger' },
        { label: 'Late', value: summary.late ?? 0, icon: 'bi-alarm', color: 'warning' },
        { label: 'Half Day', value: summary.halfDay ?? 0, icon: 'bi-clock-history', color: 'info' },
        { label: 'Leave', value: summary.leaveDays ?? 0, icon: 'bi-calendar2-week', color: 'secondary' },
        { label: 'Attendance %', value: `${summary.attendancePercentage ?? 0}%`, icon: 'bi-percent', color: 'primary' },
        { label: 'Working Hours', value: summary.totalWorkingHours ?? 0, icon: 'bi-hourglass', color: 'success' },
      ];
    case 'leave':
      return [
        { label: 'Total Requests', value: summary.totalLeaveRequests ?? 0, icon: 'bi-file-earmark-text', color: 'primary' },
        { label: 'Pending', value: summary.pending ?? 0, icon: 'bi-clock', color: 'warning' },
        { label: 'Approved', value: summary.approved ?? 0, icon: 'bi-check-circle', color: 'success' },
        { label: 'Rejected', value: summary.rejected ?? 0, icon: 'bi-x-circle', color: 'danger' },
        { label: 'Cancelled', value: summary.cancelled ?? 0, icon: 'bi-slash-circle', color: 'secondary' },
        { label: 'Total Days', value: summary.totalLeaveDays ?? 0, icon: 'bi-calendar3', color: 'info' },
      ];
    case 'payroll':
      return [
        { label: 'Total Payroll', value: summary.totalPayroll ?? 0, icon: 'bi-cash-stack', color: 'primary' },
        { label: 'Gross Salary', value: formatMoney(summary.totalGrossSalary), icon: 'bi-cash', color: 'info' },
        { label: 'Net Salary', value: formatMoney(summary.totalNetSalary), icon: 'bi-wallet2', color: 'success' },
        { label: 'Tax', value: formatMoney(summary.totalTax), icon: 'bi-percent', color: 'danger' },
        { label: 'Deductions', value: formatMoney(summary.totalDeduction), icon: 'bi-dash-circle', color: 'warning' },
        { label: 'Bonuses', value: formatMoney(summary.totalBonus), icon: 'bi-gift', color: 'success' },
        { label: 'Paid', value: summary.paidPayroll ?? 0, icon: 'bi-check-circle', color: 'success' },
        { label: 'Pending', value: summary.pendingPayroll ?? 0, icon: 'bi-clock', color: 'warning' },
      ];
    case 'performance':
      return [
        { label: 'Average Rating', value: summary.averageRating ?? 0, icon: 'bi-star', color: 'primary' },
        { label: 'Top Performers', value: summary.topPerformers ?? 0, icon: 'bi-trophy', color: 'success' },
        { label: 'Lowest Performers', value: summary.lowestPerformers ?? 0, icon: 'bi-arrow-down-circle', color: 'danger' },
        { label: 'Total Reviews', value: summary.totalReviews ?? 0, icon: 'bi-file-earmark-text', color: 'info' },
      ];
    default:
      return [];
  }
}

function getTableConfig(type) {
  switch (type) {
    case 'employee':
      return {
        headers: ['Code', 'Name', 'Email', 'Phone', 'Department', 'Designation', 'Joining Date', 'Salary', 'Status'],
        columns: [
          { key: 'employeeCode' },
          { key: 'fullName', render: (v, row) => fullName(row) },
          { key: 'email' },
          { key: 'phone' },
          { key: 'departmentName' },
          { key: 'designationName' },
          { key: 'joiningDate', render: (v) => formatDate(v) },
          { key: 'salary', render: (v) => formatMoney(v) },
          { key: 'status', render: (v) => <StatusBadge value={v} /> },
        ],
      };
    case 'department':
      return {
        headers: ['Name', 'Description', 'Status', 'Employees', 'Active', 'Inactive', 'Total Salary'],
        columns: [
          { key: 'name' },
          { key: 'description' },
          { key: 'status', render: (v) => <StatusBadge value={v} /> },
          { key: 'employeeCount' },
          { key: 'activeEmployees' },
          { key: 'inactiveEmployees' },
          { key: 'totalSalary', render: (v) => formatMoney(v) },
        ],
      };
    case 'designation':
      return {
        headers: ['Name', 'Department', 'Status', 'Employees', 'Total Salary'],
        columns: [
          { key: 'name' },
          { key: 'departmentName' },
          { key: 'status', render: (v) => <StatusBadge value={v} /> },
          { key: 'employeeCount' },
          { key: 'totalSalary', render: (v) => formatMoney(v) },
        ],
      };
    case 'attendance':
      return {
        headers: ['Code', 'Employee', 'Department', 'Date', 'Check In', 'Check Out', 'Hours', 'Overtime', 'Status'],
        columns: [
          { key: 'employeeCode' },
          { key: 'employeeName' },
          { key: 'departmentName' },
          { key: 'attendanceDate', render: (v) => formatDate(v) },
          { key: 'checkIn', render: (v) => formatDateTime(v) },
          { key: 'checkOut', render: (v) => formatDateTime(v) },
          { key: 'workingHours' },
          { key: 'overtime' },
          { key: 'status', render: (v) => <StatusBadge value={v} /> },
        ],
      };
    case 'leave':
      return {
        headers: ['Code', 'Employee', 'Department', 'Type', 'Start', 'End', 'Days', 'Status', 'Approved By'],
        columns: [
          { key: 'employeeCode' },
          { key: 'employeeName' },
          { key: 'departmentName' },
          { key: 'leaveTypeName' },
          { key: 'startDate', render: (v) => formatDate(v) },
          { key: 'endDate', render: (v) => formatDate(v) },
          { key: 'totalDays' },
          { key: 'status', render: (v) => <StatusBadge value={v} /> },
          { key: 'approvedByName' },
        ],
      };
    case 'payroll':
      return {
        headers: ['Code', 'Employee', 'Department', 'Month', 'Year', 'Basic', 'Overtime', 'Bonus', 'Gross', 'Tax', 'Deduction', 'Net', 'Status'],
        columns: [
          { key: 'employeeCode' },
          { key: 'employeeName' },
          { key: 'departmentName' },
          { key: 'month' },
          { key: 'year' },
          { key: 'basicSalary', render: (v) => formatMoney(v) },
          { key: 'overtime', render: (v) => formatMoney(v) },
          { key: 'bonus', render: (v) => formatMoney(v) },
          { key: 'grossSalary', render: (v) => formatMoney(v) },
          { key: 'tax', render: (v) => formatMoney(v) },
          { key: 'deduction', render: (v) => formatMoney(v) },
          { key: 'netSalary', render: (v) => formatMoney(v) },
          { key: 'paymentStatus', render: (v) => <StatusBadge value={v} /> },
        ],
      };
    case 'performance':
      return {
        headers: ['Code', 'Employee', 'Department', 'Reviewer', 'Date', 'Rating', 'Comments'],
        columns: [
          { key: 'employeeCode' },
          { key: 'employeeName' },
          { key: 'departmentName' },
          { key: 'reviewerName' },
          { key: 'reviewDate', render: (v) => formatDate(v) },
          { key: 'rating' },
          { key: 'comments' },
        ],
      };
    default:
      return { headers: [], columns: [] };
  }
}

function renderCharts(type, chartData) {
  if (!chartData) return null;

  switch (type) {
    case 'employee':
      return (
        <div className="row g-3 mb-3">
          <div className="col-12 col-xl-6">
            <ChartCard title="Employees by Department" subtitle="Distribution across departments">
              {chartData.byDepartment && chartData.byDepartment.length ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={chartData.byDepartment} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                    <XAxis dataKey="name" tick={{ fontSize: 12 }} interval={0} angle={-20} textAnchor="end" height={50} />
                    <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                    <Tooltip />
                    <Bar dataKey="count" name="Employees" fill="#2563eb" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <EmptyState message="No department data" icon="bi-diagram-3" />
              )}
            </ChartCard>
          </div>
          <div className="col-12 col-xl-6">
            <ChartCard title="Employees by Designation" subtitle="Distribution across designations">
              {chartData.byDesignation && chartData.byDesignation.length ? (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={chartData.byDesignation}
                      dataKey="count"
                      nameKey="name"
                      outerRadius={90}
                      label={(p) => `${p.name}`}
                    >
                      {chartData.byDesignation.map((entry, i) => (
                        <Cell key={i} fill={COLOR_BY_NAME[entry.name] || PALETTE[i % PALETTE.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <EmptyState message="No designation data" icon="bi-briefcase" />
              )}
            </ChartCard>
          </div>
        </div>
      );
    case 'attendance':
      return (
        <div className="row g-3 mb-3">
          <div className="col-12 col-xl-6">
            <ChartCard title="Attendance Status Distribution" subtitle="Breakdown by status">
              {chartData.statusDistribution && chartData.statusDistribution.length ? (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={chartData.statusDistribution}
                      dataKey="count"
                      nameKey="name"
                      outerRadius={90}
                      label={(p) => `${p.name}`}
                    >
                      {chartData.statusDistribution.map((entry, i) => (
                        <Cell key={i} fill={COLOR_BY_NAME[entry.name] || PALETTE[i % PALETTE.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <EmptyState message="No attendance data" icon="bi-calendar-check" />
              )}
            </ChartCard>
          </div>
        </div>
      );
    case 'leave':
      return (
        <div className="row g-3 mb-3">
          <div className="col-12 col-xl-6">
            <ChartCard title="Leave Status Distribution" subtitle="Breakdown by status">
              {chartData.statusDistribution && chartData.statusDistribution.length ? (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={chartData.statusDistribution}
                      dataKey="count"
                      nameKey="name"
                      outerRadius={90}
                      label={(p) => `${p.name}`}
                    >
                      {chartData.statusDistribution.map((entry, i) => (
                        <Cell key={i} fill={COLOR_BY_NAME[entry.name] || PALETTE[i % PALETTE.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <EmptyState message="No leave data" icon="bi-calendar2-week" />
              )}
            </ChartCard>
          </div>
        </div>
      );
    case 'payroll':
      return (
        <div className="row g-3 mb-3">
          <div className="col-12 col-xl-6">
            <ChartCard title="Monthly Payroll by Department" subtitle="Gross salary distribution">
              {chartData.byDepartment && chartData.byDepartment.length ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={chartData.byDepartment} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                    <XAxis dataKey="name" tick={{ fontSize: 12 }} interval={0} angle={-20} textAnchor="end" height={50} />
                    <YAxis tick={{ fontSize: 12 }} tickFormatter={(v) => (v >= 1000 ? `${v / 1000}k` : v)} />
                    <Tooltip formatter={(v) => formatMoney(v)} />
                    <Bar dataKey="amount" name="Gross Salary" fill="#2563eb" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <EmptyState message="No payroll data" icon="bi-cash-stack" />
              )}
            </ChartCard>
          </div>
        </div>
      );
    case 'performance':
      return (
        <div className="row g-3 mb-3">
          <div className="col-12 col-xl-6">
            <ChartCard title="Rating Distribution" subtitle="Reviews by rating">
              {chartData.ratingDistribution && chartData.ratingDistribution.length ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={chartData.ratingDistribution} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                    <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                    <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                    <Tooltip />
                    <Bar dataKey="count" name="Reviews" radius={[4, 4, 0, 0]}>
                      {chartData.ratingDistribution.map((entry, i) => (
                        <Cell key={i} fill={PALETTE[i % PALETTE.length]} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <EmptyState message="No performance data" icon="bi-graph-up" />
              )}
            </ChartCard>
          </div>
        </div>
      );
    default:
      return null;
  }
}

export default function ReportsPage() {
  const { user } = useAuth();
  const { error: toastError, success: toastSuccess } = useToast();
  const [searchParams] = useSearchParams();

  const initialType = searchParams.get('type') || 'employee';
  const [reportType, setReportType] = useState(initialType);
  const [filters, setFilters] = useState({});
  const [data, setData] = useState([]);
  const [summary, setSummary] = useState(null);
  const [chartData, setChartData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [departments, setDepartments] = useState([]);
  const [designations, setDesignations] = useState([]);

  const isEmployee = user?.role === 'EMPLOYEE';

  useEffect(() => {
    async function loadDropdowns() {
      try {
        const [depts, desigs] = await Promise.all([
          departmentService.getActive(),
          designationService.getActive(),
        ]);
        setDepartments(Array.isArray(depts) ? depts : depts?.content || []);
        setDesignations(Array.isArray(desigs) ? desigs : desigs?.content || []);
      } catch {
      }
    }
    loadDropdowns();
  }, []);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const params = {};
      Object.entries(filters).forEach(([k, v]) => {
        if (v !== undefined && v !== null && v !== '') {
          if (k === 'status') {
            if (reportType === 'employee' || reportType === 'department' || reportType === 'designation') {
              params.status = v === 'ACTIVE';
            } else {
              params.status = v;
            }
          } else if (k === 'month' || k === 'year') {
            params[k] = Number(v) || undefined;
          } else if (k === 'departmentId' || k === 'designationId') {
            params[k] = Number(v) || undefined;
          } else {
            params[k] = v;
          }
        }
      });

      let reportResult, summaryResult;

      switch (reportType) {
        case 'employee':
          [reportResult, summaryResult] = await Promise.all([
            reportService.getEmployeeReport(params),
            reportService.getEmployeeStatistics(),
          ]);
          const deptMap = {};
          const desigMap = {};
          (reportResult || []).forEach((e) => {
            const d = e.departmentName || 'Unknown';
            deptMap[d] = (deptMap[d] || 0) + 1;
            const des = e.designationName || 'Unknown';
            desigMap[des] = (desigMap[des] || 0) + 1;
          });
          setChartData({
            byDepartment: Object.entries(deptMap).map(([name, count]) => ({ name, count })),
            byDesignation: Object.entries(desigMap).map(([name, count]) => ({ name, count })),
          });
          break;
        case 'department':
          [reportResult, summaryResult] = await Promise.all([
            reportService.getDepartmentReport(params),
            reportService.getDepartmentReport({ ...params, summary: true }),
          ]);
          break;
        case 'designation':
          [reportResult, summaryResult] = await Promise.all([
            reportService.getDesignationReport(params),
            reportService.getDesignationReport({ ...params, summary: true }),
          ]);
          break;
        case 'attendance':
          [reportResult, summaryResult] = await Promise.all([
            reportService.getAttendanceReport(params),
            reportService.getAttendanceStatistics({
              employeeId: params.employeeId,
              departmentId: params.departmentId,
              month: params.month,
              year: params.year,
            }),
          ]);
          const statusMap = {};
          (reportResult || []).forEach((a) => {
            const s = a.status || 'UNKNOWN';
            statusMap[s] = (statusMap[s] || 0) + 1;
          });
          setChartData({
            statusDistribution: Object.entries(statusMap).map(([name, count]) => ({ name, count })),
          });
          break;
        case 'leave':
          [reportResult, summaryResult] = await Promise.all([
            reportService.getLeaveReport(params),
            reportService.getLeaveStatistics({ departmentId: params.departmentId }),
          ]);
          const leaveStatusMap = {};
          (reportResult || []).forEach((l) => {
            const s = l.status || 'UNKNOWN';
            leaveStatusMap[s] = (leaveStatusMap[s] || 0) + 1;
          });
          setChartData({
            statusDistribution: Object.entries(leaveStatusMap).map(([name, count]) => ({ name, count })),
          });
          break;
        case 'payroll':
          [reportResult, summaryResult] = await Promise.all([
            reportService.getPayrollReport(params),
            reportService.getPayrollStatistics({
              month: params.month,
              year: params.year,
              departmentId: params.departmentId,
            }),
          ]);
          const payrollDeptMap = {};
          (reportResult || []).forEach((p) => {
            const d = p.departmentName || 'Unknown';
            payrollDeptMap[d] = (payrollDeptMap[d] || 0) + Number(p.grossSalary || 0);
          });
          setChartData({
            byDepartment: Object.entries(payrollDeptMap).map(([name, amount]) => ({ name, amount })),
          });
          break;
        case 'performance':
          [reportResult, summaryResult] = await Promise.all([
            reportService.getPerformanceReport(params),
            reportService.getPerformanceStatistics({ departmentId: params.departmentId }),
          ]);
          const ratingMap = {};
          (reportResult || []).forEach((p) => {
            const r = p.rating || 0;
            ratingMap[r] = (ratingMap[r] || 0) + 1;
          });
          setChartData({
            ratingDistribution: Object.entries(ratingMap)
              .map(([name, count]) => ({ name: `Rating ${name}`, count }))
              .sort((a, b) => a.name.localeCompare(b.name)),
          });
          break;
        default:
          break;
      }

      setData(Array.isArray(reportResult) ? reportResult : reportResult?.content || []);
      setSummary(summaryResult || null);
    } catch (e) {
      const msg = getErrorMessage(e);
      setError(msg);
      toastError(msg);
    } finally {
      setLoading(false);
    }
  }, [reportType, filters, toastError]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  useEffect(() => {
    setFilters({});
  }, [reportType]);

  const handleFilterChange = useCallback((key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
  }, []);

  const handleReset = useCallback(() => {
    setFilters({});
  }, []);

  const handleExportPdf = useCallback(async () => {
    try {
      const params = {};
      Object.entries(filters).forEach(([k, v]) => {
        if (v !== undefined && v !== null && v !== '') params[k] = v;
      });
      const res = await reportService.exportPdf(reportType, params);
      downloadBlob(res, `${reportType}-report.pdf`);
      toastSuccess('PDF exported successfully');
    } catch (e) {
      toastError(getErrorMessage(e));
    }
  }, [reportType, filters, toastSuccess, toastError]);

  const handleExportExcel = useCallback(async () => {
    try {
      const params = {};
      Object.entries(filters).forEach(([k, v]) => {
        if (v !== undefined && v !== null && v !== '') params[k] = v;
      });
      const res = await reportService.exportExcel(reportType, params);
      downloadBlob(res, `${reportType}-report.xlsx`);
      toastSuccess('Excel exported successfully');
    } catch (e) {
      toastError(getErrorMessage(e));
    }
  }, [reportType, filters, toastSuccess, toastError]);

  const handleExportCsv = useCallback(async () => {
    try {
      const params = {};
      Object.entries(filters).forEach(([k, v]) => {
        if (v !== undefined && v !== null && v !== '') params[k] = v;
      });
      const res = await reportService.exportCsv(reportType, params);
      downloadBlob(res, `${reportType}-report.csv`);
      toastSuccess('CSV exported successfully');
    } catch (e) {
      toastError(getErrorMessage(e));
    }
  }, [reportType, filters, toastSuccess, toastError]);

  const handlePrint = useCallback(() => {
    window.print();
  }, []);

  const filterDefs = getReportFilters(reportType, departments, designations);
  const summaryItems = getReportSummaryItems(reportType, summary);
  const { headers, columns } = getTableConfig(reportType);
  const reportLabel = REPORT_TYPES.find((r) => r.value === reportType)?.label || 'Report';

  return (
    <div>
      <PageHeader
        title="Reports"
        subtitle="Generate and export comprehensive reports"
        actions={
          <div className="d-flex align-items-center gap-2">
            <select
              className="form-select form-select-sm"
              value={reportType}
              onChange={(e) => setReportType(e.target.value)}
              style={{ minWidth: 180 }}
            >
              {REPORT_TYPES.map((rt) => (
                <option key={rt.value} value={rt.value}>
                  {rt.label}
                </option>
              ))}
            </select>
          </div>
        }
      />

      {error && <ErrorAlert message={error} />}

      <div className="report-printable">
        <ReportFilter
          filters={filterDefs}
          values={filters}
          onChange={handleFilterChange}
          onReset={handleReset}
        />

        {loading ? (
          <LoadingSpinner full text={`Loading ${reportLabel.toLowerCase()}...`} />
        ) : (
          <>
            {summaryItems.length > 0 && <ReportSummary items={summaryItems} />}

            {renderCharts(reportType, chartData)}

            <div className="card border-0 shadow-sm mb-4 report-table-print">
              <div className="card-header bg-transparent d-flex justify-content-between align-items-center">
                <h6 className="mb-0 fw-semibold">{reportLabel}</h6>
                {!isEmployee && (
                  <ExportButtons
                    onExportPdf={handleExportPdf}
                    onExportExcel={handleExportExcel}
                    onExportCsv={handleExportCsv}
                    onPrint={handlePrint}
                    disabled={loading || !data.length}
                  />
                )}
              </div>
              <div className="card-body p-0">
                <ReportTable
                  headers={headers}
                  data={data}
                  columns={columns}
                  emptyMessage={`No ${reportLabel.toLowerCase()} data found`}
                />
              </div>
            </div>

            {isEmployee && (
              <div className="text-end">
                <ExportButtons
                  onExportPdf={handleExportPdf}
                  onExportExcel={handleExportExcel}
                  onExportCsv={handleExportCsv}
                  onPrint={handlePrint}
                  disabled={loading || !data.length}
                />
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
