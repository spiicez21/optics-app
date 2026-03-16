import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  collection, collectionGroup, getDocs,
  getCountFromServer,
} from 'firebase/firestore';
import {
  Chart as ChartJS,
  CategoryScale, LinearScale, PointElement, LineElement,
  ArcElement, Tooltip, Legend, Filler,
} from 'chart.js';
import { Line, Doughnut } from 'react-chartjs-2';
import { TrendingUp, Package, ShoppingBag, Users, CreditCard, BarChart2, Glasses } from 'lucide-react';
import { db } from '../lib/firebase.js';
import StatCard from '../components/StatCard.jsx';
import Modal from '../components/Modal.jsx';
import { StatusBadge } from '../components/Badge.jsx';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, ArcElement, Tooltip, Legend, Filler);

const fmt = n => n != null ? '₹' + n.toLocaleString('en-IN', { maximumFractionDigits: 0 }) : '—';

// Orders store timestamp as epoch ms (Long), not Firestore Timestamp
const getOrderDate = o => o.timestamp ? new Date(o.timestamp) : o.createdAt?.toDate?.() ?? null;

// Status is stored uppercase ("PENDING") — normalise for display/grouping
const normStatus = s => (s ?? 'pending').toLowerCase();

export default function Dashboard() {
  const [stats,         setStats]         = useState(null);
  const [revenueData,   setRevenueData]   = useState(null);
  const [statusData,    setStatusData]    = useState(null);
  const [recentOrders,  setRecentOrders]  = useState(null);
  const [topProducts,   setTopProducts]   = useState(null);
  const [loading,       setLoading]       = useState(true);
  const [selectedTx,    setSelectedTx]    = useState(null);

  useEffect(() => {
    loadAll();
  }, []);

  async function loadAll() {
    try {
      const [orderSnap, productCount, userCount] = await Promise.all([
        getDocs(collectionGroup(db, 'orders')),
        getCountFromServer(collection(db, 'products')),
        getCountFromServer(collection(db, 'users')),
      ]);

      // ── Stats ──
      let totalRevenue = 0;
      const orders = [];
      const todayStart = new Date(); todayStart.setHours(0, 0, 0, 0);
      let todayRevenue = 0;
      orderSnap.forEach(d => {
        const o = { id: d.id, _ref: d.ref, ...d.data() };
        totalRevenue += o.totalAmount ?? 0;
        const ts = getOrderDate(o);
        if (ts && ts >= todayStart) todayRevenue += o.totalAmount ?? 0;
        orders.push(o);
      });
      orders.sort((a, b) => (getOrderDate(b)?.getTime() ?? 0) - (getOrderDate(a)?.getTime() ?? 0));
      const avgOrder = orders.length > 0 ? totalRevenue / orders.length : 0;

      setStats({
        revenue:    fmt(totalRevenue),
        orders:     orders.length,
        products:   productCount.data().count,
        customers:  userCount.data().count,
        avgOrder:   fmt(avgOrder),
        todayRev:   fmt(todayRevenue),
      });

      // ── Revenue last 7 days ──
      const days = [], labels = [], values = [];
      for (let i = 6; i >= 0; i--) {
        const d = new Date(); d.setDate(d.getDate() - i); d.setHours(0, 0, 0, 0);
        days.push(d);
        labels.push(d.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' }));
        values.push(0);
      }
      orders.forEach(o => {
        const ts = getOrderDate(o);
        if (!ts) return;
        for (let i = 0; i < days.length; i++) {
          const next = new Date(days[i]); next.setDate(next.getDate() + 1);
          if (ts >= days[i] && ts < next) { values[i] += o.totalAmount ?? 0; break; }
        }
      });
      setRevenueData({ labels, datasets: [{ label: 'Revenue (₹)', data: values, borderColor: '#FF6B35', backgroundColor: 'rgba(255,107,53,.1)', borderWidth: 2.5, pointBackgroundColor: '#FF6B35', pointRadius: 4, tension: .4, fill: true }] });

      // ── Status doughnut ──
      const counts = { pending: 0, confirmed: 0, shipped: 0, delivered: 0, cancelled: 0 };
      orders.forEach(o => { const s = normStatus(o.status); if (s in counts) counts[s]++; else counts.pending++; });
      setStatusData({
        labels: Object.keys(counts).map(k => k.charAt(0).toUpperCase() + k.slice(1)),
        datasets: [{ data: Object.values(counts), backgroundColor: ['#f59e0b','#3b82f6','#FF6B35','#22c55e','#ef4444'], borderWidth: 2, borderColor: '#fff' }],
      });

      // ── Recent 8 orders ──
      setRecentOrders(orders.slice(0, 8));

      // ── Top products ──
      const revMap = {};
      orders.forEach(o => {
        (o.items ?? []).forEach(item => {
          const pid = item.productId ?? item.productName;
          if (!pid) return;
          if (!revMap[pid]) revMap[pid] = { name: item.productName ?? pid, total: 0, qty: 0 };
          revMap[pid].total += (item.price ?? 0) * (item.quantity ?? 1);
          revMap[pid].qty   += item.quantity ?? 1;
        });
      });
      const sorted = Object.entries(revMap).sort((a, b) => b[1].total - a[1].total).slice(0, 6);
      setTopProducts(sorted);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  const lineOptions = {
    responsive: true,
    plugins: { legend: { display: false } },
    scales: {
      x: { grid: { display: false } },
      y: { grid: { color: '#f1f5f9' }, beginAtZero: true, ticks: { callback: v => '₹' + v.toLocaleString('en-IN') } },
    },
  };

  const doughnutOptions = {
    cutout: '65%',
    plugins: { legend: { position: 'bottom', labels: { padding: 12, font: { size: 12 } } } },
  };

  if (loading) return <PageLoader />;

  return (
    <>
      {/* Stat cards */}
      <div className="stats-grid">
        <StatCard icon={TrendingUp}  iconColor="orange" value={stats?.revenue}   label="Total Revenue" />
        <StatCard icon={Package}     iconColor="blue"   value={stats?.orders}    label="Total Orders" />
        <StatCard icon={ShoppingBag} iconColor="green"  value={stats?.products}  label="Products" />
        <StatCard icon={Users}       iconColor="purple" value={stats?.customers} label="Customers" />
        <StatCard icon={CreditCard}  iconColor="orange" value={stats?.todayRev}  label="Today's Revenue" />
        <StatCard icon={BarChart2}   iconColor="blue"   value={stats?.avgOrder}  label="Avg Order Value" />
      </div>

      {/* Charts */}
      <div className="charts-row">
        <div className="card">
          <div className="card-header"><span className="card-title">Revenue — Last 7 Days</span></div>
          {revenueData && <Line data={revenueData} options={lineOptions} />}
        </div>
        <div className="card">
          <div className="card-header"><span className="card-title">Orders by Status</span></div>
          {statusData && <Doughnut data={statusData} options={doughnutOptions} />}
        </div>
      </div>

      {/* Bottom row */}
      <div style={{ display: 'grid', gridTemplateColumns: '3fr 2fr', gap: 20 }}>
        {/* Recent orders */}
        <div className="card">
          <div className="card-header">
            <span className="card-title">Recent Orders</span>
            <Link to="/orders" className="btn btn-secondary btn-sm">View All</Link>
          </div>
          <div className="table-responsive">
            <table>
              <thead>
                <tr>
                  <th>Order ID</th><th>Customer</th><th>Total</th><th>Status</th><th>Date</th>
                </tr>
              </thead>
              <tbody>
                {recentOrders?.length === 0 ? (
                  <tr><td colSpan={5} style={{ textAlign: 'center', padding: 24, color: 'var(--text-muted)' }}>No orders yet.</td></tr>
                ) : recentOrders?.map(o => (
                  <tr key={o.id}>
                    <td>
                      <button
                        className="btn-link fw-bold fs-12 text-accent"
                        style={{ background: 'none', border: 'none', padding: 0, cursor: 'pointer', textDecoration: 'underline' }}
                        onClick={() => setSelectedTx(o)}
                      >
                        #{o.id.slice(-6).toUpperCase()}
                      </button>
                    </td>
                    <td>{o.address?.name ?? 'Customer'}</td>
                    <td className="fw-bold">{fmt(o.totalAmount)}</td>
                    <td><StatusBadge status={normStatus(o.status)} /></td>
                    <td className="text-muted fs-12">
                      {getOrderDate(o)?.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) ?? '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Top products */}
        <div className="card">
          <div className="card-header"><span className="card-title">Top Products</span></div>
          {topProducts?.length === 0 ? (
            <p className="text-muted" style={{ textAlign: 'center', padding: 24 }}>No data yet.</p>
          ) : (
            <div>
              {topProducts?.map(([, v], i) => {
                const maxRev = topProducts[0]?.[1]?.total || 1;
                return (
                  <div key={i} style={{ marginBottom: 14 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                      <span style={{ fontSize: 13, fontWeight: 600 }}>{v.name}</span>
                      <span className="text-muted fs-12">{fmt(v.total)}</span>
                    </div>
                    <div style={{ height: 6, background: '#f1f5f9', borderRadius: 3 }}>
                      <div style={{ height: '100%', width: `${Math.round(v.total / maxRev * 100)}%`, background: 'var(--accent)', borderRadius: 3 }} />
                    </div>
                    <span className="fs-12 text-muted">{v.qty} sold</span>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* Transaction Detail Modal */}
      <Modal
        open={!!selectedTx}
        onClose={() => setSelectedTx(null)}
        title="Transaction Details"
        size="modal-lg"
        footer={<button className="btn btn-secondary" onClick={() => setSelectedTx(null)}>Close</button>}
      >
        {selectedTx && (
          <>
            <div style={{ marginBottom: 4, fontSize: 12, color: 'var(--text-muted)' }}>
              Transaction ID: <strong>#{selectedTx.id.toUpperCase()}</strong>
            </div>
            <div style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 20 }}>
              {getOrderDate(selectedTx)?.toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' }) ?? '—'}
            </div>

            {/* Customer */}
            <div className="order-detail-section">
              <h4>Customer</h4>
              <div className="detail-row"><span className="label">Name</span><span>{selectedTx.address?.name ?? '—'}</span></div>
              <div className="detail-row"><span className="label">Phone</span><span>{selectedTx.address?.phoneNumber ?? '—'}</span></div>
              <div className="detail-row"><span className="label">Address</span><span>
                {[selectedTx.address?.streetAddress || selectedTx.address?.street, selectedTx.address?.city, selectedTx.address?.state, selectedTx.address?.pincode || selectedTx.address?.zipCode].filter(Boolean).join(', ') || '—'}
              </span></div>
              {selectedTx.address?.landmark && (
                <div className="detail-row"><span className="label">Landmark</span><span>{selectedTx.address.landmark}</span></div>
              )}
            </div>

            {/* Items */}
            <div className="order-detail-section">
              <h4>Order Items</h4>
              <ul className="order-items-list">
                {(selectedTx.items ?? []).length === 0 ? (
                  <li style={{ padding: 12, color: 'var(--text-muted)' }}>No items</li>
                ) : (selectedTx.items ?? []).map((item, i) => (
                  <li key={i} className="order-item-row">
                    {item.productImageUrl
                      ? <img src={item.productImageUrl} className="order-item-img" alt={item.productName} onError={e => e.target.style.display = 'none'} />
                      : <div className="order-item-img" style={{ display: 'grid', placeItems: 'center' }}><Glasses size={22} color="var(--text-muted)" /></div>}
                    <div style={{ flex: 1 }}>
                      <div style={{ fontSize: 14, fontWeight: 600 }}>{item.productName ?? '—'}</div>
                      <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Qty: {item.quantity ?? 1} · ₹{(item.price ?? 0).toLocaleString('en-IN')} each</div>
                    </div>
                    <div className="fw-bold fs-13">{fmt((item.price ?? 0) * (item.quantity ?? 1))}</div>
                  </li>
                ))}
              </ul>
            </div>

            {/* Payment */}
            <div className="order-detail-section">
              <h4>Payment</h4>
              <div className="detail-row"><span className="label">Payment Method</span><span>{selectedTx.paymentMethod ?? '—'}</span></div>
              <div className="detail-row"><span className="label">Status</span><span><StatusBadge status={normStatus(selectedTx.status)} /></span></div>
              <div className="detail-total">
                <span>Total</span>
                <span className="text-accent">{fmt(selectedTx.totalAmount)}</span>
              </div>
            </div>
          </>
        )}
      </Modal>
    </>
  );
}

function PageLoader() {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', paddingTop: 80 }}>
      <div className="spinner" style={{ width: 32, height: 32, borderColor: 'rgba(0,0,0,.1)', borderTopColor: 'var(--accent)' }} />
    </div>
  );
}
