import { useEffect, useState, useCallback } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import employeeService from '../../services/employee.service';
import departmentService from '../../services/department.service';
import designationService from '../../services/designation.service';
import { useToast } from '../../context/ToastContext';
import { getErrorMessage, getFieldErrors } from '../../services/api';
import { GENDERS } from '../../utils/constants';
import PageHeader from '../../components/common/PageHeader';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorAlert from '../../components/common/ErrorAlert';

const EMPTY_FORM = {
  employeeCode: '',
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  dateOfBirth: '',
  gender: '',
  address: '',
  joiningDate: '',
  departmentId: '',
  designationId: '',
  salary: '',
  status: true,
};

export default function EmployeeForm() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const { success, error: toastError } = useToast();

  const [form, setForm] = useState(EMPTY_FORM);
  const [departments, setDepartments] = useState([]);
  const [designations, setDesignations] = useState([]);
  const [loading, setLoading] = useState(isEdit);
  const [submitting, setSubmitting] = useState(false);
  const [loadErr, setLoadErr] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [tempPassword, setTempPassword] = useState('');

  const loadDepartments = useCallback(async () => {
    try {
      const depts = await departmentService.getActive();
      setDepartments(depts);
      return depts;
    } catch (e) {
      toastError(getErrorMessage(e));
      return [];
    }
  }, [toastError]);

  useEffect(() => {
    async function init() {
      const depts = await loadDepartments();
      if (isEdit) {
        try {
          const emp = await employeeService.getById(id);
          setForm({
            employeeCode: emp.employeeCode || '',
            firstName: emp.firstName || '',
            lastName: emp.lastName || '',
            email: emp.email || '',
            phone: emp.phone || '',
            dateOfBirth: emp.dateOfBirth || '',
            gender: emp.gender || '',
            address: emp.address || '',
            joiningDate: emp.joiningDate || '',
            departmentId: emp.departmentId || '',
            designationId: emp.designationId || '',
            salary: emp.salary != null ? emp.salary : '',
            status: emp.status,
          });
          if (depts.some((d) => d.id === emp.departmentId)) {
            const desigs = await designationService.getActive(emp.departmentId);
            setDesignations(desigs);
          }
        } catch (e) {
          setLoadErr(getErrorMessage(e));
        } finally {
          setLoading(false);
        }
      } else {
        setLoading(false);
      }
    }
    init();
  }, [id, isEdit, loadDepartments]);

  const handleDepartmentChange = async (departmentId) => {
    setForm((prev) => ({ ...prev, departmentId, designationId: '' }));
    setDesignations([]);
    if (departmentId) {
      try {
        const desigs = await designationService.getActive(departmentId);
        setDesignations(desigs);
      } catch (e) {
        toastError(getErrorMessage(e));
      }
    }
  };

  const handleGenerateCode = async () => {
    try {
      const code = await employeeService.generateCode();
      setForm((prev) => ({ ...prev, employeeCode: code }));
    } catch (e) {
      toastError(getErrorMessage(e));
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
    setFieldErrors((prev) => ({ ...prev, [name]: undefined }));
  };

  const validate = () => {
    const errors = {};
    if (!form.firstName.trim()) errors.firstName = 'First name is required';
    if (!form.email.trim()) errors.email = 'Email is required';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) errors.email = 'Enter a valid email address';
    if (!form.joiningDate) errors.joiningDate = 'Joining date is required';
    else if (new Date(form.joiningDate) > new Date()) errors.joiningDate = 'Joining date cannot be in the future';
    if (form.dateOfBirth && new Date(form.dateOfBirth) >= new Date())
      errors.dateOfBirth = 'Date of birth must be in the past';
    if (!form.departmentId) errors.departmentId = 'Department is required';
    if (!form.designationId) errors.designationId = 'Designation is required';
    if (form.salary !== '' && (Number.isNaN(Number(form.salary)) || Number(form.salary) < 0))
      errors.salary = 'Salary cannot be negative';
    return errors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errors = validate();
    setFieldErrors(errors);
    if (Object.keys(errors).length) return;

    const payload = {
      employeeCode: form.employeeCode || null,
      firstName: form.firstName.trim(),
      lastName: form.lastName || null,
      email: form.email.trim(),
      phone: form.phone || null,
      dateOfBirth: form.dateOfBirth || null,
      gender: form.gender || null,
      address: form.address || null,
      joiningDate: form.joiningDate,
      departmentId: Number(form.departmentId),
      designationId: Number(form.designationId),
      salary: form.salary === '' ? null : Number(form.salary),
      status: form.status,
    };

    setSubmitting(true);
    try {
      if (isEdit) {
        await employeeService.update(id, payload);
        success('Employee updated successfully');
        navigate(`/employees/${id}`);
      } else {
        const created = await employeeService.create(payload);
        success('Employee created successfully');
        setTempPassword(created.temporaryPassword || '');
        navigate(`/employees/${created.id}`, { state: { tempPassword: created.temporaryPassword } });
      }
    } catch (err) {
      const msg = getErrorMessage(err);
      const fe = getFieldErrors(err);
      if (Object.keys(fe).length) {
        setFieldErrors(fe);
      } else {
        toastError(msg);
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <LoadingSpinner full text="Loading employee..." />;
  if (loadErr) return <ErrorAlert message={loadErr} />;

  return (
    <div>
      <PageHeader
        title={isEdit ? 'Edit Employee' : 'Add Employee'}
        subtitle={isEdit ? 'Update employee information' : 'Register a new employee'}
        actions={
          <Link to={isEdit ? `/employees/${id}` : '/employees'} className="btn btn-light">
            <i className="bi bi-arrow-left me-1" />
            Back
          </Link>
        }
      />

      {tempPassword && (
        <div className="alert alert-success">
          <i className="bi bi-check-circle-fill me-2" />
          Employee created. Temporary password: <code>{tempPassword}</code>
        </div>
      )}

      <div className="card">
        <div className="card-body">
          <form onSubmit={handleSubmit} noValidate>
            <h6 className="text-primary fw-semibold mb-3">Personal Information</h6>
            <div className="row g-3 mb-4">
              <div className="col-md-4">
                <label className="form-label">Employee Code</label>
                <div className="input-group">
                  <input
                    type="text"
                    className="form-control"
                    name="employeeCode"
                    value={form.employeeCode}
                    onChange={handleChange}
                    placeholder="Auto-generated"
                    maxLength={50}
                  />
                  <button type="button" className="btn btn-outline-secondary" onClick={handleGenerateCode}>
                    <i className="bi bi-magic" />
                  </button>
                </div>
              </div>
              <div className="col-md-4">
                <label className="form-label">First Name *</label>
                <input
                  type="text"
                  className={`form-control ${fieldErrors.firstName ? 'is-invalid' : ''}`}
                  name="firstName"
                  value={form.firstName}
                  onChange={handleChange}
                />
                {fieldErrors.firstName && <div className="invalid-feedback">{fieldErrors.firstName}</div>}
              </div>
              <div className="col-md-4">
                <label className="form-label">Last Name</label>
                <input
                  type="text"
                  className="form-control"
                  name="lastName"
                  value={form.lastName}
                  onChange={handleChange}
                />
              </div>
              <div className="col-md-4">
                <label className="form-label">Email *</label>
                <input
                  type="email"
                  className={`form-control ${fieldErrors.email ? 'is-invalid' : ''}`}
                  name="email"
                  value={form.email}
                  onChange={handleChange}
                />
                {fieldErrors.email && <div className="invalid-feedback">{fieldErrors.email}</div>}
              </div>
              <div className="col-md-4">
                <label className="form-label">Phone</label>
                <input
                  type="text"
                  className="form-control"
                  name="phone"
                  value={form.phone}
                  onChange={handleChange}
                  maxLength={30}
                />
              </div>
              <div className="col-md-4">
                <label className="form-label">Gender</label>
                <select className="form-select" name="gender" value={form.gender} onChange={handleChange}>
                  <option value="">Select</option>
                  {GENDERS.map((g) => (
                    <option key={g} value={g}>
                      {g.charAt(0) + g.slice(1).toLowerCase()}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">Date of Birth</label>
                <input
                  type="date"
                  className={`form-control ${fieldErrors.dateOfBirth ? 'is-invalid' : ''}`}
                  name="dateOfBirth"
                  value={form.dateOfBirth}
                  onChange={handleChange}
                />
                {fieldErrors.dateOfBirth && <div className="invalid-feedback">{fieldErrors.dateOfBirth}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Joining Date *</label>
                <input
                  type="date"
                  className={`form-control ${fieldErrors.joiningDate ? 'is-invalid' : ''}`}
                  name="joiningDate"
                  value={form.joiningDate}
                  onChange={handleChange}
                />
                {fieldErrors.joiningDate && <div className="invalid-feedback">{fieldErrors.joiningDate}</div>}
              </div>
              <div className="col-12">
                <label className="form-label">Address</label>
                <textarea
                  className="form-control"
                  name="address"
                  rows={2}
                  value={form.address}
                  onChange={handleChange}
                />
              </div>
            </div>

            <h6 className="text-primary fw-semibold mb-3">Work Information</h6>
            <div className="row g-3 mb-4">
              <div className="col-md-4">
                <label className="form-label">Department *</label>
                <select
                  className={`form-select ${fieldErrors.departmentId ? 'is-invalid' : ''}`}
                  name="departmentId"
                  value={form.departmentId}
                  onChange={(e) => handleDepartmentChange(e.target.value)}
                >
                  <option value="">Select department</option>
                  {departments.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.name}
                    </option>
                  ))}
                </select>
                {fieldErrors.departmentId && <div className="invalid-feedback">{fieldErrors.departmentId}</div>}
              </div>
              <div className="col-md-4">
                <label className="form-label">Designation *</label>
                <select
                  className={`form-select ${fieldErrors.designationId ? 'is-invalid' : ''}`}
                  name="designationId"
                  value={form.designationId}
                  onChange={handleChange}
                  disabled={!form.departmentId}
                >
                  <option value="">Select designation</option>
                  {designations.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.name}
                    </option>
                  ))}
                </select>
                {fieldErrors.designationId && <div className="invalid-feedback">{fieldErrors.designationId}</div>}
              </div>
              <div className="col-md-4">
                <label className="form-label">Salary</label>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  className={`form-control ${fieldErrors.salary ? 'is-invalid' : ''}`}
                  name="salary"
                  value={form.salary}
                  onChange={handleChange}
                />
                {fieldErrors.salary && <div className="invalid-feedback">{fieldErrors.salary}</div>}
              </div>
              <div className="col-md-4 d-flex align-items-center">
                <div className="form-check form-switch">
                  <input
                    className="form-check-input"
                    type="checkbox"
                    name="status"
                    id="statusSwitch"
                    checked={form.status}
                    onChange={handleChange}
                  />
                  <label className="form-check-label" htmlFor="statusSwitch">
                    Active
                  </label>
                </div>
              </div>
            </div>

            <div className="d-flex gap-2">
              <button type="submit" className="btn btn-primary px-4" disabled={submitting}>
                {submitting ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2" />
                    Saving...
                  </>
                ) : isEdit ? (
                  'Update Employee'
                ) : (
                  'Create Employee'
                )}
              </button>
              <Link to={isEdit ? `/employees/${id}` : '/employees'} className="btn btn-light">
                Cancel
              </Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}