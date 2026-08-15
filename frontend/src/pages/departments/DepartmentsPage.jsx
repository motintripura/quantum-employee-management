import { useEffect, useState, useCallback } from 'react';
import departmentService from '../../services/department.service';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { getErrorMessage, getFieldErrors } from '../../services/api';
import { DEFAULT_PAGE_SIZE } from '../../utils/constants';
import PageHeader from '../../components/common/PageHeader';
import SearchInput from '../../components/common/SearchInput';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorAlert from '../../components/common/ErrorAlert';
import EmptyState from '../../components/common/EmptyState';
import Pagination from '../../components/common/Pagination';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import StatusBadge from '../../components/common/StatusBadge';

const EMPTY_FORM = { name: '', description: '', status: true };

export default function DepartmentsPage() {
  const { user } = useAuth();
  const { success, error: toastError } = useToast();
  const isEditor = user.role === 'ADMIN' || user.role === 'HR';
  const canDelete = user.role === 'ADMIN';

  const [filters, setFilters] = useState({ keyword: '', status: '' });
  const [page, setPage] = useState(0);
  const [size] = useState(DEFAULT_PAGE_SIZE);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');

  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [formErrors, setFormErrors] = useState({});
  const [saving, setSaving] = useState(false);

  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setErr('');
    try {
      const result = await departmentService.getAll({
        keyword: filters.keyword || undefined,
        status: filters.status || undefined,
        page,
        size,
        sort: 'name,asc',
      });
      setData(result);
    } catch (e) {
      setErr(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [filters, page, size]);

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

  const openEdit = (dept) => {
    setEditing(dept);
    setForm({ name: dept.name, description: dept.description || '', status: dept.status });
    setFormErrors({});
    setShowModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    const errors = {};
    if (!form.name.trim()) errors.name = 'Name is required';
    else if (form.name.trim().length > 100) errors.name = 'Name must be at most 100 characters';
    setFormErrors(errors);
    if (Object.keys(errors).length) return;

    setSaving(true);
    try {
      if (editing) {
        await departmentService.update(editing.id, form);
        success('Department updated');
      } else {
        await departmentService.create(form);
        success('Department created');
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
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await departmentService.remove(deleteTarget.id);
      success(`Department "${deleteTarget.name}" deleted`);
      setDeleteTarget(null);
      load();
    } catch (e) {
      toastError(getErrorMessage(e));
    } finally {
      setDeleting(false);
    }
  };

  const handleToggle = async (dept) => {
    try {
      const updated = await departmentService.toggleStatus(dept.id);
      success(`Department "${updated.name}" ${updated.status ? 'activated' : 'deactivated'}`);
      load();
    } catch (e) {
      toastError(getErrorMessage(e));
    }
  };

  return (
    <div>
      <PageHeader
        title="Departments"
        subtitle="Organize your departments"
        actions={
          isEditor && (
            <button type="button" className="btn btn-primary" onClick={openCreate}>
              <i className="bi bi-plus-lg me-1" />
              Add Department
            </button>
          )
        }
      />

      <div className="filter-panel mb-3">
        <div className="row g-2">
          <div className="col-12 col-md-6">
            <SearchInput
              value={filters.keyword}
              onChange={(v) => handleFilterChange('keyword', v)}
              placeholder="Search departments..."
            />
          </div>
          <div className="col-6 col-md-3">
            <select
              className="form-select"
              value={filters.status}
              onChange={(e) => handleFilterChange('status', e.target.value)}
            >
              <option value="">All Status</option>
              <option value="true">Active</option>
              <option value="false">Inactive</option>
            </select>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead className="bg-light">
              <tr>
                <th>Name</th>
                <th>Description</th>
                <th className="text-end">Employees</th>
                <th>Status</th>
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="5">
                    <LoadingSpinner />
                  </td>
                </tr>
              ) : data && data.content.length ? (
                data.content.map((dept) => (
                  <tr key={dept.id}>
                    <td className="fw-semibold">{dept.name}</td>
                    <td className="text-muted">
                      <span className="text-truncate-2 d-block" style={{ maxWidth: 320 }}>
                        {dept.description || '—'}
                      </span>
                    </td>
                    <td className="text-end">{dept.employeeCount}</td>
                    <td>
                      <StatusBadge value={dept.status ? 'ACTIVE' : 'INACTIVE'} />
                    </td>
                    <td>
                      <div className="d-flex justify-content-end gap-1 table-actions">
                        {isEditor && (
                          <>
                            <button type="button" className="btn btn-sm btn-outline-primary" title="Edit" onClick={() => openEdit(dept)}>
                              <i className="bi bi-pencil" />
                            </button>
                            <button
                              type="button"
                              className="btn btn-sm btn-outline-warning"
                              title={dept.status ? 'Deactivate' : 'Activate'}
                              onClick={() => handleToggle(dept)}
                            >
                              <i className="bi bi-arrow-repeat" />
                            </button>
                          </>
                        )}
                        {canDelete && (
                          <button type="button" className="btn btn-sm btn-outline-danger" title="Delete" onClick={() => setDeleteTarget(dept)}>
                            <i className="bi bi-trash" />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="5">
                    {err ? <ErrorAlert message={err} /> : <EmptyState message="No departments found" icon="bi-diagram-3" />}
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
              <form onSubmit={handleSave} noValidate>
                <div className="modal-header">
                  <h5 className="modal-title">{editing ? 'Edit Department' : 'Add Department'}</h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)} />
                </div>
                <div className="modal-body">
                  <div className="mb-3">
                    <label className="form-label">Name *</label>
                    <input
                      type="text"
                      className={`form-control ${formErrors.name ? 'is-invalid' : ''}`}
                      value={form.name}
                      onChange={(e) => setForm({ ...form, name: e.target.value })}
                      maxLength={100}
                    />
                    {formErrors.name && <div className="invalid-feedback">{formErrors.name}</div>}
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Description</label>
                    <textarea
                      className="form-control"
                      rows={3}
                      value={form.description}
                      onChange={(e) => setForm({ ...form, description: e.target.value })}
                      maxLength={255}
                    />
                  </div>
                  <div className="form-check form-switch">
                    <input
                      className="form-check-input"
                      type="checkbox"
                      id="deptStatus"
                      checked={form.status}
                      onChange={(e) => setForm({ ...form, status: e.target.checked })}
                    />
                    <label className="form-check-label" htmlFor="deptStatus">
                      Active
                    </label>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-light" onClick={() => setShowModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={saving}>
                    {saving ? 'Saving...' : editing ? 'Update' : 'Create'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      <ConfirmDialog
        show={!!deleteTarget}
        title="Delete Department"
        message={
          deleteTarget
            ? `Are you sure you want to delete "${deleteTarget.name}"? Deletion is blocked while employees or designations are assigned.`
            : ''
        }
        confirmText={deleting ? 'Deleting...' : 'Delete'}
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
        disabled={deleting}
      />
    </div>
  );
}