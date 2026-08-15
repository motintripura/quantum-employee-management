import { useEffect, useState, useCallback } from 'react';
import authService from '../../services/auth.service';
import attendanceService from '../../services/attendance.service';
import leaveService, { leaveBalanceService } from '../../services/leave.service';
import payrollService from '../../services/payroll.service';
import performanceService from '../../services/performance.service';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { getErrorMessage } from '../../services/api';
import { formatDate, formatDateTime, formatMoney, monthName, today, currentYear } from '../../utils/formatters';
import { UPLOADS_BASE_URL, ROLE_LABELS } from '../../utils/constants';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorAlert from '../../components/common/ErrorAlert';
import EmptyState from '../../components/common/EmptyState';
import StatusBadge from '../../components/common/StatusBadge';
import RatingBadge from '../../components/common/RatingBadge';

export default function ProfilePage() {
  const { user } = useAuth();
  const { success, error: toastError } = useToast();
  const isEmployee = user.role === 'EMPLOYEE';

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');

  const [tab, setTab] = useState('profile');

  const [attendance, setAttendance] = useState(null);
  const [attendanceLoading, setAttendanceLoading] = useState(false);
  const [checkingIn, setCheckingIn] = useState(false);
  const [checkingOut, setCheckingOut] = useState(false);

  const [leaves, setLeaves] = useState(null);
  const [balances, setBalances] = useState(null);

  const [payroll, setPayroll] = useState(null);
  const [performance, setPerformance] = useState(null);

  const loadProfile = useCallback(async () => {
    try {
      const data = await authService.profile();
      setProfile(data);
    } catch (e) {
      setErr(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadProfile();
  }, [loadProfile]);

  useEffect(() => {
    if (!loading && profile && tab === 'attendance') {
      setAttendanceLoading(true);
      setAttendance(null);
      attendanceService.getEmployeeAttendance(profile.id)
        .then((rows) => setAttendance(rows || []))
        .catch((e) => toastError(getErrorMessage(e)))
        .finally(() => setAttendanceLoading(false));
    }
  }, [tab, loading, profile, toastError]);

  useEffect(() => {
    if (!loading && profile && tab === 'leave') {
      leaveService.getEmployeeLeaves(profile.id)
        .then((rows) => setLeaves(rows || []))
        .catch((e) => toastError(getErrorMessage(e)));
      leaveBalanceService.getBalances(profile.id, currentYear())
        .then((rows) => setBalances(rows || []))
        .catch(() => setBalances([]));
    }
  }, [tab, loading, profile, toastError]);

  useEffect(() => {
    if (!loading && profile && tab === 'payroll') {
      payrollService.getEmployeePayroll(profile.id)
        .then((rows) => setPayroll(rows || []))
        .catch((e) => toastError(getErrorMessage(e)));
    }
  }, [tab, loading, profile, toastError]);

  useEffect(() => {
    if (!loading && profile && tab === 'performance') {
      performanceService.getEmployeePerformance(profile.id)
        .then((rows) => setPerformance(rows || []))
        .catch((e) => toastError(getErrorMessage(e)));
    }
  }, [tab, loading, profile, toastError]);

  const handleCheckIn = async () => {
    setCheckingIn(true);
    try {
      await attendanceService.checkIn();
      success('Checked in successfully');
      const rows = await attendanceService.getEmployeeAttendance(profile.id);
      setAttendance(rows || []);
    } catch (e) {
      toastError(getErrorMessage(e));
    } finally {
      setCheckingIn(false);
    }
  };

  const handleCheckOut = async () => {
    setCheckingOut(true);
    try {
      await attendanceService.checkOut();
      success('Checked out successfully');
      const rows = await attendanceService.getEmployeeAttendance(profile.id);
      setAttendance(rows || []);
    } catch (e) {
      toastError(getErrorMessage(e));
    } finally {
      setCheckingOut(false);
    }
  };

  const todayRecord = (attendance || []).find((a) => String(a.attendanceDate).startsWith(today()));
  const checkedIn = Boolean(todayRecord && todayRecord.checkInTime);
  const checkedOut = Boolean(todayRecord && todayRecord.checkOutTime);

  if (loading) {
    return (
      <div className="d-flex justify-content-center py-5">
        <LoadingSpinner />
      </div>
    );
  }

  if (err || !profile) {
    return <ErrorAlert message={err || 'Unable to load profile'} />;
  }

  const photoUrl = profile.profileImage ? `${UPLOADS_BASE_URL}/${profile.profileImage}` : null;

  const tabs = [
    { key: 'profile', label: 'Profile', icon: 'bi-person' },
    { key: 'attendance', label: 'Attendance', icon: 'bi-calendar-check' },
    { key: 'leave', label: 'Leave', icon: 'bi-calendar2-week' },
    { key: 'payroll', label: 'Payroll', icon: 'bi-cash-stack' },
    { key: 'performance', label: 'Performance', icon: 'bi-clipboard-data' },
  ];

  return (
    <div>
      <div className="card mb-3">
        <div className="card-body d-flex flex-wrap align-items-center gap-3">
          {photoUrl ? (
            <img src={photoUrl} alt={profile.fullName} className="rounded-circle" style={{ width: 72, height: 72, objectFit: 'cover' }} />
          ) : (
            <div className="avatar" style={{ width: 72, height: 72, fontSize: '1.6rem' }}>
              {(profile.firstName?.[0] || '?').toUpperCase()}
            </div>
          )}
          <div className="flex-grow-1">
            <h4 className="mb-1">{profile.fullName}</h4>
            <div className="d-flex flex-wrap gap-3 small text-muted">
              <span>
                <i className="bi bi-person-badge me-1" />
                {profile.employeeCode}
              </span>
              <span>
                <i className="bi bi-building me-1" />
                {profile.departmentName}
              </span>
              <span>
                <i className="bi bi-briefcase me-1" />
                {profile.designationName}
              </span>
              <span>
                <i className="bi bi-shield-lock me-1" />
                {ROLE_LABELS[user.role] || user.role}
              </span>
            </div>
          </div>
          {isEmployee && (
            <div className="d-flex gap-2">
              <button type="button" className="btn btn-outline-success" onClick={handleCheckIn} disabled={checkingIn || checkedIn}>
                <i className="bi bi-box-arrow-in-right me-1" />
                {checkedIn ? 'Checked In' : checkingIn ? 'Checking in...' : 'Check In'}
              </button>
              <button type="button" className="btn btn-outline-danger" onClick={handleCheckOut} disabled={checkingOut || !checkedIn || checkedOut}>
                <i className="bi bi-box-arrow-right me-1" />
                {checkedOut ? 'Checked Out' : checkingOut ? 'Checking out...' : 'Check Out'}
              </button>
            </div>
          )}
        </div>
      </div>

      <ul className="nav nav-tabs mb-3">
        {tabs.map((t) => (
          <li className="nav-item" key={t.key}>
            <button
              type="button"
              className={`nav-link ${tab === t.key ? 'active' : ''}`}
              onClick={() => setTab(t.key)}
            >
              <i className={`bi ${t.icon} me-1`} />
              {t.label}
            </button>
          </li>
        ))}
      </ul>

      {tab === 'profile' && (
        <div className="row g-3">
          <div className="col-12 col-lg-6">
            <div className="card h-100">
              <div className="card-header bg-white fw-semibold">Personal Information</div>
              <div className="card-body">
                <InfoRow label="Full Name" value={profile.fullName} />
                <InfoRow label="Employee ID" value={profile.employeeCode} />
                <InfoRow label="Date of Birth" value={formatDate(profile.dateOfBirth)} />
                <InfoRow label="Gender" value={profile.gender || '—'} />
                <InfoRow label="Address" value={profile.address || '—'} />
                <InfoRow label="Phone" value={profile.phone || '—'} />
                <InfoRow label="Email" value={profile.email || '—'} />
              </div>
            </div>
          </div>
          <div className="col-12 col-lg-6">
            <div className="card h-100">
              <div className="card-header bg-white fw-semibold">Employment Details</div>
              <div className="card-body">
                <InfoRow label="Department" value={profile.departmentName} />
                <InfoRow label="Designation" value={profile.designationName} />
                <InfoRow label="Joining Date" value={formatDate(profile.joiningDate)} />
                <InfoRow label="Salary" value={formatMoney(profile.salary)} />
                <InfoRow
                  label="Status"
                  value={profile.status ? <StatusBadge value={profile.status ? 'ACTIVE' : 'INACTIVE'} /> : '—'}
                />
                <InfoRow label="Account" value={user.username} />
                <InfoRow label="Account Status" value={user.enabled ? 'Enabled' : 'Disabled'} />
              </div>
            </div>
          </div>
        </div>
      )}

      {tab === 'attendance' && (
        <div className="card">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="bg-light">
                <tr>
                  <th>Date</th>
                  <th>Check In</th>
                  <th>Check Out</th>
                  <th className="text-end">Working Hrs</th>
                  <th>Status</th>
                  <th className="text-end">Overtime Hrs</th>
                </tr>
              </thead>
              <tbody>
                {attendanceLoading ? (
                  <tr>
                    <td colSpan="6">
                      <LoadingSpinner />
                    </td>
                  </tr>
                ) : attendance && attendance.length ? (
                  attendance.map((a) => (
                    <tr key={a.id}>
                      <td>{formatDate(a.attendanceDate)}</td>
                      <td>{a.checkInTime ? formatDateTime(a.checkInTime) : '—'}</td>
                      <td>{a.checkOutTime ? formatDateTime(a.checkOutTime) : '—'}</td>
                      <td className="text-end">{a.workingHours ?? '—'}</td>
                      <td>
                        <StatusBadge value={a.status} />
                      </td>
                      <td className="text-end">{a.overtimeHours ?? '—'}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="6">
                      <EmptyState message="No attendance records found" icon="bi-calendar-check" />
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {tab === 'leave' && (
        <div className="row g-3">
          <div className="col-12 col-xl-4">
            <div className="card h-100">
              <div className="card-header bg-white fw-semibold">Leave Balances ({currentYear()})</div>
              <div className="table-responsive">
                <table className="table align-middle mb-0">
                  <thead className="bg-light">
                    <tr>
                      <th>Type</th>
                      <th className="text-end">Allocated</th>
                      <th className="text-end">Used</th>
                      <th className="text-end">Remaining</th>
                    </tr>
                  </thead>
                  <tbody>
                    {balances && balances.length ? (
                      balances.map((b) => (
                        <tr key={b.id}>
                          <td>{b.leaveTypeName}</td>
                          <td className="text-end">{b.allocatedDays}</td>
                          <td className="text-end">{b.usedDays}</td>
                          <td className="text-end fw-semibold">{b.remainingDays}</td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="4">
                          <EmptyState message="No balances" icon="bi-calendar2-week" small />
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
          <div className="col-12 col-xl-8">
            <div className="card h-100">
              <div className="card-header bg-white fw-semibold">Leave Requests</div>
              <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                  <thead className="bg-light">
                    <tr>
                      <th>Type</th>
                      <th>Start</th>
                      <th>End</th>
                      <th className="text-end">Days</th>
                      <th>Reason</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {leaves && leaves.length ? (
                      leaves.map((l) => (
                        <tr key={l.id}>
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
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="6">
                          <EmptyState message="No leave requests found" icon="bi-calendar2-week" />
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      )}

      {tab === 'payroll' && (
        <div className="card">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="bg-light">
                <tr>
                  <th>Period</th>
                  <th className="text-end">Basic</th>
                  <th className="text-end">Gross</th>
                  <th className="text-end">Net</th>
                  <th>Status</th>
                  <th>Paid On</th>
                </tr>
              </thead>
              <tbody>
                {payroll && payroll.length ? (
                  payroll.map((p) => (
                    <tr key={p.id}>
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
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="6">
                      <EmptyState message="No payroll records found" icon="bi-cash-stack" />
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {tab === 'performance' && (
        <div className="card">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="bg-light">
                <tr>
                  <th>Review Date</th>
                  <th>Rating</th>
                  <th>Reviewed By</th>
                  <th>Strengths</th>
                  <th>Comments</th>
                </tr>
              </thead>
              <tbody>
                {performance && performance.length ? (
                  performance.map((r) => (
                    <tr key={r.id}>
                      <td>{formatDate(r.reviewDate)}</td>
                      <td>
                        <RatingBadge rating={r.rating} />
                      </td>
                      <td>{r.reviewerName}</td>
                      <td className="text-truncate" style={{ maxWidth: 180 }}>
                        {r.strengths || '—'}
                      </td>
                      <td className="text-truncate" style={{ maxWidth: 200 }}>
                        {r.comments || '—'}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="5">
                      <EmptyState message="No performance reviews found" icon="bi-clipboard-data" />
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

function InfoRow({ label, value }) {
  return (
    <div className="d-flex justify-content-between py-2 border-bottom">
      <span className="text-muted">{label}</span>
      <span className="fw-semibold text-end">{value}</span>
    </div>
  );
}