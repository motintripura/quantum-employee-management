export default function ErrorAlert({ message }) {
  if (!message) return null;
  return (
    <div className="alert alert-danger py-2 mb-3">
      <i className="bi bi-exclamation-triangle-fill me-2" />
      {message}
    </div>
  );
}