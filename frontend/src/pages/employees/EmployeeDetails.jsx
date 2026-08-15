import { useEffect, useState, useCallback } from 'react';
import { useParams, Link, useNavigate, useLocation } from 'react-router-dom';
import employeeService from '../../services/employee.service';
import attendanceService from '../../services/attendance.service';
import leaveService from '../../services/leave.service';
import payrollService from '../../services/payroll.service';
import performanceService from '../../services/performance.service';
import authService from '../../services/auth.service';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { getErrorMessage, getFieldErrors } from '../../services/api';
import { formatMoney, formatDate, formatTime, fullName } from '../../utils/formatters';
import { UPLOADS_BASE_URL, RATING_LABELS, ROLE_LABELS } from '../../utils/constants';
import PageHeader from '../../components/common/PageHeader';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorAlert from '../../components/common/ErrorAlert';
import EmptyState from '../../components/common/EmptyState';
import StatusBadge from '../../components/common/StatusBadge';
import ConfirmDialog from '../../components/common/ConfirmDialog';

function Stars({ rating }) {
  return (
    <span className="text-warning">
      {[1, 2, 3, 4, 5].map((n) => (
        <i key={n} className={`bi ${n <= rating ? 'bi-star-fill' : 'bi-star'}`} />
      ))}
      {rating && <small className="text-muted ms-1">{RATING_LABELS[rating]}</small>}
    </span>
  );
}

export default function EmployeeDetails() {
  const { id } = useParams();
  const { user } = useAuth();
  const { success, error: toastError } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const isEditor = user.role === 'ADMIN' || user.role === 'HR';
  const canDelete = user.role === 'ADMIN';

  const [employee, setEmployee] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');
  const [tab, setTab] = useState('attendance');
  const [tabsData, setTabsData] = useState({ attendance: null, leave: null, payroll: null, performance: null });
  const [uploading, setUploading] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [showRegister, setShowRegister] = useState(false);
  const [regForm, setRegForm] = useState({ username: '', email: '', password: '', role: 'EMPLOYEE' });
  const [regErrors, setRegErrors] = useState({});
  const [regSubmitting, setRegSubmitting] = useState(false);

  const tempPassword = location.state?.tempPassword;

  const loadEmployee = useCallback(async () => {
    setLoading(true);
    setErr('');
    try {
      const emp = await employeeService.getById(id);
      setEmployee(emp);
    } catch (e) {
      setErr(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadEmployee();
  }, [loadEmployee]);

  useEffect(() => {
    if (!employee || !isEditor) return;
    let active = true;
    setTabsData((prev) => ({ ...prev, [tab]: null }));
    const loaders = {
      attendance: () => attendanceService.getEmployeeAttendance(employee.id),
      leave: () => leaveService.getEmployeeLeaves(employee.id),
      payroll: () => payrollService.getEmployeePayroll(employee.id),
      performance: () => performanceService.getEmployeePerformance(employee.id),
    };
    loaders[tab]()
      .then((data) => {
        if (active) setTabsData((prev) => ({ ...prev, [tab]: data }));
      })
      .catch((e) => toastError(getErrorMessage(e)))
      .finally(() => {
        if (active) setTabsData((prev) => ({ ...prev, [tab]: prev[tab] || [] }));
      });
    return () => {
      active = false;
    };
  }, [tab, employee, isEditor, toastError]);

  const handleUpload = async (file) => {
    if (!file) return;
    if (!/\.(jpg|jpeg|png|gif|webp)$/i.test(file.name)) {
      toastError('Only image files (jpg, jpeg, png, gif, webp) are allowed');
      return;
    }
    setUploading(true);
    try {
      const updated = await employeeService.uploadImage(id, file);
      setEmployee(updated);
      success('Profile image updated');
    } catch (e) {
      toastError(getErrorMessage(e));
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await employeeService.remove(id);
      success('Employee deleted');
      navigate('/employees');
    } catch (e) {
      toastError(getErrorMessage(e));
      setDeleting(false);
    }
  };

  const openRegister = () => {
    setRegForm({
      username: employee.employeeCode || '',
      email: employee.email || '',
      password: tempPassword || 'Default@123',
      role: employee.role || 'EMPLOYEE',
    });
    setRegErrors({});
    setShowRegister(true);
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    const errors = {};
    if (regForm.username.trim().length < 3) errors.username = 'Username must be at least 3 characters';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(regForm.email.trim())) errors.email = 'Enter a valid email';
    if (regForm.password.length < 8) errors.password = 'Password must be at least 8 characters';
    setRegErrors(errors);
    if (Object.keys(errors).length) return;

    setRegSubmitting(true);
    try {
      const registered = await authService.register({
        username: regForm.username.trim(),
        email: regForm.email.trim(),
        password: regForm.password,
        role: regForm.role || 'EMPLOYEE',
        employeeId: Number(id),
      });
      success(`Login account ${registered.username} saved`);
      setShowRegister(false);
    } catch (err) {
      const fe = getFieldErrors(err);
      if (Object.keys(fe).length) setRegErrors(fe);
      else toastError(getErrorMessage(err));
    } finally {
      setRegSubmitting(false);
    }
  };

  if (loading) return <LoadingSpinner full text="Loading employee..." />;
  if (err) return <ErrorAlert message={err} />;
  if (!employee) return null;

  const photoSrc = employee.profileImage
    ? `${UPLOADS_BASE_URL}/${employee.profileImage.replace(/^\/+/, '')}`
    : undefined;

  const tabItems = [
    { key: 'attendance', label: 'Attendance', icon: 'bi-calendar-check' },
    { key: 'leave', label: 'Leave', icon: 'bi-calendar2-week' },
    { key: 'payroll', label: 'Payroll', icon: 'bi-cash-stack' },
    { key: 'performance', label: 'Performance', icon: 'bi-graph-up' },
  ];

  return (
    <div>
      <PageHeader
        title={fullName(employee)}
        subtitle={`${employee.employeeCode} &middot; ${employee.departmentName || '—'} &middot; ${employee.designationName || '—'}`}
        actions={
          <>
            {isEditor && (
              <>
                <button type="button" className="btn btn-light" onClick={openRegister}>
                  <i className="bi bi-person-plus me-1" />
                  Create Login
                </button>
                <Link to={`/employees/${employee.id}/edit`} className="btn btn-primary">
                  <i className="bi bi-pencil me-1" />
                  Edit
                </Link>
              </>
            )}
            {canDelete && (
              <button type="button" className="btn btn-outline-danger" onClick={() => setDeleteTarget(true)}>
                <i className="bi bi-trash me-1" />
                Delete
              </button>
            )}
          </>
        }
      />

      {tempPassword && !employee.email && (
        <div className="alert alert-success">
          <i className="bi bi-check-circle-fill me-2" />
          Employee created. Temporary password: <code>{tempPassword}</code> — use "Create Login" to set up the account.
        </div>
      )}

      <div className="row g-3 mb-3">
        <div className="col-lg-4">
          <div className="card h-100">
            <div className="card-body text-center">
              <div className="position-relative d-inline-block">
                {photoSrc ? (
                  <img
                    src={photoSrc}
                    alt={fullName(employee)}
                    className="rounded-circle"
                    style={{ width: 120, height: 120, objectFit: 'cover', border: '3px solid #e2e8f0' }}
                    onError={(e) => {
                      e.currentTarget.style.display = 'none';
                    }}
                  />
                ) : (
                  <span className="avatar rounded-circle" style={{ width: 120, height: 120, fontSize: '2.5rem' }}>
                    {employee.firstName ? employee.firstName.charAt(0).toUpperCase() : 'U'}
                  </span>
                )}
                {isEditor && (
                  <label
                    className="btn btn-sm btn-primary rounded-circle position-absolute bottom-0 end-0"
                    style={{ cursor: 'pointer' }}
                    htmlFor="profileImageUpload"
                    title="Upload photo"
                  >
                    {uploading ? <i className="bi bi-arrow-repeat" /> : <i className="bi bi-camera" />}
                    <input
                      id="profileImageUpload"
                      type="file"
                      accept="image/*"
                      hidden
                      disabled={uploading}
                      onChange={(e) => {
                        handleUpload(e.target.files[0]);
                        e.target.value = '';
                      }}
                    />
                  </label>
                )}
              </div>
              <h5 className="mt-3 mb-1">{fullName(employee)}</h5>
              <div className="text-muted">{employee.designationName}</div>
              <div className="mt-2">
                <StatusBadge value={employee.status ? 'ACTIVE' : 'INACTIVE'} />
              </div>
            </div>
          </div>
        </div>

        <div className="col-lg-8">
          <div className="card h-100">
            <div className="card-body">
              <h6 className="card-title fw-semibold mb-3">Profile Information</h6>
              <div className="row g-2 small">
                <InfoItem label="Employee Code" value={employee.employeeCode} />
                <InfoItem label="Email" value={employee.email} />
                <InfoItem label="Phone" value={employee.phone} />
                <InfoItem label="Department" value={employee.departmentName} />
                <InfoItem label="Designation" value={employee.designationName} />
                <InfoItem label="Login Role" value={employee.role ? ROLE_LABELS[employee.role] : 'Employee'} />
                <InfoItem label="Gender" value={employee.gender ? employee.gender.toLowerCase() : null} />
                <InfoItem label="Date of Birth" value={formatDate(employee.dateOfBirth)} />
                <InfoItem label="Joining Date" value={formatDate(employee.joiningDate)} />
                <InfoItem label="Salary" value={formatMoney(employee.salary)} />
                <InfoItem label="Address" value={employee.address} />
              </div>
            </div>
          </div>
        </div>
      </div>

      {isEditor && (
        <div className="card">
          <ul className="nav nav-tabs card-header-tabs nav-tabs-scroll px-3 pt-2">
            {tabItems.map((item) => (
              <li className="nav-item" key={item.key}>
                <button
                  type="button"
                  className={`nav-link ${tab === item.key ? 'active' : ''}`}
                  onClick={() => setTab(item.key)}
                >
                  <i className={`bi ${item.icon} me-1`} />
                  {item.label}
                </button>
              </li>
            ))}
          </ul>
          <div className="card-body">
            {tab === 'attendance' && (
              <TabTable
                loading={tabsData.attendance === null}
                empty={<EmptyState message="No attendance records" icon="bi-calendar-check" />}
                head={
                  <tr>
                    <th>Date</th>
                    <th>Check In</th>
                    <th>Check Out</th>
                    <th className="text-end">Hours</th>
                    <th className="text-end">Overtime</th>
                    <th>Status</th>
                  </tr>
                }
                rows={(tabsData.attendance || []).map((a) => (
                  <tr key={a.id}>
                    <td>{formatDate(a.attendanceDate)}</td>
                    <td>{formatTime(a.checkIn)}</td>
                    <td>{formatTime(a.checkOut)}</td>
                    <td className="text-end">{a.workingHours}</td>
                    <td className="text-end">{a.overtime}</td>
                    <td>
                      <StatusBadge value={a.status} />
                    </td>
                  </tr>
                ))}
              />
            )}
            {tab === 'leave' && (
              <TabTable
                loading={tabsData.leave === null}
                empty={<EmptyState message="No leave requests" icon="bi-calendar2-week" />}
                head={
                  <tr>
                    <th>Type</th>
                    <th>Start</th>
                    <th>End</th>
                    <th className="text-end">Days</th>
                    <th>Status</th>
                  </tr>
                }
                rows={(tabsData.leave || []).map((l) => (
                  <tr key={l.id}>
                    <td>{l.leaveTypeName}</td>
                    <td>{formatDate(l.startDate)}</td>
                    <td>{formatDate(l.endDate)}</td>
                    <td className="text-end">{l.totalDays}</td>
                    <td>
                      <StatusBadge value={l.status} />
                    </td>
                  </tr>
                ))}
              />
            )}
            {tab === 'payroll' && (
              <TabTable
                loading={tabsData.payroll === null}
                empty={<EmptyState message="No payroll records" icon="bi-cash-stack" />}
                head={
                  <tr>
                    <th>Period</th>
                    <th className="text-end">Gross</th>
                    <th className="text-end">Net</th>
                    <th>Status</th>
                    <th>Paid On</th>
                  </tr>
                }
                rows={(tabsData.payroll || []).map((p) => (
                  <tr key={p.id}>
                    <td>
                      {new Date(p.year, p.month - 1, 1).toLocaleString('en-US', { month: 'long' })} {p.year}
                    </td>
                    <td className="text-end">{formatMoney(p.grossSalary)}</td>
                    <td className="text-end">{formatMoney(p.netSalary)}</td>
                    <td>
                      <StatusBadge value={p.paymentStatus} />
                    </td>
                    <td>{formatDate(p.paymentDate)}</td>
                  </tr>
                ))}
              />
            )}
            {tab === 'performance' && (
              <TabTable
                loading={tabsData.performance === null}
                empty={<EmptyState message="No performance reviews" icon="bi-graph-up" />}
                head={
                  <tr>
                    <th>Review Date</th>
                    <th>Reviewer</th>
                    <th>Rating</th>
                    <th>Comments</th>
                  </tr>
                }
                rows={(tabsData.performance || []).map((r) => (
                  <tr key={r.id}>
                    <td>{formatDate(r.reviewDate)}</td>
                    <td>{r.reviewerUsername || '—'}</td>
                    <td>
                      <Stars rating={r.rating} />
                    </td>
                    <td className="text-truncate" style={{ maxWidth: 260 }}>
                      {r.comments || r.strengths || '—'}
                    </td>
                  </tr>
                ))}
              />
            )}
          </div>
        </div>
      )}

      <ConfirmDialog
        show={deleteTarget}
        title="Delete Employee"
        message={`Are you sure you want to delete ${fullName(employee)}? This action cannot be undone.`}
        confirmText={deleting ? 'Deleting...' : 'Delete'}
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(false)}
        disabled={deleting}
      />

      {showRegister && (
        <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true">
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <form onSubmit={handleRegister} noValidate>
                <div className="modal-header">
                  <h5 className="modal-title">Manage Login Account</h5>
                  <button type="button" className="btn-close" onClick={() => setShowRegister(false)} />
                </div>
                <div className="modal-body">
                  <p className="small text-muted mb-3">
                    Configure the login account for <strong>{fullName(employee)}</strong>. This updates their existing
                    account, credentials, and access role.
                  </p>
                  <div className="mb-3">
                    <label className="form-label">Role</label>
                    <select
                      className="form-select"
                      value={regForm.role}
                      onChange={(e) => setRegForm({ ...regForm, role: e.target.value })}
                      disabled={user.role !== 'ADMIN'}
                    >
                      {['EMPLOYEE', 'HR', 'MANAGER'].map((r) => (
                        <option key={r} value={r}>
                          {ROLE_LABELS[r]}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Username</label>
                    <input
                      type="text"
                      className={`form-control ${regErrors.username ? 'is-invalid' : ''}`}
                      value={regForm.username}
                      onChange={(e) => setRegForm({ ...regForm, username: e.target.value })}
                    />
                    {regErrors.username && <div className="invalid-feedback">{regErrors.username}</div>}
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Email</label>
                    <input
                      type="email"
                      className={`form-control ${regErrors.email ? 'is-invalid' : ''}`}
                      value={regForm.email}
                      onChange={(e) => setRegForm({ ...regForm, email: e.target.value })}
                    />
                    {regErrors.email && <div className="invalid-feedback">{regErrors.email}</div>}
                  </div>
                  <div className="mb-1">
                    <label className="form-label">Password</label>
                    <input
                      type="text"
                      className={`form-control ${regErrors.password ? 'is-invalid' : ''}`}
                      value={regForm.password}
                      onChange={(e) => setRegForm({ ...regForm, password: e.target.value })}
                    />
                    {regErrors.password && <div className="invalid-feedback">{regErrors.password}</div>}
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-light" onClick={() => setShowRegister(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={regSubmitting}>
                    {regSubmitting ? 'Saving...' : 'Save Account'}
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

function InfoItem({ label, value }) {
  return (
    <div className="col-md-6">
      <div className="text-muted text-uppercase small fw-semibold">{label}</div>
      <div className="mb-2">{value || '—'}</div>
    </div>
  );
}

function TabTable({ loading, empty, head, rows }) {
  return (
    <div className="table-responsive">
      <table className="table table-hover align-middle mb-0">
        <thead className="bg-light">{head}</thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan="6">
                <LoadingSpinner />
              </td>
            </tr>
          ) : rows.length ? (
            rows
          ) : (
            <tr>
              <td colSpan="6">{empty}</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}