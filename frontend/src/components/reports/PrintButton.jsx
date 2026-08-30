export default function PrintButton({ onPrint, disabled }) {
  const handlePrint = () => {
    if (onPrint) {
      onPrint();
    } else {
      window.print();
    }
  };

  return (
    <button
      type="button"
      className="btn btn-outline-secondary btn-sm"
      onClick={handlePrint}
      disabled={disabled}
      title="Print report"
    >
      <i className="bi bi-printer me-1" />
      Print
    </button>
  );
}
