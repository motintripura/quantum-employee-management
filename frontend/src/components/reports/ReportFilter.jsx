import { currentYear } from '../../utils/formatters';

const MONTHS = [
  { value: '1', label: 'January' },
  { value: '2', label: 'February' },
  { value: '3', label: 'March' },
  { value: '4', label: 'April' },
  { value: '5', label: 'May' },
  { value: '6', label: 'June' },
  { value: '7', label: 'July' },
  { value: '8', label: 'August' },
  { value: '9', label: 'September' },
  { value: '10', label: 'October' },
  { value: '11', label: 'November' },
  { value: '12', label: 'December' },
];

const YEARS = (() => {
  const y = currentYear();
  return Array.from({ length: 6 }, (_, i) => ({ value: String(y - i), label: String(y - i) }));
})();

function FilterControl({ filter, value, onChange }) {
  const { key, label, type, options, placeholder } = filter;

  if (type === 'select') {
    return (
      <div className="col-md-3 col-sm-6">
        <label className="form-label fw-medium">{label}</label>
        <select
          className="form-select form-select-sm"
          value={value || ''}
          onChange={(e) => onChange(key, e.target.value)}
        >
          <option value="">{placeholder || `All ${label}`}</option>
          {(options || []).map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
      </div>
    );
  }

  if (type === 'month') {
    return (
      <div className="col-md-2 col-sm-6">
        <label className="form-label fw-medium">{label}</label>
        <select
          className="form-select form-select-sm"
          value={value || ''}
          onChange={(e) => onChange(key, e.target.value)}
        >
          <option value="">All Months</option>
          {MONTHS.map((m) => (
            <option key={m.value} value={m.value}>
              {m.label}
            </option>
          ))}
        </select>
      </div>
    );
  }

  if (type === 'year') {
    return (
      <div className="col-md-2 col-sm-6">
        <label className="form-label fw-medium">{label}</label>
        <select
          className="form-select form-select-sm"
          value={value || ''}
          onChange={(e) => onChange(key, e.target.value)}
        >
          <option value="">All Years</option>
          {YEARS.map((y) => (
            <option key={y.value} value={y.value}>
              {y.label}
            </option>
          ))}
        </select>
      </div>
    );
  }

  if (type === 'date') {
    return (
      <div className="col-md-2 col-sm-6">
        <label className="form-label fw-medium">{label}</label>
        <input
          type="date"
          className="form-control form-control-sm"
          value={value || ''}
          onChange={(e) => onChange(key, e.target.value)}
        />
      </div>
    );
  }

  return (
    <div className="col-md-3 col-sm-6">
      <label className="form-label fw-medium">{label}</label>
      <input
        type="text"
        className="form-control form-control-sm"
        placeholder={placeholder || label}
        value={value || ''}
        onChange={(e) => onChange(key, e.target.value)}
      />
    </div>
  );
}

export default function ReportFilter({ filters = [], values = {}, onChange, onReset }) {
  return (
    <div className="card border-0 shadow-sm mb-4">
      <div className="card-body">
        <div className="row g-3 align-items-end">
          {filters.map((f) => (
            <FilterControl
              key={f.key}
              filter={f}
              value={values[f.key]}
              onChange={onChange}
            />
          ))}
          <div className="col-md-auto col-sm-6 d-flex gap-2">
            <button type="button" className="btn btn-primary btn-sm px-3">
              <i className="bi bi-bar-chart me-1" />
              Generate Report
            </button>
            <button type="button" className="btn btn-outline-secondary btn-sm" onClick={onReset}>
              <i className="bi bi-arrow-counterclockwise me-1" />
              Reset
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
