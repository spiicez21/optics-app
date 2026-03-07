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
import { TrendingUp, Package, ShoppingBag, Users } from 'lucide-react';
import { db } from '../lib/firebase.js';
import StatCard from '../components/StatCard.jsx';
import { StatusBadge } from '../components/Badge.jsx';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, ArcElement, Tooltip, Legend, Filler);

const fmt = n => n != null ? 'EGP ' + n.toLocaleString('en-US', { maximumFractionDigits: 0 }) : '—';

export default function Dashboard() {
  const [stats,         setStats]         = useState(null);
  const [revenueData,   setRevenueData]   = useState(null);
  const [statusData,    setStatusData]    = useState(null);
  const [recentOrders,  setRecentOrders]  = useState(null);
  const [topProducts,   setTopProducts]   = useState(null);
  const [loading,       setLoading]       = useState(true);

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
      orderSnap.forEach(d => {
        const o = { id: d.id, _ref: d.ref, ...d.data() };
        totalRevenue += o.total ?? 0;
        orders.push(o);
      });
      orders.sort((a, b) => (b.createdAt?.toDate?.()?.getTime() ?? 0) - (a.createdAt?.toDate?.()?.getTime() ?? 0));

      setStats({
        revenue:   fmt(totalRevenue),
        orders:    orders.length,
        products:  productCount.data().count,
        customers: userCount.data().count,
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
        const ts = o.createdAt?.toDate?.() ?? null;
        if (!ts) return;
        for (let i = 0; i < days.length; i++) {
          const next = new Date(days[i]); next.setDate(next.getDate() + 1);
          if (ts >= days[i] && ts < next) { values[i] += o.total ?? 0; break; }
        }
      });
      setRevenueData({ labels, datasets: [{ label: 'Revenue (EGP)', data: values, borderColor: '#FF6B35', backgroundColor: 'rgba(255,107,53,.1)', borderWidth: 2.5, pointBackgroundColor: '#FF6B35', pointRadius: 4, tension: .4, fill: true }] });

      // ── Status doughnut ──
      const counts = { pending: 0, confirmed: 0, shipped: 0, delivered: 0, cancelled: 0 };
      orders.forEach(o => { const s = o.status ?? 'pending'; if (s in counts) counts[s]++; else counts.pending++; });
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
          const pid = item.productId ?? item.id ?? item.name;
          if (!pid) return;
          if (!revMap[pid]) revMap[pid] = { name: item.name ?? pid, total: 0, qty: 0 };
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
      y: { grid: { color: '#f1f5f9' }, beginAtZero: true, ticks: { callback: v => 'EGP ' + v.toLocaleString() } },
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
                    <td><span className="fw-bold fs-12 text-accent">#{o.id.slice(-6).toUpperCase()}</span></td>
                    <td>{o.userName ?? o.userEmail?.split('@')[0] ?? 'Customer'}</td>
                    <td className="fw-bold">{fmt(o.total)}</td>
                    <td><StatusBadge status={o.status ?? 'pending'} /></td>
                    <td className="text-muted fs-12">
                      {o.createdAt?.toDate?.()?.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) ?? '—'}
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
