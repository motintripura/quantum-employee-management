export default function Pagination({ page, totalPages, onChange }) {
  if (!totalPages || totalPages <= 1) return null;

  const items = [];
  items.push(0);
  for (let p = Math.max(1, page - 1); p <= Math.min(totalPages - 2, page + 1); p += 1) items.push(p);
  if (totalPages > 1) items.push(totalPages - 1);
  const unique = [...new Set(items)].sort((a, b) => a - b);

  const withEllipsis = [];
  for (let i = 0; i < unique.length; i += 1) {
    if (i > 0 && unique[i] - unique[i - 1] > 1) withEllipsis.push('...');
    withEllipsis.push(unique[i]);
  }

  return (
    <nav aria-label="Pagination">
      <ul className="pagination pagination-sm mb-0">
        <li className={`page-item ${page === 0 ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => onChange(page - 1)}>
            Previous
          </button>
        </li>
        {withEllipsis.map((item, idx) =>
          item === '...' ? (
            <li key={`e-${idx}`} className="page-item disabled">
              <span className="page-link">&hellip;</span>
            </li>
          ) : (
            <li key={item} className={`page-item ${item === page ? 'active' : ''}`}>
              <button className="page-link" onClick={() => onChange(item)}>
                {item + 1}
              </button>
            </li>
          )
        )}
        <li className={`page-item ${page >= totalPages - 1 ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => onChange(page + 1)}>
            Next
          </button>
        </li>
      </ul>
    </nav>
  );
}