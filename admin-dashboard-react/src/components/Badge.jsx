const STATUS_CLASS = {
  pending:    'badge-warning',
  confirmed:  'badge-info',
  processing: 'badge-info',
  shipped:    'badge-orange',
  delivered:  'badge-success',
  cancelled:  'badge-danger',
};

export function StatusBadge({ status }) {
  const cls = STATUS_CLASS[status] ?? 'badge-muted';
  const label = status ? status.charAt(0).toUpperCase() + status.slice(1) : '—';
  return <span className={`badge ${cls}`}>{label}</span>;
}

export function StockBadge({ stock }) {
  const cls = stock > 10 ? 'badge-success' : stock > 0 ? 'badge-warning' : 'badge-danger';
  return <span className={`badge ${cls}`}>{stock ?? 0}</span>;
}

export function Badge({ children, type = 'muted' }) {
  return <span className={`badge badge-${type}`}>{children}</span>;
}
