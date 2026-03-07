import { useEffect, useState, useMemo } from 'react';
import { collectionGroup, getDocs, updateDoc } from 'firebase/firestore';
import { Clock, CheckCircle2, Truck, PackageCheck, Search, Glasses } from 'lucide-react';
import { db } from '../lib/firebase.js';
import { useToast } from '../context/ToastContext.jsx';
import Modal from '../components/Modal.jsx';
import Pagination from '../components/Pagination.jsx';
import StatCard from '../components/StatCard.jsx';
import { StatusBadge } from '../components/Badge.jsx';

const PAGE_SIZE = 15;
const fmt = n => 'EGP ' + (n ?? 0).toLocaleString();

const STATUSES = ['pending','confirmed','processing','shipped','delivered','cancelled'];

export default function Orders() {
  const { showToast } = useToast();

  const [orders,       setOrders]       = useState([]);
  const [loading,      setLoading]      = useState(true);
  const [search,       setSearch]       = useState('');
  const [filterStatus, setFilterStatus] = useState('');
  const [filterDate,   setFilterDate]   = useState('all');
  const [page,         setPage]         = useState(1);

  // Detail modal
  const [selected,     setSelected]     = useState(null); // full order object
  const [newStatus,    setNewStatus]    = useState('');
  const [updating,     setUpdating]     = useState(false);

  useEffect(() => { load(); }, []);

  async function load() {
    setLoading(true);
    try {
      const snap = await getDocs(collectionGroup(db, 'orders'));
      const list = [];
      snap.forEach(d => list.push({ id: d.id, _ref: d.ref, ...d.data() }));
      list.sort((a, b) => (b.createdAt?.toDate?.()?.getTime() ?? 0) - (a.createdAt?.toDate?.()?.getTime() ?? 0));
      setOrders(list);
    } catch (e) {
      showToast('Failed to load orders. Check Firestore rules.', 'error');
      console.error(e);
    } finally { setLoading(false); }
  }

  // ── Status quick-count ──
  const counts = useMemo(() => {
    const c = { pending: 0, confirmed: 0, processing: 0, shipped: 0, delivered: 0, cancelled: 0 };
    orders.forEach(o => { const s = o.status ?? 'pending'; if (s in c) c[s]++; });
    return c;
  }, [orders]);

  // ── Filters ──
  const filtered = useMemo(() => {
    const now   = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const s     = search.toLowerCase();

    return orders.filter(o => {
      const name = (o.userName ?? o.userEmail ?? '').toLowerCase();
      const matchSearch = !s || o.id.toLowerCase().includes(s) || name.includes(s);
      const matchStatus = !filterStatus || (o.status ?? 'pending') === filterStatus;

      let matchDate = true;
      if (filterDate !== 'all') {
        const ts = o.createdAt?.toDate?.() ?? null;
        if (!ts) { matchDate = false; }
        else {
          const cutoff = new Date(today);
          if      (filterDate === 'today') { matchDate = ts >= today; }
          else if (filterDate === 'week')  { cutoff.setDate(cutoff.getDate() - 7);   matchDate = ts >= cutoff; }
          else if (filterDate === 'month') { cutoff.setMonth(cutoff.getMonth() - 1); matchDate = ts >= cutoff; }
        }
      }
      return matchSearch && matchStatus && matchDate;
    });
  }, [orders, search, filterStatus, filterDate]);

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const pageItems  = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);
  const resetPage  = () => setPage(1);

  // ── Open order detail ──
  const openOrder = o => {
    setSelected(o);
    setNewStatus(o.status ?? 'pending');
  };

  // ── Update status ──
  const handleUpdateStatus = async () => {
    if (!selected) return;
    setUpdating(true);
    try {
      await updateDoc(selected._ref, { status: newStatus });
      setOrders(prev => prev.map(o => o.id === selected.id ? { ...o, status: newStatus } : o));
      setSelected(prev => ({ ...prev, status: newStatus }));
      showToast(`Order status updated to "${newStatus}".`);
    } catch (e) { showToast('Error: ' + e.message, 'error'); }
    finally { setUpdating(false); }
  };

  // ── Address helper ──
  const formatAddress = addr => {
    if (!addr) return 'No address';
    if (typeof addr === 'string') return addr;
    return [addr.street ?? addr.line1, addr.city, addr.state ?? addr.governorate, addr.country].filter(Boolean).join(', ') || 'No address';
  };

  return (
    <>
      {/* Quick stats */}
      <div className="stats-grid" style={{ marginBottom: 24 }}>
        <StatCard icon={Clock}        iconColor="warning" value={counts.pending}                      label="Pending"   />
        <StatCard icon={CheckCircle2} iconColor="blue"   value={counts.confirmed + counts.processing} label="Confirmed" />
        <StatCard icon={Truck}        iconColor="orange"  value={counts.shipped}                      label="Shipped"   />
        <StatCard icon={PackageCheck} iconColor="green"   value={counts.delivered}                    label="Delivered" />
      </div>

      <div className="card">
        <div className="toolbar">
          <div className="toolbar-left">
            <div className="search-bar">
              <Search size={15} color="var(--text-muted)" />
              <input placeholder="Search order ID, name…" value={search}
                onChange={e => { setSearch(e.target.value); resetPage(); }} />
            </div>
            <select className="form-control" style={{ maxWidth: 170 }}
              value={filterStatus} onChange={e => { setFilterStatus(e.target.value); resetPage(); }}>
              <option value="">All Statuses</option>
              {STATUSES.map(s => <option key={s} value={s}>{s.charAt(0).toUpperCase() + s.slice(1)}</option>)}
            </select>
            <select className="form-control" style={{ maxWidth: 160 }}
              value={filterDate} onChange={e => { setFilterDate(e.target.value); resetPage(); }}>
              <option value="all">All Time</option>
              <option value="today">Today</option>
              <option value="week">This Week</option>
              <option value="month">This Month</option>
            </select>
          </div>
          <div className="toolbar-right">
            <span className="text-muted fs-13">{filtered.length} order{filtered.length !== 1 ? 's' : ''}</span>
          </div>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: 48 }}>
            <div className="spinner" style={{ width: 28, height: 28, borderColor: 'rgba(0,0,0,.1)', borderTopColor: 'var(--accent)', margin: '0 auto' }} />
          </div>
        ) : (
          <>
            <div className="table-responsive">
              <table>
                <thead>
                  <tr>
                    <th>Order ID</th><th>Customer</th><th>Items</th>
                    <th>Total</th><th>Status</th><th>Date</th><th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {pageItems.length === 0 ? (
                    <tr><td colSpan={7}><div className="empty-state"><PackageCheck size={44} color="var(--border)" style={{ marginBottom: 12 }} /><p>No orders found.</p></div></td></tr>
                  ) : pageItems.map(o => (
                    <tr key={o.id}>
                      <td><span className="fw-bold fs-12 text-accent">#{o.id.slice(-8).toUpperCase()}</span></td>
                      <td>
                        <div className="fw-bold fs-13">{o.userName ?? o.userEmail?.split('@')[0] ?? 'Customer'}</div>
                        <div className="text-muted fs-12">{o.userEmail ?? ''}</div>
                      </td>
                      <td className="text-muted fs-13">{(o.items ?? []).length} item{(o.items ?? []).length !== 1 ? 's' : ''}</td>
                      <td className="fw-bold">{fmt(o.total)}</td>
                      <td><StatusBadge status={o.status ?? 'pending'} /></td>
                      <td className="text-muted fs-12">
                        {o.createdAt?.toDate?.()?.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) ?? '—'}
                      </td>
                      <td>
                        <button className="btn btn-secondary btn-sm" onClick={() => openOrder(o)}>View</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination currentPage={page} totalPages={totalPages} onPage={p => { setPage(p); window.scrollTo(0, 0); }} />
          </>
        )}
      </div>

      {/* Order Detail Modal */}
      <Modal
        open={!!selected}
        onClose={() => setSelected(null)}
        title="Order Details"
        size="modal-lg"
        footer={<button className="btn btn-secondary" onClick={() => setSelected(null)}>Close</button>}
      >
        {selected && (
          <>
            <div style={{ marginBottom: 4, fontSize: 12, color: 'var(--text-muted)' }}>
              #{selected.id.slice(-8).toUpperCase()}
            </div>

            {/* Status update */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: 14, background: 'var(--bg)', borderRadius: 10, marginBottom: 20 }}>
              <span style={{ fontSize: 13, fontWeight: 600 }}>Update Status:</span>
              <select className="form-control" style={{ maxWidth: 200 }} value={newStatus} onChange={e => setNewStatus(e.target.value)}>
                {STATUSES.map(s => <option key={s} value={s}>{s.charAt(0).toUpperCase() + s.slice(1)}</option>)}
              </select>
              <button className="btn btn-primary btn-sm" onClick={handleUpdateStatus} disabled={updating}>
                {updating ? <span className="spinner" /> : 'Update'}
              </button>
            </div>

            {/* Customer */}
            <div className="order-detail-section">
              <h4>Customer</h4>
              <div className="detail-row"><span className="label">Name</span><span>{selected.userName ?? '—'}</span></div>
              <div className="detail-row"><span className="label">Email</span><span>{selected.userEmail ?? '—'}</span></div>
              <div className="detail-row"><span className="label">Phone</span><span>{selected.phone ?? selected.address?.phone ?? '—'}</span></div>
            </div>

            {/* Address */}
            <div className="order-detail-section">
              <h4>Delivery Address</h4>
              <p style={{ fontSize: 14, lineHeight: 1.7 }}>{formatAddress(selected.address ?? selected.shippingAddress)}</p>
            </div>

            {/* Items */}
            <div className="order-detail-section">
              <h4>Order Items</h4>
              <ul className="order-items-list">
                {(selected.items ?? []).length === 0 ? (
                  <li style={{ padding: 12, color: 'var(--text-muted)' }}>No items</li>
                ) : (selected.items ?? []).map((item, i) => (
                  <li key={i} className="order-item-row">
                    {item.imageUrl
                      ? <img src={item.imageUrl} className="order-item-img" alt={item.name} onError={e => e.target.style.display = 'none'} />
                      : <div className="order-item-img" style={{ display: 'grid', placeItems: 'center' }}><Glasses size={22} color="var(--text-muted)" /></div>}
                    <div style={{ flex: 1 }}>
                      <div style={{ fontSize: 14, fontWeight: 600 }}>{item.name ?? '—'}</div>
                      <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Qty: {item.quantity ?? 1}</div>
                    </div>
                    <div className="fw-bold fs-13">{fmt((item.price ?? 0) * (item.quantity ?? 1))}</div>
                  </li>
                ))}
              </ul>
            </div>

            {/* Payment */}
            <div className="order-detail-section">
              <h4>Payment</h4>
              <div className="detail-row"><span className="label">Subtotal</span><span>{fmt(selected.subtotal ?? selected.total)}</span></div>
              <div className="detail-row"><span className="label">Delivery Fee</span><span>{fmt(selected.deliveryFee ?? selected.shippingFee ?? 0)}</span></div>
              <div className="detail-row"><span className="label">Payment Method</span><span>{selected.paymentMethod ?? '—'}</span></div>
              <div className="detail-total">
                <span>Total</span>
                <span className="text-accent">{fmt(selected.total)}</span>
              </div>
            </div>

            {/* Note */}
            {(selected.note || selected.notes) && (
              <div className="order-detail-section">
                <h4>Customer Note</h4>
                <p style={{ fontSize: 14, color: 'var(--text-muted)' }}>{selected.note ?? selected.notes}</p>
              </div>
            )}
          </>
        )}
      </Modal>
    </>
  );
}
