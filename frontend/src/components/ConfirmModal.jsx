import { useEffect } from 'react';

export function Modal({ open, title, onClose, children, wide = false }) {
  useEffect(() => {
    if (!open) return undefined;
    const onKey = (e) => e.key === 'Escape' && onClose?.();
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [open, onClose]);

  if (!open) return null;
  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label={title}
      className="fixed inset-0 z-50 flex items-center justify-center bg-ink/50 p-4"
      onMouseDown={(e) => e.target === e.currentTarget && onClose?.()}
    >
      <div className={`card max-h-[90vh] w-full overflow-auto p-5 shadow-xl sm:p-6 ${wide ? 'max-w-3xl' : 'max-w-lg'}`}>
        <h2 className="mb-4 text-xl font-bold">{title}</h2>
        {children}
      </div>
    </div>
  );
}

export default function ConfirmModal({ open, title, message, confirmText = 'Confirm', danger = false, busy = false, onConfirm, onCancel }) {
  return (
    <Modal open={open} title={title} onClose={onCancel}>
      <p className="text-sm">{message}</p>
      <div className="mt-6 flex justify-end gap-2">
        <button type="button" className="btn btn-ghost" onClick={onCancel} disabled={busy}>
          Keep it
        </button>
        <button type="button" className={`btn ${danger ? 'btn-danger' : 'btn-primary'}`} onClick={onConfirm} disabled={busy}>
          {confirmText}
        </button>
      </div>
    </Modal>
  );
}
