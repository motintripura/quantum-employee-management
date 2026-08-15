import { useEffect, useState, useCallback } from 'react';
import designationService from '../../services/designation.service';
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

const EMPTY_FORM = { name: '', description: '', departmentId: '', status: true };

export default function DesignationsPage() {
  const { user } = useAuth();
  const { success, error: toastError } = useToast();
  const isEditor = user.role === 'ADMIN' || user.role === 'HR';
  const canDelete = user.role === 'ADMIN';

  const [filters, setFilters] = useState({ keyword: '', departmentId: '', status: '' });
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(DEFAULT_PAGE_SIZE);
  const [data, setData] = useState(null);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');

  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [formErrors, setFormErrors] = useState({});
  const [saving, setSaving] = useState(false);

  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const loadDepartments = useCallback(async () => {
    try {
      setDepartments(await departmentService.getActive());
    } catch {
      setDepartments([]);
    }
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    setErr('');
    try {
      const result = await designationService.getAll({
        keyword: filters.keyword || undefined,
        departmentId: filters.departmentId || undefined,
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
    loadDepartments();
  }, [loadDepartments]);

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

  const openEdit = (desig) => {
    setEditing(desig);
    setForm({
      name: desig.name,
      description: desig.description || '',
      departmentId: desig.departmentId,
      status: desig.status,
    });
    setFormErrors({});
    setShowModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    const errors = {};
    if (!form.name.trim()) errors.name = 'Name is required';
    else if (form.name.trim().length > 100) errors.name = 'Name must be at most 100 characters';
    if (!form.departmentId) errors.departmentId = 'Department is required';
    setFormErrors(errors);
    if (Object.keys(errors).length) return;

    setSaving(true);
    try {
      const payload = {
        name: form.name.trim(),
        description: form.description || null,
        departmentId: Number(form.departmentId),
        status: form.status,
      };
      if (editing) {
        await designationService.update(editing.id, payload);
        success('Designation updated');
      } else {
        await designationService.create(payload);
        success('Designation created');
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
      await designationService.remove(deleteTarget.id);
      success(`Designation "${deleteTarget.name}" deleted`);
      setDeleteTarget(null);
      load();
    } catch (e) {
      toastError(getErrorMessage(e));
    } finally {
      setDeleting(false);
    }
  };

  const handleToggle = async (desig) => {
    try {
      const updated = await designationService.toggleStatus(desig.id);
      success(`Designation "${updated.name}" ${updated.status ? 'activated' : 'deactivated'}`);
      load();
    } catch (e) {
      toastError(getErrorMessage(e));
    }
  };

  return (
    <div>
      <PageHeader
        title="Designations"
        subtitle="Job roles across departments"
        actions={
          isEditor && (
            <button type="button" className="btn btn-primary" onClick={openCreate}>
              <i className="bi bi-plus-lg me-1" />
              Add Designation
            </button>
          )
        }
      />

      <div className="filter-panel mb-3">
        <div className="row g-2">
          <div className="col-12 col-md-5">
            <SearchInput
              value={filters.keyword}
              onChange={(v) => handleFilterChange('keyword', v)}
              placeholder="Search designations..."
            />
          </div>
          <div className="col-6 col-md-3">
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
                <th>Department</th>
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
                data.content.map((desig) => (
                  <tr key={desig.id}>
                    <td className="fw-semibold">{desig.name}</td>
                    <td className="text-muted">
                      <span className="text-truncate-2 d-block" style={{ maxWidth: 320 }}>
                        {desig.description || '—'}
                      </span>
                    </td>
                    <td>{desig.departmentName}</td>
                    <td>
                      <StatusBadge value={desig.status ? 'ACTIVE' : 'INACTIVE'} />
                    </td>
                    <td>
                      <div className="d-flex justify-content-end gap-1 table-actions">
                        {isEditor && (
                          <>
                            <button type="button" className="btn btn-sm btn-outline-primary" title="Edit" onClick={() => openEdit(desig)}>
                              <i className="bi bi-pencil" />
                            </button>
                            <button
                              type="button"
                              className="btn btn-sm btn-outline-warning"
                              title={desig.status ? 'Deactivate' : 'Activate'}
                              onClick={() => handleToggle(desig)}
                            >
                              <i className="bi bi-arrow-repeat" />
                            </button>
                          </>
                        )}
                        {canDelete && (
                          <button type="button" className="btn btn-sm btn-outline-danger" title="Delete" onClick={() => setDeleteTarget(desig)}>
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
                    {err ? <ErrorAlert message={err} /> : <EmptyState message="No designations found" icon="bi-briefcase" />}
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
                  <h5 className="modal-title">{editing ? 'Edit Designation' : 'Add Designation'}</h5>
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
                      rows={2}
                      value={form.description}
                      onChange={(e) => setForm({ ...form, description: e.target.value })}
                      maxLength={255}
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Department *</label>
                    <select
                      className={`form-select ${formErrors.departmentId ? 'is-invalid' : ''}`}
                      value={form.departmentId}
                      onChange={(e) => setForm({ ...form, departmentId: e.target.value })}
                    >
                      <option value="">Select department</option>
                      {departments.map((d) => (
                        <option key={d.id} value={d.id}>
                          {d.name}
                        </option>
                      ))}
                    </select>
                    {formErrors.departmentId && <div className="invalid-feedback">{formErrors.departmentId}</div>}
                  </div>
                  <div className="form-check form-switch">
                    <input
                      className="form-check-input"
                      type="checkbox"
                      id="desigStatus"
                      checked={form.status}
                      onChange={(e) => setForm({ ...form, status: e.target.checked })}
                    />
                    <label className="form-check-label" htmlFor="desigStatus">
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
        title="Delete Designation"
        message={
          deleteTarget
            ? `Are you sure you want to delete "${deleteTarget.name}"? Deletion is blocked while employees are assigned.`
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