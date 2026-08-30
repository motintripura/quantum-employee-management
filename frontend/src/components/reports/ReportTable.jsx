import EmptyState from '../common/EmptyState';

export default function ReportTable({
  headers = [],
  data = [],
  columns = [],
  emptyMessage = 'No records found',
}) {
  if (!data.length) {
    return <EmptyState message={emptyMessage} icon="bi-table" />;
  }

  return (
    <div className="table-responsive">
      <table className="table table-striped table-hover align-middle mb-0">
        <thead className="table-light">
          <tr>
            {headers.map((h, i) => (
              <th key={i} className="fw-semibold">
                {h}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.map((row, rowIdx) => (
            <tr key={row.id || rowIdx}>
              {columns.map((col, colIdx) => (
                <td key={colIdx}>
                  {col.render ? col.render(row[col.key], row) : row[col.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
