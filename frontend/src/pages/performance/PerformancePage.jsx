import { useEffect, useState, useCallback } from 'react';
import performanceService from '../../services/performance.service';
import employeeService from '../../services/employee.service';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { getErrorMessage, getFieldErrors } from '../../services/api';
import { formatDate, today } from '../../utils/formatters';
import { RATING_LABELS, DEFAULT_PAGE_SIZE } from '../../utils/constants';
import PageHeader from '../../components/common/PageHeader';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorAlert from '../../components/common/ErrorAlert';
import EmptyState from '../../components/common/EmptyState';
import Pagination from '../../components/common/Pagination';
import RatingBadge from '../../components/common/RatingBadge';

const EMPTY_FORM = {
  employeeId: '',
  reviewDate: today(),
  rating: '3',
  strengths: '',
  weaknesses: '',
  comments: '',
};

export default function PerformancePage() {
  const { user } = useAuth();
  const { success, error: toastError } = useToast();
  const isAdmin = user.role === 'ADMIN';

  const [filters, setFilters] = useState({ employeeId: '', rating: '' });
  const [page, setPage] = useState(0);
  const [size] = useState(DEFAULT_PAGE_SIZE);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');

  const [employees, setEmployees] = useState([]);

  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [formErrors, setFormErrors] = useState({});
  const [saving, setSaving] = useState(false);

  const [confirmDelete, setConfirmDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setErr('');
    try {
      const result = await performanceService.getAll({
        employeeId: filters.employeeId || undefined,
        rating: filters.rating || undefined,
        page,
        size,
        sort: 'reviewDate,desc',
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

  const openEdit = (r) => {
    setEditing(r);
    setForm({
      employeeId: r.employeeId,
      reviewDate: r.reviewDate,
      rating: String(r.rating),
      strengths: r.strengths || '',
      weaknesses: r.weaknesses || '',
      comments: r.comments || '',
    });
    setFormErrors({});
    setShowModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    const errors = {};
    if (!form.employeeId) errors.employeeId = 'Employee is required';
    if (!form.reviewDate) errors.reviewDate = 'Review date is required';
    setFormErrors(errors);
    if (Object.keys(errors).length) return;

    setSaving(true);
    try {
      const payload = {
        employeeId: Number(form.employeeId),
        reviewDate: form.reviewDate,
        rating: Number(form.rating),
        strengths: form.strengths || null,
        weaknesses: form.weaknesses || null,
        comments: form.comments || null,
      };
      if (editing) {
        await performanceService.update(editing.id, payload);
        success('Review updated');
      } else {
        await performanceService.create(payload);
        success('Review added');
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

  const handleDelete = async () => {
    if (!confirmDelete) return;
    setDeleting(true);
    try {
      await performanceService.remove(confirmDelete.id);
      success('Review deleted');
      setConfirmDelete(null);
      load();
    } catch (err) {
      toastError(getErrorMessage(err));
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div>
      <PageHeader
        title="Performance Reviews"
        subtitle="Track and manage employee performance ratings"
        actions={
          <button type="button" className="btn btn-primary" onClick={openCreate}>
            <i className="bi bi-plus-lg me-1" />
            Add Review
          </button>
        }
      />

      <div className="filter-panel mb-3">
        <div className="row g-2">
          <div className="col-12 col-md-4">
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
          <div className="col-6 col-md-3">
            <select
              className="form-select"
              value={filters.rating}
              onChange={(e) => handleFilterChange('rating', e.target.value)}
            >
              <option value="">All Ratings</option>
              {Object.keys(RATING_LABELS).map((r) => (
                <option key={r} value={r}>
                  {r} - {RATING_LABELS[r]}
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
                <th>Review Date</th>
                <th>Rating</th>
                <th>Reviewed By</th>
                <th>Comments</th>
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="6">
                    <LoadingSpinner />
                  </td>
                </tr>
              ) : data && data.content.length ? (
                data.content.map((r) => (
                  <tr key={r.id}>
                    <td>
                      <div className="fw-semibold">{r.employeeName}</div>
                      <div className="small text-muted">{r.employeeCode}</div>
                    </td>
                    <td>{formatDate(r.reviewDate)}</td>
                    <td>
                      <RatingBadge rating={r.rating} />
                    </td>
                    <td>{r.reviewerName}</td>
                    <td className="text-truncate" style={{ maxWidth: 200 }}>
                      {r.comments || '—'}
                    </td>
                    <td>
                      <div className="d-flex justify-content-end gap-1 table-actions">
                        <button type="button" className="btn btn-sm btn-outline-primary" title="Edit" onClick={() => openEdit(r)}>
                          <i className="bi bi-pencil" />
                        </button>
                        {isAdmin && (
                          <button type="button" className="btn btn-sm btn-outline-danger" title="Delete" onClick={() => setConfirmDelete(r)}>
                            <i className="bi bi-trash" />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="6">
                    {err ? <ErrorAlert message={err} /> : <EmptyState message="No performance reviews found" icon="bi-clipboard-data" />}
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
                  <h5 className="modal-title">{editing ? 'Edit Review' : 'Add Review'}</h5>
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
                    <div className="col-6 col-md-3">
                      <label className="form-label small">Review Date *</label>
                      <input
                        type="date"
                        className={`form-control ${formErrors.reviewDate ? 'is-invalid' : ''}`}
                        name="reviewDate"
                        value={form.reviewDate}
                        onChange={(e) => setForm({ ...form, reviewDate: e.target.value })}
                      />
                      {formErrors.reviewDate && <div className="invalid-feedback">{formErrors.reviewDate}</div>}
                    </div>
                    <div className="col-6 col-md-3">
                      <label className="form-label small">Rating *</label>
                      <select
                        className="form-select"
                        name="rating"
                        value={form.rating}
                        onChange={(e) => setForm({ ...form, rating: e.target.value })}
                      >
                        {Object.keys(RATING_LABELS).map((r) => (
                          <option key={r} value={r}>
                            {r} - {RATING_LABELS[r]}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                  <div className="mb-3">
                    <label className="form-label small">Strengths</label>
                    <textarea
                      className="form-control"
                      rows={2}
                      name="strengths"
                      value={form.strengths}
                      onChange={(e) => setForm({ ...form, strengths: e.target.value })}
                      maxLength={2000}
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label small">Weaknesses / Areas to Improve</label>
                    <textarea
                      className="form-control"
                      rows={2}
                      name="weaknesses"
                      value={form.weaknesses}
                      onChange={(e) => setForm({ ...form, weaknesses: e.target.value })}
                      maxLength={2000}
                    />
                  </div>
                  <div>
                    <label className="form-label small">Comments</label>
                    <textarea
                      className="form-control"
                      rows={2}
                      name="comments"
                      value={form.comments}
                      onChange={(e) => setForm({ ...form, comments: e.target.value })}
                      maxLength={2000}
                    />
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-light" onClick={() => setShowModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={saving}>
                    {saving ? 'Saving...' : editing ? 'Update' : 'Save'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {confirmDelete && (
        <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true">
          <div className="modal-dialog modal-dialog-centered modal-sm">
            <div className="modal-content">
              <div className="modal-body text-center p-4">
                <h5 className="mb-2">Delete Review?</h5>
                <p className="text-muted mb-0">
                  Review for <strong>{confirmDelete.employeeName}</strong> on {formatDate(confirmDelete.reviewDate)} will be
                  permanently removed.
                </p>
              </div>
              <div className="modal-footer justify-content-center border-0 pt-0">
                <button type="button" className="btn btn-light" onClick={() => setConfirmDelete(null)}>
                  Cancel
                </button>
                <button type="button" className="btn btn-danger" onClick={handleDelete} disabled={deleting}>
                  {deleting ? 'Deleting...' : 'Delete'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}