import { AlertTriangle } from 'lucide-react';
import Modal from './Modal.jsx';

export default function ConfirmDialog({
  open, onClose, onConfirm,
  title, message,
  confirmLabel = 'Delete',
  loading = false,
}) {
  return (
    <Modal
      open={open}
      onClose={onClose}
      title={title}
      footer={
        <>
          <button className="btn btn-secondary" onClick={onClose} disabled={loading}>Cancel</button>
          <button className="btn btn-danger"    onClick={onConfirm} disabled={loading}>
            {loading
              ? <span className="spinner" />
              : confirmLabel}
          </button>
        </>
      }
    >
      <div style={{ display: 'flex', gap: 14, alignItems: 'flex-start' }}>
        <AlertTriangle size={22} color="var(--warning)" style={{ flexShrink: 0, marginTop: 1 }} />
        <div style={{ fontSize: 14, lineHeight: 1.6 }}>{message}</div>
      </div>
    </Modal>
  );
}
