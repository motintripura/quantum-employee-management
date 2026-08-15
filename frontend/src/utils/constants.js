export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
export const UPLOADS_BASE_URL = process.env.REACT_APP_UPLOADS_URL || 'http://localhost:8080/uploads';

export const TOKEN_KEY = 'ems_token';
export const USER_KEY = 'ems_user';

export const ROLES = ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'];

export const ROLE_LABELS = {
  ADMIN: 'Admin',
  HR: 'HR',
  MANAGER: 'Manager',
  EMPLOYEE: 'Employee',
};

export const GENDERS = ['MALE', 'FEMALE', 'OTHER'];

export const ATTENDANCE_STATUSES = ['PRESENT', 'ABSENT', 'LATE', 'HALF_DAY', 'LEAVE'];
export const LEAVE_STATUSES = ['PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'];
export const PAYMENT_STATUSES = ['PENDING', 'PAID', 'CANCELLED'];

export const RATING_LABELS = {
  1: 'Poor',
  2: 'Needs Improvement',
  3: 'Average',
  4: 'Good',
  5: 'Excellent',
};

export const DEFAULT_PAGE_SIZE = 10;

export function homePathForRole(role) {
  if (role === 'ADMIN' || role === 'HR') return '/dashboard';
  if (role === 'MANAGER') return '/employees';
  return '/profile';
}