import { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { getErrorMessage } from '../services/api';
import { homePathForRole } from '../utils/constants';

export default function Login() {
  const { user, token, login } = useAuth();
  const { success, error } = useToast();
  const navigate = useNavigate();

  const [form, setForm] = useState({ username: '', password: '' });
  const [submitting, setSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  if (token && user) {
    return <Navigate to={homePathForRole(user.role)} replace />;
  }

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.username.trim() || !form.password) {
      error('Username and password are required');
      return;
    }
    setSubmitting(true);
    try {
      const loggedIn = await login(form);
      success(`Welcome back, ${loggedIn.username}!`);
      navigate(homePathForRole(loggedIn.role), { replace: true });
    } catch (err) {
      error(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="login-wrap">
      <div className="login-card p-4 p-md-5">
        <div className="text-center mb-4">
          <div className="mb-3">
            <span className="avatar" style={{ width: 56, height: 56, fontSize: '1.4rem' }}>
              <i className="bi bi-building" />
            </span>
          </div>
          <h4 className="fw-bold mb-1">Employee Management System</h4>
          <p className="text-muted mb-0 small">Sign in to your account</p>
        </div>
        <form onSubmit={handleSubmit} noValidate>
          <div className="mb-3">
            <label className="form-label" htmlFor="username">
              Username or Email
            </label>
            <div className="input-group">
              <span className="input-group-text bg-white">
                <i className="bi bi-person text-muted" />
              </span>
              <input
                id="username"
                name="username"
                type="text"
                className="form-control"
                placeholder="Enter username or email"
                value={form.username}
                onChange={handleChange}
                autoComplete="username"
                autoFocus
              />
            </div>
          </div>
          <div className="mb-4">
            <label className="form-label" htmlFor="password">
              Password
            </label>
            <div className="input-group">
              <span className="input-group-text bg-white">
                <i className="bi bi-lock text-muted" />
              </span>
              <input
                id="password"
                name="password"
                type={showPassword ? 'text' : 'password'}
                className="form-control"
                placeholder="Enter password"
                value={form.password}
                onChange={handleChange}
                autoComplete="current-password"
              />
              <button
                type="button"
                className="btn btn-outline-secondary"
                onClick={() => setShowPassword((v) => !v)}
                tabIndex={-1}
              >
                <i className={`bi ${showPassword ? 'bi-eye-slash' : 'bi-eye'}`} />
              </button>
            </div>
          </div>
          <button type="submit" className="btn btn-primary w-100 py-2" disabled={submitting}>
            {submitting ? (
              <>
                <span className="spinner-border spinner-border-sm me-2" />
                Signing in...
              </>
            ) : (
              'Sign In'
            )}
          </button>
        </form>
        <p className="text-center text-muted small mt-4 mb-0">
          Default admin &mdash; <code>admin</code> / <code>Admin@123</code>
        </p>
      </div>
    </div>
  );
}