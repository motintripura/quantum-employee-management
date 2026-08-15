export default function EmptyState({ message = 'No records found', icon = 'bi-inbox' }) {
  return (
    <div className="text-center text-muted py-5">
      <i className={`bi ${icon} display-4 d-block mb-2 opacity-50`} />
      <div>{message}</div>
    </div>
  );
}