import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import attendanceService from '../../services/attendance.service';
import employeeService from '../../services/employee.service';
import departmentService from '../../services/department.service';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { getErrorMessage, getFieldErrors } from '../../services/api';
import { formatDate, formatTime, currentMonth, currentYear, monthName } from '../../utils/formatters';
import { ATTENDANCE_STATUSES, DEFAULT_PAGE_SIZE } from '../../utils/constants';
import PageHeader from '../../components/common/PageHeader';
import SearchInput from '../../components/common/SearchInput';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorAlert from '../../components/common/ErrorAlert';
import EmptyState from '../../components/common/EmptyState';
import Pagination from '../../components/common/Pagination';
import StatusBadge from '../../components/common/StatusBadge';

function today() {
  return new Date().toISOString().slice(0, 10);
}

export default function AttendancePage() {
  const { user } = useAuth();
  const { success, error: toastError } = useToast();
  const isEditor = user.role === 'ADMIN' || user.role === 'HR';
  const canViewMonthly = user.role !== 'EMPLOYEE';

  const [view, setView] = useState('records');

  const [filters, setFilters] = useState({
    keyword: '',
    employeeId: '',
    departmentId: '',
    status: '',
    fromDate: '',
    toDate: '',
  });
  const [page, setPage] = useState(0);
  const [size] = useState(DEFAULT_PAGE_SIZE);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');

  const [monthly, setMonthly] = useState({ month: currentMonth(), year: currentYear() });
  const [monthlyData, setMonthlyData] = useState(null);
  const [monthlyLoading, setMonthlyLoading] = useState(false);
  const [monthlyErr, setMonthlyErr] = useState('');

  const [employees, setEmployees] = useState([]);
  const [departments, setDepartments] = useState([]);

  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({
    employeeId: '',
    attendanceDate: today(),
    checkIn: '',
    checkOut: '',
    status: 'PRESENT',
    remarks: '',
  });
  const [formErrors, setFormErrors] = useState({});
  const [saving, setSaving] = useState(false);

  const loadOptions = useCallback(async () => {
    try {
      const [empPage, depts] = await Promise.all([
        employeeService.getAll({ page: 0, size: 200, sort: 'firstName,asc' }),
        departmentService.getActive(),
      ]);
      setEmployees(empPage.content || []);
      setDepartments(depts);
    } catch {
      // ignore
    }
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    setErr('');
    try {
      const params = {
        employeeId: filters.employeeId || undefined,
        departmentId: filters.departmentId || undefined,
        status: filters.status || undefined,
        fromDate: filters.fromDate || undefined,
        toDate: filters.toDate || undefined,
        page,
        size,
        sort: 'attendanceDate,desc',
      };
      const result = await attendanceService.getAll(params);
      setData(result);
    } catch (e) {
      setErr(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [filters, page, size]);

  const loadMonthly = useCallback(async () => {
    setMonthlyLoading(true);
    setMonthlyErr('');
    try {
      const result = await attendanceService.monthly(monthly.month, monthly.year);
      setMonthlyData(result);
    } catch (e) {
      setMonthlyErr(getErrorMessage(e));
    } finally {
      setMonthlyLoading(false);
    }
  }, [monthly]);

  useEffect(() => {
    loadOptions();
  }, [loadOptions]);

  useEffect(() => {
    if (view === 'records') load();
  }, [view, load]);

  useEffect(() => {
    if (view === 'monthly' && canViewMonthly) loadMonthly();
  }, [view, canViewMonthly, loadMonthly]);

  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setPage(0);
  };

  const openModal = () => {
    setForm({ employeeId: '', attendanceDate: today(), checkIn: '', checkOut: '', status: 'PRESENT', remarks: '' });
    setFormErrors({});
    setShowModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    const errors = {};
    if (!form.employeeId) errors.employeeId = 'Employee is required';
    if (!form.attendanceDate) errors.attendanceDate = 'Date is required';
    if (form.checkIn && form.checkOut && form.checkOut < form.checkIn)
      errors.checkOut = 'Check-out cannot be before check-in';
    setFormErrors(errors);
    if (Object.keys(errors).length) return;

    setSaving(true);
    try {
      await attendanceService.record({
        employeeId: Number(form.employeeId),
        attendanceDate: form.attendanceDate,
        checkIn: form.checkIn || null,
        checkOut: form.checkOut || null,
        status: form.status,
        remarks: form.remarks || null,
      });
      success('Attendance recorded');
      setShowModal(false);
      load();
    } catch (err) {
      const fe = getFieldErrors(err);
      if (Object.keys(fe).length) setFormErrors(fe);
      else toastError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <PageHeader
        title="Attendance"
        subtitle="Track attendance and working hours"
        actions={
          <div className="d-flex gap-2">
            <Link to="/reports?type=attendance" className="btn btn-outline-secondary btn-sm">
              <i className="bi bi-bar-chart me-1" />
              Report
            </Link>
            <div className="btn-group">
              <button
                type="button"
                className={`btn ${view === 'records' ? 'btn-primary' : 'btn-light'}`}
                onClick={() => setView('records')}
              >
                Records
              </button>
              {canViewMonthly && (
                <button
                  type="button"
                  className={`btn ${view === 'monthly' ? 'btn-primary' : 'btn-light'}`}
                  onClick={() => setView('monthly')}
                >
                  Monthly Report
                </button>
              )}
            </div>
            {isEditor && (
              <button type="button" className="btn btn-primary" onClick={openModal}>
                <i className="bi bi-plus-lg me-1" />
                Record
              </button>
            )}
          </div>
        }
      />

      {view === 'records' && (
        <>
          <div className="filter-panel mb-3">
            <div className="row g-2">
              <div className="col-12 col-md-3">
                <SearchInput
                  value={filters.keyword}
                  onChange={(v) => handleFilterChange('keyword', v)}
                  placeholder="Search employee..."
                />
              </div>
              <div className="col-6 col-md-2">
                <select
                  className="form-select"
                  value={filters.employeeId}
                  onChange={(e) => handleFilterChange('employeeId', e.target.value)}
                >
                  <option value="">All Employees</option>
                  {employees.map((emp) => (
                    <option key={emp.id} value={emp.id}>
                      {emp.fullName}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-6 col-md-2">
                <select
                  className="form-select"
                  value={filters.departmentId}
                  onChange={(e) => handleFilterChange('departmentId', e.target.value)}
                >
                  <option value="">All Departments</option>
                  {departments.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-6 col-md-2">
                <select
                  className="form-select"
                  value={filters.status}
                  onChange={(e) => handleFilterChange('status', e.target.value)}
                >
                  <option value="">All Status</option>
                  {ATTENDANCE_STATUSES.map((s) => (
                    <option key={s} value={s}>
                      {s}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-6 col-md-3 d-flex gap-2">
                <input
                  type="date"
                  className="form-control"
                  value={filters.fromDate}
                  onChange={(e) => handleFilterChange('fromDate', e.target.value)}
                  title="From"
                />
                <input
                  type="date"
                  className="form-control"
                  value={filters.toDate}
                  onChange={(e) => handleFilterChange('toDate', e.target.value)}
                  title="To"
                />
              </div>
            </div>
          </div>

          <div className="card">
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="bg-light">
                  <tr>
                    <th>Date</th>
                    <th>Employee</th>
                    <th>Department</th>
                    <th>Check In</th>
                    <th>Check Out</th>
                    <th className="text-end">Hours</th>
                    <th className="text-end">Overtime</th>
                    <th>Status</th>
                    <th>Remarks</th>
                  </tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr>
                      <td colSpan="9">
                        <LoadingSpinner />
                      </td>
                    </tr>
                  ) : data && data.content.length ? (
                    data.content.map((a) => (
                      <tr key={a.id}>
                        <td>{formatDate(a.attendanceDate)}</td>
                        <td>
                          <div className="fw-semibold">{a.employeeName}</div>
                          <div className="small text-muted">{a.employeeCode}</div>
                        </td>
                        <td>{a.departmentName}</td>
                        <td>{formatTime(a.checkIn)}</td>
                        <td>{formatTime(a.checkOut)}</td>
                        <td className="text-end">{a.workingHours != null ? Number(a.workingHours).toFixed(2) : '—'}</td>
                        <td className="text-end">{a.overtime != null ? Number(a.overtime).toFixed(2) : '—'}</td>
                        <td>
                          <StatusBadge value={a.status} />
                        </td>
                        <td className="text-truncate" style={{ maxWidth: 180 }}>
                          {a.remarks || '—'}
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="9">
                        {err ? <ErrorAlert message={err} /> : <EmptyState message="No attendance records found" icon="bi-calendar-check" />}
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
            {data && data.content.length > 0 && (
              <div className="card-footer bg-white d-flex flex-wrap justify-content-between align-items-center gap-2">
                <div className="small text-muted">
                  Showing {page * size + 1}&ndash;{Math.min((page + 1) * size, data.totalElements)} of {data.totalElements}
                </div>
                <Pagination page={page} totalPages={data.totalPages} onChange={setPage} />
              </div>
            )}
          </div>
        </>
      )}

      {view === 'monthly' && canViewMonthly && (
        <>
          <div className="filter-panel mb-3">
            <div className="row g-2 align-items-end">
              <div className="col-6 col-md-3">
                <label className="form-label small mb-1">Month</label>
                <select
                  className="form-select"
                  value={monthly.month}
                  onChange={(e) => setMonthly({ ...monthly, month: Number(e.target.value) })}
                >
                  {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
                    <option key={m} value={m}>
                      {monthName(m)}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-6 col-md-3">
                <label className="form-label small mb-1">Year</label>
                <input
                  type="number"
                  className="form-control"
                  value={monthly.year}
                  min={2000}
                  onChange={(e) => setMonthly({ ...monthly, year: Number(e.target.value) || currentYear() })}
                />
              </div>
              <div className="col-12 col-md-3">
                <button type="button" className="btn btn-primary w-100" onClick={loadMonthly}>
                  <i className="bi bi-funnel me-1" />
                  Generate
                </button>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="bg-light">
                  <tr>
                    <th>Employee</th>
                    <th>Department</th>
                    <th className="text-center">Present</th>
                    <th className="text-center">Absent</th>
                    <th className="text-center">Late</th>
                    <th className="text-center">Half Day</th>
                    <th className="text-center">Leave</th>
                    <th className="text-center">Total Days</th>
                    <th className="text-end">Hours</th>
                  </tr>
                </thead>
                <tbody>
                  {monthlyLoading ? (
                    <tr>
                      <td colSpan="9">
                        <LoadingSpinner />
                      </td>
                    </tr>
                  ) : monthlyData && monthlyData.length ? (
                    monthlyData.map((m) => (
                      <tr key={m.employeeId}>
                        <td>
                          <div className="fw-semibold">{m.employeeName}</div>
                          <div className="small text-muted">{m.employeeCode}</div>
                        </td>
                        <td>{m.departmentName}</td>
                        <td className="text-center">{m.presentDays}</td>
                        <td className="text-center">{m.absentDays}</td>
                        <td className="text-center">{m.lateDays}</td>
                        <td className="text-center">{m.halfDays}</td>
                        <td className="text-center">{m.leaveDays}</td>
                        <td className="text-center">{m.totalDays}</td>
                        <td className="text-end">{m.totalWorkingHours != null ? Number(m.totalWorkingHours).toFixed(2) : '—'}</td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="9">
                        {monthlyErr ? <ErrorAlert message={monthlyErr} /> : <EmptyState message="No monthly summary for this period" icon="bi-calendar2-week" />}
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}

      {showModal && (
        <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true">
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <form onSubmit={handleSave} noValidate>
                <div className="modal-header">
                  <h5 className="modal-title">Record Attendance</h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)} />
                </div>
                <div className="modal-body">
                  <div className="mb-3">
                    <label className="form-label">Employee *</label>
                    <select
                      className={`form-select ${formErrors.employeeId ? 'is-invalid' : ''}`}
                      value={form.employeeId}
                      onChange={(e) => setForm({ ...form, employeeId: e.target.value })}
                    >
                      <option value="">Select employee</option>
                      {employees.map((emp) => (
                        <option key={emp.id} value={emp.id}>
                          {emp.fullName} ({emp.employeeCode})
                        </option>
                      ))}
                    </select>
                    {formErrors.employeeId && <div className="invalid-feedback">{formErrors.employeeId}</div>}
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Date *</label>
                    <input
                      type="date"
                      className={`form-control ${formErrors.attendanceDate ? 'is-invalid' : ''}`}
                      value={form.attendanceDate}
                      onChange={(e) => setForm({ ...form, attendanceDate: e.target.value })}
                    />
                    {formErrors.attendanceDate && <div className="invalid-feedback">{formErrors.attendanceDate}</div>}
                  </div>
                  <div className="row g-3 mb-3">
                    <div className="col-6">
                      <label className="form-label">Check In</label>
                      <input
                        type="time"
                        className="form-control"
                        value={form.checkIn}
                        onChange={(e) => setForm({ ...form, checkIn: e.target.value })}
                      />
                    </div>
                    <div className="col-6">
                      <label className="form-label">Check Out</label>
                      <input
                        type="time"
                        className={`form-control ${formErrors.checkOut ? 'is-invalid' : ''}`}
                        value={form.checkOut}
                        onChange={(e) => setForm({ ...form, checkOut: e.target.value })}
                      />
                      {formErrors.checkOut && <div className="invalid-feedback">{formErrors.checkOut}</div>}
                    </div>
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Status *</label>
                    <select
                      className="form-select"
                      value={form.status}
                      onChange={(e) => setForm({ ...form, status: e.target.value })}
                    >
                      {ATTENDANCE_STATUSES.map((s) => (
                        <option key={s} value={s}>
                          {s}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="form-label">Remarks</label>
                    <textarea
                      className="form-control"
                      rows={2}
                      value={form.remarks}
                      onChange={(e) => setForm({ ...form, remarks: e.target.value })}
                    />
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-light" onClick={() => setShowModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={saving}>
                    {saving ? 'Saving...' : 'Save'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}