import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import employeeService from '../../services/employee.service';
import departmentService from '../../services/department.service';
import designationService from '../../services/designation.service';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { getErrorMessage } from '../../services/api';
import { formatMoney, formatDate } from '../../utils/formatters';
import { UPLOADS_BASE_URL, DEFAULT_PAGE_SIZE } from '../../utils/constants';
import PageHeader from '../../components/common/PageHeader';
import SearchInput from '../../components/common/SearchInput';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorAlert from '../../components/common/ErrorAlert';
import EmptyState from '../../components/common/EmptyState';
import Pagination from '../../components/common/Pagination';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import StatusBadge from '../../components/common/StatusBadge';

const ROLE_SORT_OPTIONS = [
  { value: 'id,desc', label: 'Newest first' },
  { value: 'id,asc', label: 'Oldest first' },
  { value: 'firstName,asc', label: 'Name (A-Z)' },
  { value: 'joiningDate,desc', label: 'Joining date (newest)' },
];

export default function EmployeesList() {
  const { user } = useAuth();
  const { success, error: toastError } = useToast();

  const isEditor = user.role === 'ADMIN' || user.role === 'HR';
  const canDelete = user.role === 'ADMIN';

  const [filters, setFilters] = useState({
    keyword: '',
    departmentId: '',
    designationId: '',
    status: '',
  });
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(DEFAULT_PAGE_SIZE);
  const [sort, setSort] = useState('id,desc');
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');
  const [departments, setDepartments] = useState([]);
  const [designations, setDesignations] = useState([]);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const loadOptions = useCallback(async () => {
    try {
      const [depts, desigs] = await Promise.all([
        departmentService.getActive(),
        designationService.getActive(),
      ]);
      setDepartments(depts);
      setDesignations(desigs);
    } catch {
      // filters remain empty on failure
    }
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    setErr('');
    try {
      const params = {
        keyword: filters.keyword || undefined,
        departmentId: filters.departmentId || undefined,
        designationId: filters.designationId || undefined,
        status: filters.status || undefined,
        page,
        size,
        sort,
      };
      const result = await employeeService.getAll(params);
      setData(result);
    } catch (e) {
      setErr(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [filters, page, size, sort]);

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

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await employeeService.remove(deleteTarget.id);
      success(`Employee ${deleteTarget.fullName || deleteTarget.employeeCode} deleted`);
      setDeleteTarget(null);
      load();
    } catch (e) {
      toastError(getErrorMessage(e));
    } finally {
      setDeleting(false);
    }
  };

  const handleToggleStatus = async (emp) => {
    try {
      const updated = await employeeService.toggleStatus(emp.id);
      success(`Employee ${updated.fullName} ${updated.status ? 'activated' : 'deactivated'}`);
      load();
    } catch (e) {
      toastError(getErrorMessage(e));
    }
  };

  const handleSortChange = (e) => {
    setSort(e.target.value);
    setPage(0);
  };

  return (
    <div>
      <PageHeader
        title="Employees"
        subtitle="Manage your workforce"
        actions={
          isEditor && (
            <Link to="/employees/add" className="btn btn-primary">
              <i className="bi bi-person-plus me-1" />
              Add Employee
            </Link>
          )
        }
      />

      <div className="filter-panel mb-3">
        <div className="row g-2 align-items-end">
          <div className="col-12 col-md-4">
            <label className="form-label small mb-1">Search</label>
            <SearchInput
              value={filters.keyword}
              onChange={(v) => handleFilterChange('keyword', v)}
              placeholder="Name, email, phone, code..."
            />
          </div>
          <div className="col-6 col-md-2">
            <label className="form-label small mb-1">Department</label>
            <select
              className="form-select"
              value={filters.departmentId}
              onChange={(e) => handleFilterChange('departmentId', e.target.value)}
            >
              <option value="">All</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name}
                </option>
              ))}
            </select>
          </div>
          <div className="col-6 col-md-2">
            <label className="form-label small mb-1">Designation</label>
            <select
              className="form-select"
              value={filters.designationId}
              onChange={(e) => handleFilterChange('designationId', e.target.value)}
            >
              <option value="">All</option>
              {designations.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name}
                </option>
              ))}
            </select>
          </div>
          <div className="col-6 col-md-2">
            <label className="form-label small mb-1">Status</label>
            <select
              className="form-select"
              value={filters.status}
              onChange={(e) => handleFilterChange('status', e.target.value)}
            >
              <option value="">All</option>
              <option value="true">Active</option>
              <option value="false">Inactive</option>
            </select>
          </div>
          <div className="col-6 col-md-2">
            <label className="form-label small mb-1">Sort</label>
            <select className="form-select" value={sort} onChange={handleSortChange}>
              {ROLE_SORT_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label}
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
                <th>Email / Phone</th>
                <th>Department</th>
                <th>Designation</th>
                <th className="text-end">Salary</th>
                <th>Joining Date</th>
                <th>Status</th>
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
                data.content.map((emp) => (
                  <tr key={emp.id}>
                    <td>
                      <div className="d-flex align-items-center gap-2">
                        <img
                          className="profile-photo"
                          src={emp.profileImage ? `${UPLOADS_BASE_URL}/${emp.profileImage.replace(/^\/+/, '')}` : undefined}
                          alt=""
                          onError={(e) => {
                            e.currentTarget.style.display = 'none';
                          }}
                        />
                        <div>
                          <Link to={`/employees/${emp.id}`} className="text-decoration-none fw-semibold text-body">
                            {emp.fullName}
                          </Link>
                          <div className="small text-muted">{emp.employeeCode}</div>
                        </div>
                      </div>
                    </td>
                    <td>
                      <div>{emp.email}</div>
                      <div className="small text-muted">{emp.phone || '—'}</div>
                    </td>
                    <td>{emp.departmentName}</td>
                    <td>{emp.designationName}</td>
                    <td className="text-end">{formatMoney(emp.salary)}</td>
                    <td>{formatDate(emp.joiningDate)}</td>
                    <td>
                      <StatusBadge value={emp.status ? 'ACTIVE' : 'INACTIVE'} />
                    </td>
                    <td>
                      <div className="d-flex justify-content-end gap-1 table-actions">
                        <Link to={`/employees/${emp.id}`} className="btn btn-sm btn-outline-secondary" title="View">
                          <i className="bi bi-eye" />
                        </Link>
                        {isEditor && (
                          <>
                            <Link to={`/employees/${emp.id}/edit`} className="btn btn-sm btn-outline-primary" title="Edit">
                              <i className="bi bi-pencil" />
                            </Link>
                            <button
                              type="button"
                              className="btn btn-sm btn-outline-warning"
                              title={emp.status ? 'Deactivate' : 'Activate'}
                              onClick={() => handleToggleStatus(emp)}
                            >
                              <i className="bi bi-arrow-repeat" />
                            </button>
                          </>
                        )}
                        {canDelete && (
                          <button
                            type="button"
                            className="btn btn-sm btn-outline-danger"
                            title="Delete"
                            onClick={() => setDeleteTarget(emp)}
                          >
                            <i className="bi bi-trash" />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="8">
                    {err ? <ErrorAlert message={err} /> : <EmptyState message="No employees found" icon="bi-people" />}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        {data && data.content.length > 0 && (
          <div className="card-footer bg-white d-flex flex-wrap justify-content-between align-items-center gap-2">
            <div className="small text-muted">
              Showing {data.totalElements === 0 ? 0 : page * size + 1}&ndash;
              {Math.min((page + 1) * size, data.totalElements)} of {data.totalElements}
            </div>
            <div className="d-flex align-items-center gap-3">
              <select
                className="form-select form-select-sm"
                style={{ width: 80 }}
                value={size}
                onChange={(e) => {
                  setSize(Number(e.target.value));
                  setPage(0);
                }}
              >
                {[5, 10, 20, 50].map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
              <Pagination page={page} totalPages={data.totalPages} onChange={setPage} />
            </div>
          </div>
        )}
      </div>

      <ConfirmDialog
        show={!!deleteTarget}
        title="Delete Employee"
        message={`Are you sure you want to delete ${deleteTarget ? deleteTarget.fullName || deleteTarget.employeeCode : ''}? This action cannot be undone.`}
        confirmText={deleting ? 'Deleting...' : 'Delete'}
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
        disabled={deleting}
      />
    </div>
  );
}