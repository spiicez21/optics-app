import { useEffect, useState, useMemo } from 'react';
import {
  collection, getDocs, addDoc, updateDoc, deleteDoc, doc, query, orderBy, serverTimestamp,
} from 'firebase/firestore';
import { Search, Plus, Pencil, Trash2, Glasses } from 'lucide-react';
import { db } from '../lib/firebase.js';
import { useToast } from '../context/ToastContext.jsx';
import Modal from '../components/Modal.jsx';
import ConfirmDialog from '../components/ConfirmDialog.jsx';
import Pagination from '../components/Pagination.jsx';
import ImageUpload from '../components/ImageUpload.jsx';
import { StockBadge } from '../components/Badge.jsx';

const PAGE_SIZE = 12;

const EMPTY = {
  name: '', brand: '', price: '', originalPrice: '', stock: '', rating: '',
  categoryId: '', gender: '', frameShape: '', frameMaterial: '', lensType: '',
  frameColor: '', description: '', imageUrl: '', featured: false, isNew: false,
};

export default function Products() {
  const { showToast } = useToast();

  const [products,    setProducts]    = useState([]);
  const [categories,  setCategories]  = useState([]);
  const [loading,     setLoading]     = useState(true);
  const [search,      setSearch]      = useState('');
  const [filterCat,   setFilterCat]   = useState('');
  const [filterGender,setFilterGender]= useState('');
  const [page,        setPage]        = useState(1);

  // Modal state
  const [modalOpen,   setModalOpen]   = useState(false);
  const [editId,      setEditId]      = useState(null);
  const [form,        setForm]        = useState(EMPTY);
  const [saving,      setSaving]      = useState(false);

  // Delete state
  const [deleteTarget, setDeleteTarget] = useState(null); // { id, name }
  const [deleting,     setDeleting]     = useState(false);

  useEffect(() => { loadAll(); }, []);

  async function loadAll() {
    setLoading(true);
    try {
      const [prodSnap, catSnap] = await Promise.all([
        getDocs(query(collection(db, 'products'), orderBy('name'))),
        getDocs(collection(db, 'categories')),
      ]);
      const prods = []; prodSnap.forEach(d => prods.push({ id: d.id, ...d.data() }));
      const cats  = []; catSnap.forEach(d => cats.push({ id: d.id, ...d.data() }));
      cats.sort((a, b) => a.name?.localeCompare(b.name));
      setProducts(prods);
      setCategories(cats);
    } catch (e) { showToast('Failed to load products.', 'error'); }
    finally { setLoading(false); }
  }

  // ── Filtered & paginated ──
  const catMap  = useMemo(() => Object.fromEntries(categories.map(c => [c.id, c.name])), [categories]);
  const filtered = useMemo(() => products.filter(p => {
    const s = search.toLowerCase();
    const matchSearch = !s || p.name?.toLowerCase().includes(s) || p.brand?.toLowerCase().includes(s);
    const matchCat    = !filterCat    || p.categoryId === filterCat;
    const matchGender = !filterGender || p.gender === filterGender;
    return matchSearch && matchCat && matchGender;
  }), [products, search, filterCat, filterGender]);

  const totalPages  = Math.ceil(filtered.length / PAGE_SIZE);
  const pageItems   = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);
  const handleFilter = () => setPage(1);

  // ── Open add ──
  const openAdd = () => { setEditId(null); setForm(EMPTY); setModalOpen(true); };

  // ── Open edit ──
  const openEdit = p => {
    setEditId(p.id);
    setForm({
      name: p.name ?? '', brand: p.brand ?? '',
      price: p.price ?? '', originalPrice: p.originalPrice ?? '',
      stock: p.stock ?? '', rating: p.rating ?? '',
      categoryId: p.categoryId ?? '', gender: p.gender ?? '',
      frameShape: p.frameShape ?? '', frameMaterial: p.frameMaterial ?? '',
      lensType: p.lensType ?? '', frameColor: p.frameColor ?? '',
      description: p.description ?? '', imageUrl: p.imageUrl ?? (p.images?.[0] ?? ''),
      featured: !!p.featured, isNew: !!p.isNew,
    });
    setModalOpen(true);
  };

  // ── Save ──
  const handleSave = async () => {
    if (!form.name || !form.brand || form.price === '' || form.stock === '') {
      showToast('Fill in all required fields.', 'error'); return;
    }
    const catObj = categories.find(c => c.id === form.categoryId);
    const data = {
      name: form.name.trim(), brand: form.brand.trim(),
      price: parseFloat(form.price), stock: parseInt(form.stock),
      originalPrice: form.originalPrice ? parseFloat(form.originalPrice) : null,
      rating: form.rating ? parseFloat(form.rating) : 0,
      categoryId: form.categoryId || null, category: catObj?.name ?? '',
      gender: form.gender || null, frameShape: form.frameShape || null,
      frameMaterial: form.frameMaterial || null, lensType: form.lensType || null,
      frameColor: form.frameColor.trim() || null,
      description: form.description.trim() || '',
      images: form.imageUrl.trim() ? [form.imageUrl.trim()] : [],
      imageUrl: form.imageUrl.trim() || null,
      featured: form.featured, isNew: form.isNew,
    };
    setSaving(true);
    try {
      if (editId) { await updateDoc(doc(db, 'products', editId), data); showToast('Product updated!'); }
      else        { await addDoc(collection(db, 'products'), { ...data, createdAt: serverTimestamp() }); showToast('Product added!'); }
      setModalOpen(false);
      await loadAll();
    } catch (e) { showToast('Error: ' + e.message, 'error'); }
    finally { setSaving(false); }
  };

  // ── Delete ──
  const handleDelete = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await deleteDoc(doc(db, 'products', deleteTarget.id));
      showToast('Product deleted.', 'info');
      setDeleteTarget(null);
      await loadAll();
    } catch (e) { showToast('Error: ' + e.message, 'error'); }
    finally { setDeleting(false); }
  };

  const f = (k, v) => setForm(prev => ({ ...prev, [k]: v }));

  return (
    <>
      <div className="card">
        <div className="toolbar">
          <div className="toolbar-left">
            <div className="search-bar">
              <Search size={15} color="var(--text-muted)" />
              <input placeholder="Search products…" value={search}
                onChange={e => { setSearch(e.target.value); handleFilter(); }} />
            </div>
            <select className="form-control" style={{ maxWidth: 180 }}
              value={filterCat} onChange={e => { setFilterCat(e.target.value); handleFilter(); }}>
              <option value="">All Categories</option>
              {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
            <select className="form-control" style={{ maxWidth: 140 }}
              value={filterGender} onChange={e => { setFilterGender(e.target.value); handleFilter(); }}>
              <option value="">All Genders</option>
              {['Men','Women','Unisex','Kids'].map(g => <option key={g}>{g}</option>)}
            </select>
          </div>
          <div className="toolbar-right">
            <button className="btn btn-primary" onClick={openAdd} style={{ display:'flex', alignItems:'center', gap:6 }}><Plus size={16}/>Add Product</button>
          </div>
        </div>

        {loading ? <TableLoader cols={8} /> : (
          <>
            <div className="table-responsive">
              <table>
                <thead>
                  <tr>
                    <th>Image</th><th>Name / Brand</th><th>Category</th>
                    <th>Price</th><th>Stock</th><th>Gender</th><th>Featured</th><th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {pageItems.length === 0 ? (
                    <tr><td colSpan={8}><div className="empty-state"><Search size={40} color="var(--border)" style={{ marginBottom: 12 }} /><p>No products found.</p></div></td></tr>
                  ) : pageItems.map(p => (
                    <tr key={p.id}>
                      <td>
                        {(p.imageUrl || p.images?.[0])
                          ? <img src={p.imageUrl || p.images[0]} className="td-img" alt={p.name} onError={e => { e.target.style.display='none'; }} />
                          : <div className="td-img-placeholder"><Glasses size={20} color="var(--text-muted)" /></div>}
                      </td>
                      <td>
                        <div className="fw-bold fs-13">{p.name ?? '—'}</div>
                        <div className="text-muted fs-12">{p.brand ?? '—'}</div>
                      </td>
                      <td className="text-muted fs-13">{catMap[p.categoryId] ?? p.category ?? '—'}</td>
                      <td>
                        <div className="fw-bold">₹{(p.price ?? 0).toLocaleString('en-IN')}</div>
                        {p.originalPrice && <div className="text-muted fs-12" style={{ textDecoration: 'line-through' }}>₹{p.originalPrice.toLocaleString('en-IN')}</div>}
                      </td>
                      <td><StockBadge stock={p.stock} /></td>
                      <td className="text-muted fs-13">{p.gender ?? '—'}</td>
                      <td style={{ textAlign: 'center', color: p.featured ? 'var(--warning)' : 'var(--border)', fontSize: 18 }}>
                        {p.featured ? '★' : '—'}
                      </td>
                      <td>
                        <div style={{ display: 'flex', gap: 8 }}>
                          <button className="btn btn-icon btn-sm" title="Edit" onClick={() => openEdit(p)}><Pencil size={14} /></button>
                          <button className="btn btn-icon btn-sm" title="Delete" onClick={() => setDeleteTarget({ id: p.id, name: p.name })}><Trash2 size={14} color="var(--danger)" /></button>
                        </div>
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

      {/* Add / Edit Modal */}
      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editId ? 'Edit Product' : 'Add Product'}
        size="modal-lg"
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setModalOpen(false)} disabled={saving}>Cancel</button>
            <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
              {saving ? <span className="spinner" /> : 'Save Product'}
            </button>
          </>
        }
      >
        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Product Name *</label>
            <input className="form-control" placeholder="e.g. Classic Aviator" value={form.name} onChange={e => f('name', e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Brand *</label>
            <input className="form-control" placeholder="e.g. RayBan" value={form.brand} onChange={e => f('brand', e.target.value)} />
          </div>
        </div>
        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Price (₹) *</label>
            <input className="form-control" type="number" placeholder="0.00" min="0" step="0.01" value={form.price} onChange={e => f('price', e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Original Price (₹)</label>
            <input className="form-control" type="number" placeholder="0.00" min="0" step="0.01" value={form.originalPrice} onChange={e => f('originalPrice', e.target.value)} />
          </div>
        </div>
        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Stock *</label>
            <input className="form-control" type="number" placeholder="0" min="0" value={form.stock} onChange={e => f('stock', e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Rating</label>
            <input className="form-control" type="number" placeholder="0.0" min="0" max="5" step="0.1" value={form.rating} onChange={e => f('rating', e.target.value)} />
          </div>
        </div>
        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Category</label>
            <select className="form-control" value={form.categoryId} onChange={e => f('categoryId', e.target.value)}>
              <option value="">Select category…</option>
              {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Gender</label>
            <select className="form-control" value={form.gender} onChange={e => f('gender', e.target.value)}>
              <option value="">Select…</option>
              {['Men','Women','Unisex','Kids'].map(g => <option key={g}>{g}</option>)}
            </select>
          </div>
        </div>
        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Frame Shape</label>
            <select className="form-control" value={form.frameShape} onChange={e => f('frameShape', e.target.value)}>
              <option value="">Select…</option>
              {['Round','Square','Rectangle','Oval','Cat Eye','Aviator','Wayfarer','Browline'].map(s => <option key={s}>{s}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Frame Material</label>
            <select className="form-control" value={form.frameMaterial} onChange={e => f('frameMaterial', e.target.value)}>
              <option value="">Select…</option>
              {['Metal','Plastic','Acetate','Titanium','Wood','TR90'].map(s => <option key={s}>{s}</option>)}
            </select>
          </div>
        </div>
        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Lens Type</label>
            <select className="form-control" value={form.lensType} onChange={e => f('lensType', e.target.value)}>
              <option value="">Select…</option>
              {['Single Vision','Bifocal','Progressive','Polarized','Photochromic','Sunglasses','Clear'].map(s => <option key={s}>{s}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Frame Color</label>
            <input className="form-control" placeholder="e.g. Gold, Black" value={form.frameColor} onChange={e => f('frameColor', e.target.value)} />
          </div>
        </div>
        <div className="form-group">
          <label className="form-label">Description</label>
          <textarea className="form-control" rows={3} placeholder="Product description…" style={{ resize: 'vertical' }} value={form.description} onChange={e => f('description', e.target.value)} />
        </div>
        <div className="form-group">
          <label className="form-label">Product Image</label>
          <ImageUpload
            value={form.imageUrl}
            onChange={url => f('imageUrl', url)}
            path="admin/products"
          />
        </div>
        <div className="form-row">
          <div className="form-check">
            <input type="checkbox" id="fFeatured" checked={form.featured} onChange={e => f('featured', e.target.checked)} />
            <label htmlFor="fFeatured">Featured product</label>
          </div>
          <div className="form-check">
            <input type="checkbox" id="fIsNew" checked={form.isNew} onChange={e => f('isNew', e.target.checked)} />
            <label htmlFor="fIsNew">Mark as New</label>
          </div>
        </div>
      </Modal>

      {/* Delete confirm */}
      <ConfirmDialog
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        loading={deleting}
        title="Delete Product"
        message={`Are you sure you want to delete "${deleteTarget?.name}"? This cannot be undone.`}
      />
    </>
  );
}

function TableLoader({ cols }) {
  return (
    <div style={{ textAlign: 'center', padding: 48, color: 'var(--text-muted)' }}>
      <div className="spinner" style={{ width: 28, height: 28, borderColor: 'rgba(0,0,0,.1)', borderTopColor: 'var(--accent)', margin: '0 auto' }} />
    </div>
  );
}
