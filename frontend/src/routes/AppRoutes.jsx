import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import ProtectedRoute from './ProtectedRoute';
import AdminLayout from '../layouts/AdminLayout';
import Login from '../pages/Login';
import Dashboard from '../pages/Dashboard';
import EmployeesList from '../pages/employees/EmployeesList';
import EmployeeForm from '../pages/employees/EmployeeForm';
import EmployeeDetails from '../pages/employees/EmployeeDetails';
import DepartmentsPage from '../pages/departments/DepartmentsPage';
import DesignationsPage from '../pages/designations/DesignationsPage';
import AttendancePage from '../pages/attendance/AttendancePage';
import LeavesPage from '../pages/leaves/LeavesPage';
import PayrollPage from '../pages/payroll/PayrollPage';
import PerformancePage from '../pages/performance/PerformancePage';
import ProfilePage from '../pages/profile/ProfilePage';
import SettingsPage from '../pages/settings/SettingsPage';
import { homePathForRole } from '../utils/constants';

export default function AppRoutes() {
  const { user } = useAuth();
  const homePath = homePathForRole(user ? user.role : undefined);

  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route
        element={
          <ProtectedRoute>
            <AdminLayout />
          </ProtectedRoute>
        }
      >
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute roles={['ADMIN', 'HR']}>
              <Dashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/employees"
          element={
            <ProtectedRoute roles={['ADMIN', 'HR', 'MANAGER']}>
              <EmployeesList />
            </ProtectedRoute>
          }
        />
        <Route
          path="/employees/add"
          element={
            <ProtectedRoute roles={['ADMIN', 'HR']}>
              <EmployeeForm />
            </ProtectedRoute>
          }
        />
        <Route
          path="/employees/:id"
          element={
            <ProtectedRoute roles={['ADMIN', 'HR', 'MANAGER']}>
              <EmployeeDetails />
            </ProtectedRoute>
          }
        />
        <Route
          path="/employees/:id/edit"
          element={
            <ProtectedRoute roles={['ADMIN', 'HR']}>
              <EmployeeForm />
            </ProtectedRoute>
          }
        />

        <Route
          path="/departments"
          element={
            <ProtectedRoute roles={['ADMIN', 'HR', 'MANAGER']}>
              <DepartmentsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/designations"
          element={
            <ProtectedRoute roles={['ADMIN', 'HR', 'MANAGER']}>
              <DesignationsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/attendance"
          element={
            <ProtectedRoute roles={['ADMIN', 'HR', 'MANAGER']}>
              <AttendancePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/leaves"
          element={
            <ProtectedRoute roles={['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE']}>
              <LeavesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/payroll"
          element={
            <ProtectedRoute roles={['ADMIN', 'HR']}>
              <PayrollPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/performance"
          element={
            <ProtectedRoute roles={['ADMIN', 'HR', 'MANAGER']}>
              <PerformancePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute roles={['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE']}>
              <ProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/settings"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <SettingsPage />
            </ProtectedRoute>
          }
        />
      </Route>

      <Route path="/" element={<Navigate to={homePath} replace />} />
      <Route path="*" element={<Navigate to={homePath} replace />} />
    </Routes>
  );
}