import { createContext, useContext, useState, useCallback } from 'react';

const ToastContext = createContext(null);

let nextId = 1;

const ICONS = {
  success: 'bi-check-circle-fill',
  danger: 'bi-x-circle-fill',
  info: 'bi-info-circle-fill',
};

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const remove = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const push = useCallback((type, message) => {
    const id = nextId++;
    setToasts((prev) => [...prev, { id, type, message }]);
    setTimeout(() => remove(id), 4500);
  }, [remove]);

  const success = useCallback((m) => push('success', m), [push]);
  const error = useCallback((m) => push('danger', m), [push]);
  const info = useCallback((m) => push('info', m), [push]);

  return (
    <ToastContext.Provider value={{ success, error, info }}>
      {children}
      <div className="toast-container position-fixed top-0 end-0 p-3" style={{ zIndex: 1090 }}>
        {toasts.map((t) => (
          <div key={t.id} className={`toast show align-items-center border-0 text-bg-${t.type} mb-2`} role="status">
            <div className="d-flex">
              <div className="toast-body">
                <i className={`bi ${ICONS[t.type] || 'bi-info-circle-fill'} me-2`} />
                {t.message}
              </div>
              <button type="button" className="btn-close btn-close-white me-2 m-auto" onClick={() => remove(t.id)} />
            </div>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  return useContext(ToastContext);
}