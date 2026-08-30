const COLOR_MAP = {
  primary: { bg: 'bg-primary-subtle', text: 'text-primary' },
  success: { bg: 'bg-success-subtle', text: 'text-success' },
  danger: { bg: 'bg-danger-subtle', text: 'text-danger' },
  warning: { bg: 'bg-warning-subtle', text: 'text-warning' },
  info: { bg: 'bg-info-subtle', text: 'text-info' },
  secondary: { bg: 'bg-secondary-subtle', text: 'text-secondary' },
};

export default function ReportSummary({ items = [] }) {
  return (
    <div className="row g-3 mb-4">
      {items.map((item, idx) => {
        const color = COLOR_MAP[item.color] || COLOR_MAP.primary;
        return (
          <div key={idx} className="col-xl-3 col-md-6">
            <div className="card border-0 shadow-sm h-100">
              <div className="card-body">
                <div className="d-flex align-items-center">
                  <div className={`flex-shrink-0 ${color.bg} rounded-3 p-3`}>
                    <i className={`bi ${item.icon || 'bi-graph-up'} fs-4 ${color.text}`} />
                  </div>
                  <div className="ms-3">
                    <p className="text-muted mb-0 small">{item.label}</p>
                    <h5 className="fw-bold mb-0">{item.value}</h5>
                  </div>
                </div>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}
