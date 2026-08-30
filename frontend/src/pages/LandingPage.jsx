import { Navigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { homePathForRole } from '../utils/constants';

const FEATURES = [
  {
    icon: 'bi-people',
    title: 'Employee Management',
    text: 'Full CRUD for employee records with profile photos, search, filters, pagination, and active/inactive status tracking.',
  },
  {
    icon: 'bi-diagram-3',
    title: 'Departments & Designations',
    text: 'Organize your org structure with departments and designations, complete with assignment-protected deletion.',
  },
  {
    icon: 'bi-calendar-check',
    title: 'Attendance Tracking',
    text: 'Check-in/check-out, working hours and overtime calculation, plus monthly reports with detailed statuses.',
  },
  {
    icon: 'bi-calendar2-week',
    title: 'Leave Management',
    text: 'Leave types, balances, apply/cancel workflows, and approve/reject pipelines with overlap and balance validation.',
  },
  {
    icon: 'bi-cash-stack',
    title: 'Payroll & Payslips',
    text: 'Monthly payroll generation with automatic gross/net salary calculations, payslips, and payment status tracking.',
  },
  {
    icon: 'bi-graph-up',
    title: 'Performance Reviews',
    text: '1–5 rating system with strengths, weaknesses, and comments — plus a full review history per employee.',
  },
  {
    icon: 'bi-grid-1x2',
    title: 'Dashboard & Analytics',
    text: 'Statistic cards and live charts for employees by department, attendance, leave, monthly payroll, and performance.',
  },
  {
    icon: 'bi-shield-lock',
    title: 'Role-Based Access',
    text: 'Secure JWT authentication with BCrypt hashing and fine-grained access control for Admin, HR, Manager, and Employee.',
  },
];

const ROLES = [
  {
    icon: 'bi-person-gear',
    role: 'Admin',
    text: 'Full system access including settings, user oversight, and complete visibility across every module.',
    badge: 'Full access',
  },
  {
    icon: 'bi-person-badge',
    role: 'HR',
    text: 'Manages employees, departments, attendance, leave, payroll, and performance end to end.',
    badge: 'Most used',
  },
  {
    icon: 'bi-person-check',
    role: 'Manager',
    text: 'Views employees and team attendance, approves leave requests, and reviews performance.',
    badge: 'Approvals',
  },
  {
    icon: 'bi-person',
    role: 'Employee',
    text: 'Own profile, personal attendance, leave requests, and views of own payroll and performance.',
    badge: 'Self-service',
  },
];

export default function LandingPage() {
  const { user, token } = useAuth();

  if (token && user) {
    return <Navigate to={homePathForRole(user.role)} replace />;
  }

  return (
    <div className="landing">
      <header className="landing-nav">
        <nav className="container">
          <Link to="/" className="landing-brand">
            <span className="landing-logo">
              <i className="bi bi-building" />
            </span>
            <span>EMS</span>
            <small>Employee Management System</small>
          </Link>
          <ul className="landing-nav-links">
            <li><a href="#features">Features</a></li>
            <li><a href="#modules">Modules</a></li>
            <li><a href="#roles">Roles</a></li>
          </ul>
          <div className="d-flex align-items-center gap-2">
            <Link to="/login" className="btn btn-outline-light btn-sm">Sign In</Link>
            <Link to="/login" className="btn btn-light btn-sm d-none d-sm-inline-block">Get Started</Link>
          </div>
        </nav>
      </header>

      <section className="landing-hero">
        <div className="container">
          <div className="row align-items-center g-5">
            <div className="col-lg-6">
              <span className="landing-badge">
                <i className="bi bi-stars me-1" />
                All-in-one HR suite
              </span>
              <h1 className="landing-title">
                Manage your entire workforce from a <span className="text-gradient">single dashboard</span>
              </h1>
              <p className="landing-subtitle">
                A production-grade Employee Management System covering attendance, leave, payroll,
                performance reviews, and organization structure — secured with role-based access.
              </p>
              <div className="d-flex flex-wrap gap-3">
                <Link to="/login" className="btn btn-primary btn-lg px-4">
                  <i className="bi bi-box-arrow-in-right me-2" />
                  Sign In to Dashboard
                </Link>
                <a href="#features" className="btn btn-outline-light btn-lg px-4">
                  Explore Features
                  <i className="bi bi-arrow-down ms-2" />
                </a>
              </div>
              <div className="landing-stats">
                <div>
                  <strong>4</strong>
                  <span>Roles</span>
                </div>
                <div>
                  <strong>8</strong>
                  <span>Modules</span>
                </div>
                <div>
                  <strong>100%</strong>
                  <span>Digital</span>
                </div>
                <div>
                  <strong>24/7</strong>
                  <span>Available</span>
                </div>
              </div>
            </div>
            <div className="col-lg-6">
              <div className="landing-preview">
                <div className="preview-window">
                  <div className="preview-sidebar">
                    <div className="preview-logo" />
                    <i className="bi bi-grid-1x2" />
                    <i className="bi bi-people" />
                    <i className="bi bi-diagram-3" />
                    <i className="bi bi-calendar-check" />
                    <i className="bi bi-cash-stack" />
                    <i className="bi bi-graph-up" />
                  </div>
                  <div className="preview-body">
                    <div className="preview-topbar">
                      <span />
                      <span />
                      <span className="preview-avatar" />
                    </div>
                    <div className="preview-grid">
                      <div className="preview-card">
                        <i className="bi bi-people preview-card-icon" />
                        <strong>248</strong>
                        <span>Employees</span>
                      </div>
                      <div className="preview-card">
                        <i className="bi bi-calendar-check preview-card-icon green" />
                        <strong>203</strong>
                        <span>Present Today</span>
                      </div>
                      <div className="preview-card">
                        <i className="bi bi-cash-stack preview-card-icon amber" />
                        <strong>$1.2M</strong>
                        <span>Monthly Payroll</span>
                      </div>
                      <div className="preview-card">
                        <i className="bi bi-clock-history preview-card-icon violet" />
                        <strong>12</strong>
                        <span>Pending Leaves</span>
                      </div>
                    </div>
                    <div className="preview-chart">
                      <div className="preview-chart-bars">
                        <span style={{ height: '45%' }} />
                        <span style={{ height: '70%' }} />
                        <span style={{ height: '55%' }} />
                        <span style={{ height: '85%' }} />
                        <span style={{ height: '65%' }} />
                        <span style={{ height: '95%' }} />
                        <span style={{ height: '78%' }} />
                        <span style={{ height: '60%' }} />
                      </div>
                      <span className="preview-chart-line" />
                      <span className="preview-chart-dot" />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section id="features" className="landing-section">
        <div className="container">
          <div className="landing-section-head">
            <span className="landing-badge light"><i className="bi bi-magic me-1" />Why choose EMS</span>
            <h2>Everything your HR team needs</h2>
            <p>One secure platform for the full employee lifecycle — from onboarding records to payroll and performance.</p>
          </div>
          <div className="row g-4">
            {FEATURES.map((f) => (
              <div key={f.title} className="col-12 col-md-6 col-lg-3">
                <div className="landing-feature">
                  <div className="feature-icon">
                    <i className={`bi ${f.icon}`} />
                  </div>
                  <h5>{f.title}</h5>
                  <p>{f.text}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section id="modules" className="landing-section landing-section-alt">
        <div className="container">
          <div className="row align-items-center g-5">
            <div className="col-lg-5">
              <div className="landing-section-head text-start">
                <span className="landing-badge light"><i className="bi bi-diagram-3 me-1" />Full module suite</span>
                <h2>Built for every part of workforce management</h2>
                <p>
                  Spring Boot and React power a fast, reliable REST API with a clean, responsive interface.
                  Every module is wired end to end with validation, real business rules, and audit-friendly history.
                </p>
              </div>
              <ul className="landing-checklist">
                <li><i className="bi bi-check-circle-fill" /> JWT authentication with BCrypt hashing</li>
                <li><i className="bi bi-check-circle-fill" /> Role-based permissions on every endpoint</li>
                <li><i className="bi bi-check-circle-fill" /> Automatic payslip and salary calculations</li>
                <li><i className="bi bi-check-circle-fill" /> Monthly reports and analytics dashboards</li>
              </ul>
            </div>
            <div className="col-lg-7">
              <div className="row g-3">
                {FEATURES.slice(0, 6).map((f, i) => (
                  <div key={f.title} className={`col-md-6 ${i % 2 === 1 ? 'offset-md-0' : ''}`}>
                    <div className="landing-module">
                      <div className="module-icon">
                        <i className={`bi ${f.icon}`} />
                      </div>
                      <div>
                        <h6>{f.title}</h6>
                        <p>{f.text.slice(0, 70)}...</p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>

      <section id="roles" className="landing-section">
        <div className="container">
          <div className="landing-section-head">
            <span className="landing-badge light"><i className="bi bi-shield-lock me-1" />Role-based access</span>
            <h2>One system, four perspectives</h2>
            <p>Every user sees exactly what they need — nothing more, nothing less.</p>
          </div>
          <div className="row g-4">
            {ROLES.map((r) => (
              <div key={r.role} className="col-12 col-md-6 col-lg-3">
                <div className="landing-role">
                  <span className="role-badge">{r.badge}</span>
                  <div className="role-icon">
                    <i className={`bi ${r.icon}`} />
                  </div>
                  <h5>{r.role}</h5>
                  <p>{r.text}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="landing-cta">
        <div className="container">
          <div className="cta-box">
            <h2>Ready to streamline your workforce?</h2>
            <p>Sign in and start managing employees, attendance, leaves, and payroll today.</p>
            <Link to="/login" className="btn btn-light btn-lg px-5">
              <i className="bi bi-box-arrow-in-right me-2" />
              Sign In Now
            </Link>
          </div>
        </div>
      </section>

      <footer className="landing-footer">
        <div className="container">
          <div className="landing-brand mb-2">
            <span className="landing-logo">
              <i className="bi bi-building" />
            </span>
            <span>EMS</span>
          </div>
          <p className="mb-0">Employee Management System — attendance, leave, payroll, and performance in one place.</p>
          <small>&copy; {new Date().getFullYear()} Employee Management System. All rights reserved.</small>
        </div>
      </footer>
    </div>
  );
}