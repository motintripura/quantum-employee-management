import { useEffect, useState, useCallback } from 'react';
import { leaveTypeService } from '../../services/leave.service';
import { useToast } from '../../context/ToastContext';
import { getErrorMessage, getFieldErrors } from '../../services/api';
import PageHeader from '../../components/common/PageHeader';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorAlert from '../../components/common/ErrorAlert';
import EmptyState from '../../components/common/EmptyState';
import StatusBadge from '../../components/common/StatusBadge';

const EMPTY_FORM = {
  name: '',
  description: '',
  defaultDays: '',
  status: true,
};

export default function SettingsPage() {
  const { success, error: toastError } = useToast();

  const [leaveTypes, setLeaveTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');

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
      const rows = await leaveTypeService.getAll();
      setLeaveTypes(rows || []);
    } catch (e) {
      setErr(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const openCreate = () => {
    setEditing(null);
    setForm(EMPTY_FORM);
    setFormErrors({});
    setShowModal(true);
  };

  const openEdit = (t) => {
    setEditing(t);
    setForm({
      name: t.name,
      description: t.description || '',
      defaultDays: t.defaultDays,
      status: t.status,
    });
    setFormErrors({});
    setShowModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    const errors = {};
    if (!form.name.trim()) errors.name = 'Name is required';
    if (form.defaultDays === '' || form.defaultDays === null) errors.defaultDays = 'Default days is required';
    else if (Number(form.defaultDays) < 0) errors.defaultDays = 'Default days cannot be negative';
    setFormErrors(errors);
    if (Object.keys(errors).length) return;

    setSaving(true);
    try {
      const payload = {
        name: form.name.trim(),
        description: form.description.trim() || null,
        defaultDays: Number(form.defaultDays),
        status: form.status,
      };
      if (editing) {
        await leaveTypeService.update(editing.id, payload);
        success('Leave type updated');
      } else {
        await leaveTypeService.create(payload);
        success('Leave type created');
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

  const handleToggle = async (t) => {
    try {
      const updated = await leaveTypeService.toggleStatus(t.id);
      success(`Leave type ${updated.status ? 'activated' : 'deactivated'}`);
      load();
    } catch (e) {
      toastError(getErrorMessage(e));
    }
  };

  const handleDelete = async () => {
    if (!confirmDelete) return;
    setDeleting(true);
    try {
      await leaveTypeService.remove(confirmDelete.id);
      success('Leave type deleted');
      setConfirmDelete(null);
      load();
    } catch (e) {
      toastError(getErrorMessage(e));
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div>
      <PageHeader
        title="Settings"
        subtitle="Manage leave types"
        actions={
          <button type="button" className="btn btn-primary" onClick={openCreate}>
            <i className="bi bi-plus-lg me-1" />
            Add Leave Type
          </button>
        }
      />

      <div className="card">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead className="bg-light">
              <tr>
                <th>Name</th>
                <th>Description</th>
                <th className="text-end">Default Days</th>
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
              ) : leaveTypes.length ? (
                leaveTypes.map((t) => (
                  <tr key={t.id}>
                    <td className="fw-semibold">{t.name}</td>
                    <td className="text-truncate" style={{ maxWidth: 300 }}>
                      {t.description || '—'}
                    </td>
                    <td className="text-end">{t.defaultDays}</td>
                    <td>
                      <StatusBadge value={t.status ? 'ACTIVE' : 'INACTIVE'} />
                    </td>
                    <td>
                      <div className="d-flex justify-content-end gap-1 table-actions">
                        <button type="button" className="btn btn-sm btn-outline-primary" title="Edit" onClick={() => openEdit(t)}>
                          <i className="bi bi-pencil" />
                        </button>
                        <button type="button" className="btn btn-sm btn-outline-secondary" title={t.status ? 'Deactivate' : 'Activate'} onClick={() => handleToggle(t)}>
                          <i className="bi bi-toggle2-off" />
                        </button>
                        <button type="button" className="btn btn-sm btn-outline-danger" title="Delete" onClick={() => setConfirmDelete(t)}>
                          <i className="bi bi-trash" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="5">
                    {err ? <ErrorAlert message={err} /> : <EmptyState message="No leave types configured" icon="bi-gear" />}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true">
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <form onSubmit={handleSave} noValidate>
                <div className="modal-header">
                  <h5 className="modal-title">{editing ? 'Edit Leave Type' : 'Add Leave Type'}</h5>
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
                      placeholder="e.g. Annual Leave"
                    />
                    {formErrors.name && <div className="invalid-feedback">{formErrors.name}</div>}
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Default Days *</label>
                    <input
                      type="number"
                      min="0"
                      className={`form-control ${formErrors.defaultDays ? 'is-invalid' : ''}`}
                      value={form.defaultDays}
                      onChange={(e) => setForm({ ...form, defaultDays: e.target.value })}
                      placeholder="e.g. 14"
                    />
                    {formErrors.defaultDays && <div className="invalid-feedback">{formErrors.defaultDays}</div>}
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Description</label>
                    <textarea
                      className="form-control"
                      rows={2}
                      value={form.description}
                      onChange={(e) => setForm({ ...form, description: e.target.value })}
                      maxLength={500}
                      placeholder="Short description (optional)"
                    />
                  </div>
                  <div className="form-check form-switch">
                    <input
                      className="form-check-input"
                      type="checkbox"
                      id="leaveTypeActive"
                      checked={form.status}
                      onChange={(e) => setForm({ ...form, status: e.target.checked })}
                    />
                    <label className="form-check-label" htmlFor="leaveTypeActive">
                      Active
                    </label>
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
                <h5 className="mb-2">Delete Leave Type?</h5>
                <p className="text-muted mb-0">
                  <strong>{confirmDelete.name}</strong> will be permanently removed.
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