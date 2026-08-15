import { useState, useEffect } from 'react';

export default function SearchInput({ value, onChange, placeholder = 'Search...', className = '' }) {
  const [text, setText] = useState(value || '');

  useEffect(() => {
    setText(value || '');
  }, [value]);

  useEffect(() => {
    const timer = setTimeout(() => {
      if (text !== (value || '')) onChange(text);
    }, 400);
    return () => clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [text]);

  return (
    <div className={`input-group ${className}`}>
      <span className="input-group-text bg-white">
        <i className="bi bi-search text-muted" />
      </span>
      <input
        type="search"
        className="form-control"
        value={text}
        onChange={(e) => setText(e.target.value)}
        placeholder={placeholder}
        aria-label={placeholder}
      />
      {text && (
        <button type="button" className="btn btn-outline-secondary" onClick={() => setText('')}>
          <i className="bi bi-x-lg" />
        </button>
      )}
    </div>
  );
}