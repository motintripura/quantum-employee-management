import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import leaveService, { leaveTypeService } from '../../services/leave.service';
import employeeService from '../../services/employee.service';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { getErrorMessage, getFieldErrors } from '../../services/api';
import { formatDate } from '../../utils/formatters';
import { LEAVE_STATUSES, DEFAULT_PAGE_SIZE } from '../../utils/constants';
import PageHeader from '../../components/common/PageHeader';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorAlert from '../../components/common/ErrorAlert';
import EmptyState from '../../components/common/EmptyState';
import Pagination from '../../components/common/Pagination';
import StatusBadge from '../../components/common/StatusBadge';

function today() {
  return new Date().toISOString().slice(0, 10);
}

export default function LeavesPage() {
  const { user } = useAuth();
  const { success, error: toastError } = useToast();
  const isApprover = user.role === 'ADMIN' || user.role === 'HR' || user.role === 'MANAGER';
  const isEmployee = user.role === 'EMPLOYEE';

  const [filters, setFilters] = useState({
    employeeId: '',
    leaveTypeId: '',
    status: '',
    fromDate: '',
    toDate: '',
  });
  const [page, setPage] = useState(0);
  const [size] = useState(DEFAULT_PAGE_SIZE);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');

  const [employees, setEmployees] = useState([]);
  const [leaveTypes, setLeaveTypes] = useState([]);

  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({
    employeeId: '',
    leaveTypeId: '',
    startDate: today(),
    endDate: today(),
    reason: '',
  });
  const [formErrors, setFormErrors] = useState({});
  const [saving, setSaving] = useState(false);

  const loadOptions = useCallback(async () => {
    try {
      const [types, empPage] = await Promise.all([
        leaveTypeService.getActive(),
        isEmployee ? Promise.resolve({ content: [] }) : employeeService.getAll({ page: 0, size: 200, sort: 'firstName,asc' }),
      ]);
      setLeaveTypes(types);
      setEmployees(empPage.content || []);
    } catch {
      // ignore
    }
  }, [isEmployee]);

  const load = useCallback(async () => {
    setLoading(true);
    setErr('');
    try {
      const params = {
        employeeId: filters.employeeId || undefined,
        leaveTypeId: filters.leaveTypeId || undefined,
        status: filters.status || undefined,
        fromDate: filters.fromDate || undefined,
        toDate: filters.toDate || undefined,
        page,
        size,
        sort: 'createdAt,desc',
      };
      const result = await leaveService.getAll(params);
      setData(result);
    } catch (e) {
      setErr(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [filters, page, size]);

  useEffect(() => {
    loadOptions();
  }, [loadOptions]);

  useEffect(() => {
    load();
  }, [load]);

  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setPage(0);
  };

  const openModal = () => {
    setForm({ employeeId: '', leaveTypeId: '', startDate: today(), endDate: today(), reason: '' });
    setFormErrors({});
    setShowModal(true);
  };

  const handleApply = async (e) => {
    e.preventDefault();
    const errors = {};
    if (!isEmployee && !form.employeeId) errors.employeeId = 'Employee is required';
    if (!form.leaveTypeId) errors.leaveTypeId = 'Leave type is required';
    if (!form.startDate) errors.startDate = 'Start date is required';
    if (!form.endDate) errors.endDate = 'End date is required';
    else if (form.endDate < form.startDate) errors.endDate = 'End date cannot be before start date';
    setFormErrors(errors);
    if (Object.keys(errors).length) return;

    setSaving(true);
    try {
      const payload = {
        employeeId: isEmployee ? undefined : Number(form.employeeId),
        leaveTypeId: Number(form.leaveTypeId),
        startDate: form.startDate,
        endDate: form.endDate,
        reason: form.reason || null,
      };
      await leaveService.apply(payload);
      success('Leave request submitted');
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

  const handleAction = async (leave, action) => {
    try {
      if (action === 'approve') {
        await leaveService.approve(leave.id);
        success('Leave request approved');
      } else if (action === 'reject') {
        await leaveService.reject(leave.id);
        success('Leave request rejected');
      } else {
        await leaveService.cancel(leave.id);
        success('Leave request cancelled');
      }
      load();
    } catch (e) {
      toastError(getErrorMessage(e));
    }
  };

  return (
    <div>
      <PageHeader
        title="Leave Management"
        subtitle={isEmployee ? 'My leave requests' : 'Review and manage leave requests'}
        actions={
          <div className="d-flex gap-2">
            <Link to="/reports?type=leave" className="btn btn-outline-secondary btn-sm">
              <i className="bi bi-bar-chart me-1" />
              Report
            </Link>
            <button type="button" className="btn btn-primary" onClick={openModal}>
              <i className="bi bi-plus-lg me-1" />
              Apply Leave
            </button>
          </div>
        }
      />

      {!isEmployee && (
        <div className="filter-panel mb-3">
          <div className="row g-2">
            <div className="col-12 col-md-3">
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
                value={filters.leaveTypeId}
                onChange={(e) => handleFilterChange('leaveTypeId', e.target.value)}
              >
                <option value="">All Types</option>
                {leaveTypes.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.name}
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
                {LEAVE_STATUSES.map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-6 col-md-2">
              <input
                type="date"
                className="form-control"
                value={filters.fromDate}
                onChange={(e) => handleFilterChange('fromDate', e.target.value)}
                title="From"
              />
            </div>
            <div className="col-6 col-md-2">
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
      )}

      <div className="card">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead className="bg-light">
              <tr>
                {!isEmployee && <th>Employee</th>}
                <th>Type</th>
                <th>Start</th>
                <th>End</th>
                <th className="text-end">Days</th>
                <th>Reason</th>
                <th>Status</th>
                <th>Approved By</th>
                <th className="text-end">Actions</th>
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
                data.content.map((l) => (
                  <tr key={l.id}>
                    {!isEmployee && (
                      <td>
                        <div className="fw-semibold">{l.employeeName}</div>
                        <div className="small text-muted">{l.employeeCode}</div>
                      </td>
                    )}
                    <td>{l.leaveTypeName}</td>
                    <td>{formatDate(l.startDate)}</td>
                    <td>{formatDate(l.endDate)}</td>
                    <td className="text-end">{l.totalDays}</td>
                    <td className="text-truncate" style={{ maxWidth: 180 }}>
                      {l.reason || '—'}
                    </td>
                    <td>
                      <StatusBadge value={l.status} />
                    </td>
                    <td>{l.approvedByUsername || '—'}</td>
                    <td>
                      <div className="d-flex justify-content-end gap-1 table-actions">
                        {isApprover && l.status === 'PENDING' && (
                          <>
                            <button
                              type="button"
                              className="btn btn-sm btn-outline-success"
                              title="Approve"
                              onClick={() => handleAction(l, 'approve')}
                            >
                              <i className="bi bi-check-lg" />
                            </button>
                            <button
                              type="button"
                              className="btn btn-sm btn-outline-danger"
                              title="Reject"
                              onClick={() => handleAction(l, 'reject')}
                            >
                              <i className="bi bi-x-lg" />
                            </button>
                          </>
                        )}
                        {!isEmployee && (
                          <button
                            type="button"
                            className="btn btn-sm btn-outline-secondary"
                            title="Cancel"
                            onClick={() => handleAction(l, 'cancel')}
                            disabled={l.status !== 'PENDING'}
                          >
                            <i className="bi bi-x-circle" />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="9">
                    {err ? <ErrorAlert message={err} /> : <EmptyState message="No leave requests found" icon="bi-calendar2-week" />}
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

      {showModal && (
        <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true">
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <form onSubmit={handleApply} noValidate>
                <div className="modal-header">
                  <h5 className="modal-title">Apply for Leave</h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)} />
                </div>
                <div className="modal-body">
                  {!isEmployee && (
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
                  )}
                  <div className="mb-3">
                    <label className="form-label">Leave Type *</label>
                    <select
                      className={`form-select ${formErrors.leaveTypeId ? 'is-invalid' : ''}`}
                      value={form.leaveTypeId}
                      onChange={(e) => setForm({ ...form, leaveTypeId: e.target.value })}
                    >
                      <option value="">Select type</option>
                      {leaveTypes.map((t) => (
                        <option key={t.id} value={t.id}>
                          {t.name} {t.defaultDays ? `(${t.defaultDays} days)` : ''}
                        </option>
                      ))}
                    </select>
                    {formErrors.leaveTypeId && <div className="invalid-feedback">{formErrors.leaveTypeId}</div>}
                  </div>
                  <div className="row g-3 mb-3">
                    <div className="col-6">
                      <label className="form-label">Start Date *</label>
                      <input
                        type="date"
                        className={`form-control ${formErrors.startDate ? 'is-invalid' : ''}`}
                        value={form.startDate}
                        onChange={(e) => setForm({ ...form, startDate: e.target.value })}
                      />
                      {formErrors.startDate && <div className="invalid-feedback">{formErrors.startDate}</div>}
                    </div>
                    <div className="col-6">
                      <label className="form-label">End Date *</label>
                      <input
                        type="date"
                        className={`form-control ${formErrors.endDate ? 'is-invalid' : ''}`}
                        value={form.endDate}
                        onChange={(e) => setForm({ ...form, endDate: e.target.value })}
                      />
                      {formErrors.endDate && <div className="invalid-feedback">{formErrors.endDate}</div>}
                    </div>
                  </div>
                  <div>
                    <label className="form-label">Reason</label>
                    <textarea
                      className="form-control"
                      rows={3}
                      value={form.reason}
                      onChange={(e) => setForm({ ...form, reason: e.target.value })}
                      maxLength={1000}
                      placeholder="Reason for leave (optional)"
                    />
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-light" onClick={() => setShowModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={saving}>
                    {saving ? 'Submitting...' : 'Submit Request'}
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