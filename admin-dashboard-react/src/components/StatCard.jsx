/**
 * StatCard — accepts a lucide-react icon component via the `icon` prop.
 * e.g. <StatCard icon={TrendingUp} iconColor="orange" value="EGP 12,000" label="Revenue" />
 */
export default function StatCard({ icon: Icon, iconColor = 'orange', value, label }) {
  return (
    <div className="stat-card">
      <div className={`stat-icon ${iconColor}`}>
        {Icon && <Icon size={24} />}
      </div>
      <div>
        <div className="stat-value">{value ?? '—'}</div>
        <div className="stat-label">{label}</div>
      </div>
    </div>
  );
}
