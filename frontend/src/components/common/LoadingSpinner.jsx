export default function LoadingSpinner({ text = 'Loading...', full = false }) {
  if (full) {
    return (
      <div className="d-flex flex-column align-items-center justify-content-center py-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading</span>
        </div>
        {text && <span className="mt-2 text-muted">{text}</span>}
      </div>
    );
  }
  return (
    <div className="d-flex align-items-center justify-content-center py-4">
      <div className="spinner-border spinner-border-sm text-primary" role="status">
        <span className="visually-hidden">Loading</span>
      </div>
      {text && <span className="ms-2 text-muted">{text}</span>}
    </div>
  );
}