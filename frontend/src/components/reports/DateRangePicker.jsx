export default function DateRangePicker({ fromDate, toDate, onFromChange, onToChange }) {
  return (
    <div className="d-flex align-items-center gap-2">
      <div>
        <label className="form-label small text-muted mb-0">From</label>
        <input
          type="date"
          className="form-control form-control-sm"
          value={fromDate || ''}
          onChange={(e) => onFromChange(e.target.value)}
        />
      </div>
      <div className="mt-auto pb-1 text-muted">
        <i className="bi bi-arrow-right" />
      </div>
      <div>
        <label className="form-label small text-muted mb-0">To</label>
        <input
          type="date"
          className="form-control form-control-sm"
          value={toDate || ''}
          onChange={(e) => onToChange(e.target.value)}
        />
      </div>
    </div>
  );
}
