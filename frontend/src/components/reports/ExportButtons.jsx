export default function ExportButtons({ onExportPdf, onExportExcel, onExportCsv, onPrint, disabled }) {
  return (
    <div className="btn-group" role="group">
      <button
        type="button"
        className="btn btn-outline-danger btn-sm"
        onClick={onExportPdf}
        disabled={disabled}
        title="Export as PDF"
      >
        <i className="bi bi-file-earmark-pdf me-1" />
        PDF
      </button>
      <button
        type="button"
        className="btn btn-outline-success btn-sm"
        onClick={onExportExcel}
        disabled={disabled}
        title="Export as Excel"
      >
        <i className="bi bi-file-earmark-excel me-1" />
        Excel
      </button>
      <button
        type="button"
        className="btn btn-outline-primary btn-sm"
        onClick={onExportCsv}
        disabled={disabled}
        title="Export as CSV"
      >
        <i className="bi bi-filetype-csv me-1" />
        CSV
      </button>
      <button
        type="button"
        className="btn btn-outline-secondary btn-sm"
        onClick={onPrint}
        disabled={disabled}
        title="Print report"
      >
        <i className="bi bi-printer me-1" />
        Print
      </button>
    </div>
  );
}
