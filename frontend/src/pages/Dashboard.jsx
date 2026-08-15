import { useEffect, useState, useCallback } from 'react';
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
  LineChart,
  Line,
} from 'recharts';
import dashboardService from '../services/dashboard.service';
import { useToast } from '../context/ToastContext';
import { getErrorMessage } from '../services/api';
import { formatMoney, currentYear } from '../utils/formatters';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorAlert from '../components/common/ErrorAlert';
import PageHeader from '../components/common/PageHeader';
import EmptyState from '../components/common/EmptyState';

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
};

function StatCard({ label, value, icon, color }) {
  return (
    <div className="stat-card">
      <div className={`stat-icon bg-${color}-subtle text-${color}`}>
        <i className={`bi ${icon}`} />
      </div>
      <div className="min-w-0">
        <div className="fs-4 fw-bold text-body">{value == null ? 0 : value}</div>
        <div className="text-muted small text-truncate">{label}</div>
      </div>
    </div>
  );
}

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

export default function Dashboard() {
  const { error: toastError } = useToast();
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');
  const [year, setYear] = useState(currentYear());
  const [data, setData] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setErr('');
    try {
      const [stats, department, attendance, leave, payroll, performance, employeeStatus, activities] =
        await Promise.all([
          dashboardService.statistics(),
          dashboardService.departmentDistribution(),
          dashboardService.attendance(),
          dashboardService.leave(),
          dashboardService.payroll(year),
          dashboardService.performance(),
          dashboardService.employeeStatus(),
          dashboardService.recentActivities(10),
        ]);
      setData({ stats, department, attendance, leave, payroll, performance, employeeStatus, activities });
    } catch (e) {
      const msg = getErrorMessage(e);
      setErr(msg);
      toastError(msg);
    } finally {
      setLoading(false);
    }
  }, [year, toastError]);

  useEffect(() => {
    load();
  }, [load]);

  if (loading) return <LoadingSpinner full text="Loading dashboard..." />;

  const d = data || {};
  const stats = d.stats || {};

  const statCards = [
    { label: 'Total Employees', value: stats.totalEmployees, icon: 'bi-people', color: 'primary' },
    { label: 'Active Employees', value: stats.activeEmployees, icon: 'bi-person-check', color: 'success' },
    { label: 'Inactive Employees', value: stats.inactiveEmployees, icon: 'bi-person-x', color: 'secondary' },
    { label: 'Departments', value: stats.totalDepartments, icon: 'bi-diagram-3', color: 'info' },
    { label: "Today's Present", value: stats.todayPresent, icon: 'bi-calendar-check', color: 'success' },
    { label: "Today's Absent", value: stats.todayAbsent, icon: 'bi-calendar-x', color: 'danger' },
    { label: 'Late Today', value: stats.todayLate, icon: 'bi-alarm', color: 'warning' },
    { label: 'Pending Leaves', value: stats.pendingLeaveRequests, icon: 'bi-clock-history', color: 'warning' },
  ];

  const ratingData = (d.performance || []).map((r) => ({
    name: r.name.replace('rating_', 'Rating '),
    count: r.count,
  }));

  return (
    <div>
      <PageHeader
        title="Dashboard"
        subtitle="Overview of your organization at a glance"
        actions={
          <div className="d-flex align-items-center gap-2">
            <label className="form-label mb-0 small">Year</label>
            <input
              type="number"
              className="form-control form-control-sm"
              style={{ width: 90 }}
              value={year}
              min={2000}
              onChange={(e) => setYear(Number(e.target.value) || currentYear())}
            />
            <button className="btn btn-sm btn-outline-primary" onClick={load}>
              <i className="bi bi-arrow-clockwise me-1" />
              Refresh
            </button>
          </div>
        }
      />

      {err && <ErrorAlert message={err} />}

      <div className="row g-3 mb-3">
        {statCards.map((card) => (
          <div key={card.label} className="col-6 col-md-3 col-xl-3">
            <StatCard {...card} />
          </div>
        ))}
      </div>

      <div className="row g-3 mb-3">
        <div className="col-12 col-xl-6">
          <div className="stat-card h-100">
            <div className="stat-icon bg-primary-subtle text-primary">
              <i className="bi bi-cash-stack" />
            </div>
            <div className="min-w-0">
              <div className="fs-4 fw-bold text-body">{formatMoney(stats.monthlyPayrollTotal)}</div>
              <div className="text-muted small">
                Monthly Payroll Total ({new Date().toLocaleString('en-US', { month: 'long', year: 'numeric' })})
              </div>
            </div>
          </div>
        </div>
        <div className="col-12 col-xl-6">
          <div className="card h-100">
            <div className="card-body py-3">
              <h6 className="card-title mb-2 fw-semibold">Recent Activities</h6>
              {d.activities && d.activities.length ? (
                <ul className="list-unstyled mb-0" style={{ maxHeight: 120, overflowY: 'auto' }}>
                  {d.activities.map((a, i) => (
                    <li key={i} className="d-flex justify-content-between gap-2 py-1 border-bottom small">
                      <span className="text-truncate">
                        <span className="badge text-bg-light me-2 text-uppercase">{a.type}</span>
                        {a.description}
                      </span>
                      <span className="text-muted text-nowrap">{String(a.timestamp).replace('T', ' ').slice(0, 16)}</span>
                    </li>
                  ))}
                </ul>
              ) : (
                <EmptyState message="No recent activity" icon="bi-activity" />
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="row g-3 mb-3">
        <div className="col-12 col-xl-6">
          <ChartCard title="Employees by Department" subtitle="Distribution across departments">
            {d.department && d.department.length ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={d.department} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
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
          <ChartCard title="Attendance Overview" subtitle="Today's attendance by status">
            {d.attendance && d.attendance.length ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={d.attendance}
                    dataKey="count"
                    nameKey="name"
                    outerRadius={90}
                    label={(p) => `${p.name}`}
                  >
                    {d.attendance.map((entry, i) => (
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

      <div className="row g-3 mb-3">
        <div className="col-12 col-xl-6">
          <ChartCard title="Monthly Payroll" subtitle={`Gross salary per month (${year})`}>
            {d.payroll && d.payroll.some((p) => Number(p.amount) > 0) ? (
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={d.payroll} margin={{ top: 5, right: 10, left: -10, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                  <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                  <YAxis tick={{ fontSize: 11 }} tickFormatter={(v) => (v >= 1000 ? `${v / 1000}k` : v)} />
                  <Tooltip formatter={(v) => formatMoney(v)} />
                  <Line type="monotone" dataKey="amount" name="Gross Salary" stroke="#2563eb" strokeWidth={2} dot={{ r: 3 }} />
                </LineChart>
              </ResponsiveContainer>
            ) : (
              <EmptyState message="No payroll data" icon="bi-cash-stack" />
            )}
          </ChartCard>
        </div>
        <div className="col-12 col-xl-6">
          <ChartCard title="Leave Overview" subtitle="Leave requests by status">
            {d.leave && d.leave.length ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={d.leave}
                    dataKey="count"
                    nameKey="name"
                    outerRadius={90}
                    label={(p) => `${p.name}`}
                  >
                    {d.leave.map((entry, i) => (
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

      <div className="row g-3">
        <div className="col-12 col-xl-6">
          <ChartCard title="Employee Status" subtitle="Active vs inactive employees">
            {d.employeeStatus && d.employeeStatus.length ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={d.employeeStatus} dataKey="count" nameKey="name" outerRadius={90} label={(p) => `${p.name} (${p.count})`}>
                    {d.employeeStatus.map((entry, i) => (
                      <Cell key={i} fill={entry.name === 'ACTIVE' ? '#10b981' : '#64748b'} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <EmptyState message="No data" icon="bi-person" />
            )}
          </ChartCard>
        </div>
        <div className="col-12 col-xl-6">
          <ChartCard title="Performance Overview" subtitle="Reviews by rating">
            {ratingData.length ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={ratingData} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                  <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                  <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                  <Tooltip />
                  <Bar dataKey="count" name="Reviews" radius={[4, 4, 0, 0]}>
                    {ratingData.map((entry, i) => (
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
    </div>
  );
}