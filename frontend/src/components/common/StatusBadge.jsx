const COLOR_MAP = {
  PRESENT: 'success',
  ABSENT: 'danger',
  LATE: 'warning',
  HALF_DAY: 'info',
  LEAVE: 'secondary',
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
  CANCELLED: 'secondary',
  PAID: 'success',
  ACTIVE: 'success',
  INACTIVE: 'secondary',
  MALE: 'info',
  FEMALE: 'danger',
  OTHER: 'secondary',
};

export default function StatusBadge({ value, label }) {
  if (value == null) return <span className="text-muted">—</span>;
  const cls = COLOR_MAP[value] || 'secondary';
  return <span className={`badge text-bg-${cls}`}>{label || value}</span>;
}