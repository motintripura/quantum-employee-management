import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ROLE_LABELS } from '../utils/constants';

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', icon: 'bi-grid-1x2', roles: ['ADMIN', 'HR'] },
  { to: '/employees', label: 'Employees', icon: 'bi-people', roles: ['ADMIN', 'HR', 'MANAGER'] },
  { to: '/departments', label: 'Departments', icon: 'bi-diagram-3', roles: ['ADMIN', 'HR', 'MANAGER'] },
  { to: '/designations', label: 'Designations', icon: 'bi-briefcase', roles: ['ADMIN', 'HR', 'MANAGER'] },
  { to: '/attendance', label: 'Attendance', icon: 'bi-calendar-check', roles: ['ADMIN', 'HR', 'MANAGER'] },
  { to: '/leaves', label: 'Leaves', icon: 'bi-calendar2-week', roles: ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'] },
  { to: '/payroll', label: 'Payroll', icon: 'bi-cash-stack', roles: ['ADMIN', 'HR'] },
  { to: '/performance', label: 'Performance', icon: 'bi-graph-up', roles: ['ADMIN', 'HR', 'MANAGER'] },
  { to: '/reports', label: 'Reports', icon: 'bi-file-earmark-bar-graph', roles: ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'] },
  { to: '/settings', label: 'Settings', icon: 'bi-gear', roles: ['ADMIN'] },
  { to: '/profile', label: 'My Profile', icon: 'bi-person-circle', roles: ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'] },
];

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  const items = NAV_ITEMS.filter((item) => item.roles.includes(user.role));

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="app-shell">
      <aside className={`app-sidebar ${open ? 'show' : ''}`}>
        <div className="sidebar-brand">
          <i className="bi bi-building me-2" />
          EMS
        </div>
        <nav className="sidebar-nav">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
              onClick={() => setOpen(false)}
            >
              <i className={`bi ${item.icon}`} />
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
      </aside>
      {open && <div className="sidebar-backdrop show" onClick={() => setOpen(false)} />}

      <div className="app-main">
        <nav className="navbar navbar-expand app-navbar">
          <div className="container-fluid px-0">
            <button type="button" className="btn btn-link text-body d-lg-none p-1 me-2" onClick={() => setOpen(true)}>
              <i className="bi bi-list fs-3" />
            </button>
            <div className="ms-auto d-flex align-items-center gap-3">
              <span className="d-none d-md-inline text-muted small">
                {new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
              </span>
              <div className="dropdown">
                <button
                  className="btn btn-light dropdown-toggle d-flex align-items-center gap-2"
                  type="button"
                  data-bs-toggle="dropdown"
                  aria-expanded="false"
                >
                  <span className="avatar">{user.username ? user.username.charAt(0).toUpperCase() : 'U'}</span>
                  <span className="d-none d-sm-inline">{user.username}</span>
                </button>
                <ul className="dropdown-menu dropdown-menu-end shadow">
                  <li className="px-3 py-2">
                    <div className="fw-semibold">{user.username}</div>
                    <small className="text-muted">{ROLE_LABELS[user.role] || user.role}</small>
                  </li>
                  <li><hr className="dropdown-divider" /></li>
                  <li>
                    <NavLink className="dropdown-item" to="/profile">
                      <i className="bi bi-person me-2" />
                      My Profile
                    </NavLink>
                  </li>
                  <li>
                    <button type="button" className="dropdown-item text-danger" onClick={handleLogout}>
                      <i className="bi bi-box-arrow-right me-2" />
                      Logout
                    </button>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </nav>
        <main className="app-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}