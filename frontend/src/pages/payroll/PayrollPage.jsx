import { useEffect, useState, useCallback } from 'react';
import payrollService from '../../services/payroll.service';
import employeeService from '../../services/employee.service';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { getErrorMessage, getFieldErrors } from '../../services/api';
import { formatMoney, formatDate, currentMonth, currentYear, monthName } from '../../utils/formatters';
import { PAYMENT_STATUSES, DEFAULT_PAGE_SIZE } from '../../utils/constants';
import PageHeader from '../../components/common/PageHeader';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorAlert from '../../components/common/ErrorAlert';
import EmptyState from '../../components/common/EmptyState';
import Pagination from '../../components/common/Pagination';
import StatusBadge from '../../components/common/StatusBadge';

const EMPTY_FORM = {
  employeeId: '',
  month: currentMonth(),
  year: currentYear(),
  basicSalary: '',
  houseAllowance: '',
  transportAllowance: '',
  medicalAllowance: '',
  overtime: '',
  bonus: '',
  tax: '',
  deduction: '',
  paymentStatus: 'PENDING',
};

function MoneyInput({ label, name, value, onChange }) {
  return (
    <div className="col-6 col-md-4">
      <label className="form-label small">{label}</label>
      <input
        type="number"
        step="0.01"
        min="0"
        className="form-control"
        name={name}
        value={value}
        onChange={onChange}
        placeholder="0.00"
      />
    </div>
  );
}

export default function PayrollPage() {
  const { user } = useAuth();
  const { success, error: toastError } = useToast();
  const isAdmin = user.role === 'ADMIN';

  const [filters, setFilters] = useState({ employeeId: '', month: '', year: '', paymentStatus: '' });
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(DEFAULT_PAGE_SIZE);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');

  const [employees, setEmployees] = useState([]);

  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [formErrors, setFormErrors] = useState({});
  const [saving, setSaving] = useState(false);

  const [payslip, setPayslip] = useState(null);
  const [payslipLoading, setPayslipLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setErr('');
    try {
      const result = await payrollService.getAll({
        employeeId: filters.employeeId || undefined,
        month: filters.month || undefined,
        year: filters.year || undefined,
        paymentStatus: filters.paymentStatus || undefined,
        page,
        size,
        sort: 'year,desc,month,desc',
      });
      setData(result);
    } catch (e) {
      setErr(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [filters, page, size]);

  useEffect(() => {
    employeeService.getAll({ page: 0, size: 200, sort: 'firstName,asc' })
      .then((r) => setEmployees(r.content || []))
      .catch(() => setEmployees([]));
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setPage(0);
  };

  const openCreate = () => {
    setEditing(null);
    setForm(EMPTY_FORM);
    setFormErrors({});
    setShowModal(true);
  };

  const openEdit = (p) => {
    setEditing(p);
    setForm({
      employeeId: p.employeeId,
      month: p.month,
      year: p.year,
      basicSalary: p.basicSalary,
      houseAllowance: p.houseAllowance,
      transportAllowance: p.transportAllowance,
      medicalAllowance: p.medicalAllowance,
      overtime: p.overtime,
      bonus: p.bonus,
      tax: p.tax,
      deduction: p.deduction,
      paymentStatus: p.paymentStatus,
    });
    setFormErrors({});
    setShowModal(true);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    const errors = {};
    if (!form.employeeId) errors.employeeId = 'Employee is required';
    if (!form.month || form.month < 1 || form.month > 12) errors.month = 'Month must be between 1 and 12';
    if (!form.year || form.year < 2000) errors.year = 'Year must be 2000 or later';
    setFormErrors(errors);
    if (Object.keys(errors).length) return;

    const toNum = (v) => (v === '' ? null : Number(v));
    const payload = {
      employeeId: Number(form.employeeId),
      month: Number(form.month),
      year: Number(form.year),
      basicSalary: toNum(form.basicSalary),
      houseAllowance: toNum(form.houseAllowance),
      transportAllowance: toNum(form.transportAllowance),
      medicalAllowance: toNum(form.medicalAllowance),
      overtime: toNum(form.overtime),
      bonus: toNum(form.bonus),
      tax: toNum(form.tax),
      deduction: toNum(form.deduction),
      paymentStatus: form.paymentStatus,
    };

    setSaving(true);
    try {
      if (editing) {
        await payrollService.update(editing.id, payload);
        success('Payroll updated');
      } else {
        await payrollService.create(payload);
        success('Payroll generated');
      }
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

  const handleMarkPaid = async (p) => {
    try {
      const updated = await payrollService.markPaid(p.id);
      success(`Payroll for ${updated.employeeName} marked as PAID`);
      load();
    } catch (e) {
      toastError(getErrorMessage(e));
    }
  };

  const openPayslip = async (p) => {
    setPayslip(null);
    setPayslipLoading(true);
    try {
      const slip = await payrollService.getPayslip(p.id);
      setPayslip(slip);
    } catch (e) {
      toastError(getErrorMessage(e));
    } finally {
      setPayslipLoading(false);
    }
  };

  return (
    <div>
      <PageHeader
        title="Payroll"
        subtitle="Generate and manage employee salaries"
        actions={
          <button type="button" className="btn btn-primary" onClick={openCreate}>
            <i className="bi bi-plus-lg me-1" />
            Generate Payroll
          </button>
        }
      />

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
              value={filters.month}
              onChange={(e) => handleFilterChange('month', e.target.value)}
            >
              <option value="">All Months</option>
              {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
                <option key={m} value={m}>
                  {monthName(m)}
                </option>
              ))}
            </select>
          </div>
          <div className="col-6 col-md-2">
            <input
              type="number"
              className="form-control"
              placeholder="Year"
              value={filters.year}
              min={2000}
              onChange={(e) => handleFilterChange('year', e.target.value)}
            />
          </div>
          <div className="col-6 col-md-2">
            <select
              className="form-select"
              value={filters.paymentStatus}
              onChange={(e) => handleFilterChange('paymentStatus', e.target.value)}
            >
              <option value="">All Status</option>
              {PAYMENT_STATUSES.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead className="bg-light">
              <tr>
                <th>Employee</th>
                <th>Period</th>
                <th className="text-end">Basic</th>
                <th className="text-end">Gross</th>
                <th className="text-end">Net</th>
                <th>Status</th>
                <th>Paid On</th>
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="8">
                    <LoadingSpinner />
                  </td>
                </tr>
              ) : data && data.content.length ? (
                data.content.map((p) => (
                  <tr key={p.id}>
                    <td>
                      <div className="fw-semibold">{p.employeeName}</div>
                      <div className="small text-muted">{p.employeeCode}</div>
                    </td>
                    <td>
                      {monthName(p.month)} {p.year}
                    </td>
                    <td className="text-end">{formatMoney(p.basicSalary)}</td>
                    <td className="text-end">{formatMoney(p.grossSalary)}</td>
                    <td className="text-end fw-semibold">{formatMoney(p.netSalary)}</td>
                    <td>
                      <StatusBadge value={p.paymentStatus} />
                    </td>
                    <td>{formatDate(p.paymentDate)}</td>
                    <td>
                      <div className="d-flex justify-content-end gap-1 table-actions">
                        <button type="button" className="btn btn-sm btn-outline-secondary" title="Payslip" onClick={() => openPayslip(p)}>
                          <i className="bi bi-file-earmark-text" />
                        </button>
                        <button type="button" className="btn btn-sm btn-outline-primary" title="Edit" onClick={() => openEdit(p)}>
                          <i className="bi bi-pencil" />
                        </button>
                        {isAdmin && (
                          <button
                            type="button"
                            className="btn btn-sm btn-outline-success"
                            title="Mark as Paid"
                            onClick={() => handleMarkPaid(p)}
                            disabled={p.paymentStatus === 'PAID'}
                          >
                            <i className="bi bi-cash-coin" />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="8">
                    {err ? <ErrorAlert message={err} /> : <EmptyState message="No payroll records found" icon="bi-cash-stack" />}
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
          <div className="modal-dialog modal-lg modal-dialog-centered">
            <div className="modal-content">
              <form onSubmit={handleSave} noValidate>
                <div className="modal-header">
                  <h5 className="modal-title">{editing ? 'Edit Payroll' : 'Generate Payroll'}</h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)} />
                </div>
                <div className="modal-body">
                  <div className="row g-3 mb-3">
                    <div className="col-12 col-md-6">
                      <label className="form-label small">Employee *</label>
                      <select
                        className={`form-select ${formErrors.employeeId ? 'is-invalid' : ''}`}
                        name="employeeId"
                        value={form.employeeId}
                        onChange={handleChange}
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
                    <div className="col-6 col-md-3">
                      <label className="form-label small">Month *</label>
                      <select
                        className={`form-select ${formErrors.month ? 'is-invalid' : ''}`}
                        name="month"
                        value={form.month}
                        onChange={handleChange}
                      >
                        {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
                          <option key={m} value={m}>
                            {monthName(m)}
                          </option>
                        ))}
                      </select>
                      {formErrors.month && <div className="invalid-feedback">{formErrors.month}</div>}
                    </div>
                    <div className="col-6 col-md-3">
                      <label className="form-label small">Year *</label>
                      <input
                        type="number"
                        className={`form-control ${formErrors.year ? 'is-invalid' : ''}`}
                        name="year"
                        value={form.year}
                        min={2000}
                        onChange={handleChange}
                      />
                      {formErrors.year && <div className="invalid-feedback">{formErrors.year}</div>}
                    </div>
                  </div>

                  <label className="form-label small fw-semibold text-primary">Earnings</label>
                  <div className="row g-3 mb-3">
                    <MoneyInput label="Basic Salary" name="basicSalary" value={form.basicSalary} onChange={handleChange} />
                    <MoneyInput label="House Allowance" name="houseAllowance" value={form.houseAllowance} onChange={handleChange} />
                    <MoneyInput label="Transport Allowance" name="transportAllowance" value={form.transportAllowance} onChange={handleChange} />
                    <MoneyInput label="Medical Allowance" name="medicalAllowance" value={form.medicalAllowance} onChange={handleChange} />
                    <MoneyInput label="Overtime" name="overtime" value={form.overtime} onChange={handleChange} />
                    <MoneyInput label="Bonus" name="bonus" value={form.bonus} onChange={handleChange} />
                  </div>

                  <label className="form-label small fw-semibold text-danger">Deductions</label>
                  <div className="row g-3 mb-3">
                    <MoneyInput label="Tax" name="tax" value={form.tax} onChange={handleChange} />
                    <MoneyInput label="Deduction" name="deduction" value={form.deduction} onChange={handleChange} />
                    <div className="col-6 col-md-4">
                      <label className="form-label small">Payment Status</label>
                      <select className="form-select" name="paymentStatus" value={form.paymentStatus} onChange={handleChange}>
                        {PAYMENT_STATUSES.map((s) => (
                          <option key={s} value={s}>
                            {s}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-light" onClick={() => setShowModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={saving}>
                    {saving ? 'Saving...' : editing ? 'Update' : 'Generate'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {payslip && (
        <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true">
          <div className="modal-dialog modal-lg modal-dialog-centered">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Payslip</h5>
                <button type="button" className="btn-close" onClick={() => setPayslip(null)} />
              </div>
              <div className="modal-body">
                <div className="print-area border rounded p-4">
                  <div className="d-flex justify-content-between align-items-center mb-3">
                    <div>
                      <h4 className="fw-bold mb-0">Employee Management System</h4>
                      <small className="text-muted">Salary Payslip</small>
                    </div>
                    <div className="text-end">
                      <StatusBadge value={payslip.paymentStatus} />
                      <div className="small text-muted mt-1">{monthName(payslip.month)} {payslip.year}</div>
                    </div>
                  </div>
                  <div className="row g-2 small mb-3">
                    <div className="col-6">
                      <div className="text-muted">Employee</div>
                      <div className="fw-semibold">{payslip.employeeName}</div>
                    </div>
                    <div className="col-6">
                      <div className="text-muted">Employee Code</div>
                      <div>{payslip.employeeCode}</div>
                    </div>
                    <div className="col-6">
                      <div className="text-muted">Department</div>
                      <div>{payslip.departmentName}</div>
                    </div>
                    <div className="col-6">
                      <div className="text-muted">Designation</div>
                      <div>{payslip.designationName}</div>
                    </div>
                    <div className="col-6">
                      <div className="text-muted">Joining Date</div>
                      <div>{formatDate(payslip.joiningDate)}</div>
                    </div>
                    <div className="col-6">
                      <div className="text-muted">Payment Date</div>
                      <div>{formatDate(payslip.paymentDate)}</div>
                    </div>
                  </div>
                  <div className="row g-2">
                    <div className="col-6">
                      <div className="border rounded p-3">
                        <div className="fw-semibold text-primary mb-2">Earnings</div>
                        <SlipRow label="Basic Salary" value={payslip.basicSalary} />
                        <SlipRow label="House Allowance" value={payslip.houseAllowance} />
                        <SlipRow label="Transport Allowance" value={payslip.transportAllowance} />
                        <SlipRow label="Medical Allowance" value={payslip.medicalAllowance} />
                        <SlipRow label="Overtime" value={payslip.overtime} />
                        <SlipRow label="Bonus" value={payslip.bonus} />
                        <SlipRow label="Gross Salary" value={payslip.grossSalary} bold />
                      </div>
                    </div>
                    <div className="col-6">
                      <div className="border rounded p-3">
                        <div className="fw-semibold text-danger mb-2">Deductions</div>
                        <SlipRow label="Tax" value={payslip.tax} />
                        <SlipRow label="Deduction" value={payslip.deduction} />
                        <div className="mt-3 pt-2 border-top">
                          <SlipRow label="Net Salary" value={payslip.netSalary} bold />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-outline-secondary" onClick={() => window.print()}>
                  <i className="bi bi-printer me-1" />
                  Print
                </button>
                <button type="button" className="btn btn-light" onClick={() => setPayslip(null)}>
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function SlipRow({ label, value, bold }) {
  return (
    <div className={`d-flex justify-content-between ${bold ? 'fw-bold' : ''}`}>
      <span>{label}</span>
      <span>{formatMoney(value)}</span>
    </div>
  );
}