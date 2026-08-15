export default function PageHeader({ title, subtitle, actions }) {
  return (
    <div className="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-2">
      <div>
        <h4 className="mb-0 fw-semibold">{title}</h4>
        {subtitle && <small className="text-muted">{subtitle}</small>}
      </div>
      {actions && <div className="d-flex gap-2">{actions}</div>}
    </div>
  );
}