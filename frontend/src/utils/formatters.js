export function formatMoney(value) {
  if (value == null || Number.isNaN(Number(value))) return '0.00';
  return Number(value).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

export function formatDate(value) {
  if (!value) return '—';
  return String(value);
}

export function formatDateTime(value) {
  if (!value) return '—';
  return String(value).replace('T', ' ').slice(0, 19);
}

export function formatTime(value) {
  if (!value) return '—';
  return String(value).slice(0, 5);
}

export function fullName(emp) {
  if (!emp) return '—';
  if (emp.fullName) return emp.fullName;
  return [emp.firstName, emp.lastName].filter(Boolean).join(' ') || '—';
}

export function monthName(month) {
  const names = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December',
  ];
  return names[month - 1] || month;
}

export function buildSort(field, direction = 'asc') {
  return `${field},${direction}`;
}

export function toQuery(params) {
  return Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join('&');
}

export function currentYear() {
  return new Date().getFullYear();
}

export function currentMonth() {
  return new Date().getMonth() + 1;
}

export function today() {
  return new Date().toISOString().slice(0, 10);
}