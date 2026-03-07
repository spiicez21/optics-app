import { useEffect, useState, useMemo } from 'react';
import { collection, getDocs, addDoc, updateDoc, deleteDoc, doc, query, orderBy } from 'firebase/firestore';
import { Search, Plus, Pencil, Trash2, Tag } from 'lucide-react';
import { db } from '../lib/firebase.js';
import { useToast } from '../context/ToastContext.jsx';
import Modal from '../components/Modal.jsx';
import ConfirmDialog from '../components/ConfirmDialog.jsx';
import ImageUpload from '../components/ImageUpload.jsx';

const EMPTY = { name: '', description: '', icon: '', imageUrl: '', featured: false };

export default function Categories() {
  const { showToast } = useToast();

  const [categories,   setCategories]   = useState([]);
  const [loading,      setLoading]      = useState(true);
  const [search,       setSearch]       = useState('');
  const [modalOpen,    setModalOpen]    = useState(false);
  const [editId,       setEditId]       = useState(null);
  const [form,         setForm]         = useState(EMPTY);
  const [saving,       setSaving]       = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting,     setDeleting]     = useState(false);

  useEffect(() => { load(); }, []);

  async function load() {
    setLoading(true);
    try {
      const snap = await getDocs(query(collection(db, 'categories'), orderBy('name')));
      const cats = []; snap.forEach(d => cats.push({ id: d.id, ...d.data() }));
      setCategories(cats);
    } catch (e) { showToast('Failed to load categories.', 'error'); }
    finally { setLoading(false); }
  }

  const filtered = useMemo(() =>
    categories.filter(c => !search || c.name?.toLowerCase().includes(search.toLowerCase())),
    [categories, search]
  );

  const openAdd  = () => { setEditId(null); setForm(EMPTY); setModalOpen(true); };
  const openEdit = c  => { setEditId(c.id); setForm({ name: c.name ?? '', description: c.description ?? '', icon: c.icon ?? '', imageUrl: c.imageUrl ?? '', featured: !!c.featured }); setModalOpen(true); };
  const f = (k, v) => setForm(prev => ({ ...prev, [k]: v }));

  const handleSave = async () => {
    if (!form.name.trim()) { showToast('Category name is required.', 'error'); return; }
    const data = { name: form.name.trim(), description: form.description.trim() || '', icon: form.icon.trim() || '🏷️', imageUrl: form.imageUrl.trim() || null, featured: form.featured };
    setSaving(true);
    try {
      if (editId) { await updateDoc(doc(db, 'categories', editId), data); showToast('Category updated!'); }
      else        { await addDoc(collection(db, 'categories'), data);     showToast('Category added!'); }
      setModalOpen(false); await load();
    } catch (e) { showToast('Error: ' + e.message, 'error'); }
    finally { setSaving(false); }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await deleteDoc(doc(db, 'categories', deleteTarget.id));
      showToast('Category deleted.', 'info');
      setDeleteTarget(null); await load();
    } catch (e) { showToast('Error: ' + e.message, 'error'); }
    finally { setDeleting(false); }
  };

  return (
    <>
      <div className="card">
        <div className="toolbar">
          <div className="toolbar-left">
            <div className="search-bar">
              <Search size={15} color="var(--text-muted)" />
              <input placeholder="Search categories…" value={search} onChange={e => setSearch(e.target.value)} />
            </div>
          </div>
          <div className="toolbar-right">
            <button className="btn btn-primary" onClick={openAdd} style={{ display:'flex', alignItems:'center', gap:6 }}><Plus size={16}/>Add Category</button>
          </div>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: 48, color: 'var(--text-muted)' }}>
            <div className="spinner" style={{ width: 28, height: 28, borderColor: 'rgba(0,0,0,.1)', borderTopColor: 'var(--accent)', margin: '0 auto' }} />
          </div>
        ) : filtered.length === 0 ? (
          <div className="empty-state">
            <Tag size={44} color="var(--border)" style={{ marginBottom: 12 }} />
            <p>No categories found.</p>
          </div>
        ) : (
          <div className="cat-grid">
            {filtered.map(c => (
              <div key={c.id} className="cat-card">
                <div className="cat-card-img">
                  {c.imageUrl
                    ? <img src={c.imageUrl} alt={c.name} onError={e => { e.target.style.display = 'none'; }} />
                    : <Tag size={40} color="var(--text-muted)" />}
                </div>
                <div className="cat-card-body">
                  <div className="cat-card-name">{c.name}</div>
                  <div className="cat-card-desc">
                    {c.description ? c.description.slice(0, 50) + (c.description.length > 50 ? '…' : '') : 'No description'}
                  </div>
                  {c.featured && <span className="badge badge-orange" style={{ marginBottom: 8, display: 'inline-block' }}>Featured</span>}
                  <div className="cat-card-actions">
                    <button className="btn btn-secondary btn-sm" style={{ flex: 1, display:'flex', alignItems:'center', gap:5 }} onClick={() => openEdit(c)}><Pencil size={13}/>Edit</button>
                    <button className="btn btn-icon btn-sm" title="Delete" onClick={() => setDeleteTarget({ id: c.id, name: c.name })}><Trash2 size={14} color="var(--danger)" /></button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add / Edit Modal */}
      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editId ? 'Edit Category' : 'Add Category'}
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setModalOpen(false)} disabled={saving}>Cancel</button>
            <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
              {saving ? <span className="spinner" /> : 'Save Category'}
            </button>
          </>
        }
      >
        <div className="form-group">
          <label className="form-label">Category Name *</label>
          <input className="form-control" placeholder="e.g. Sunglasses" value={form.name} onChange={e => f('name', e.target.value)} />
        </div>
        <div className="form-group">
          <label className="form-label">Description</label>
          <textarea className="form-control" rows={2} placeholder="Brief description…" style={{ resize: 'vertical' }} value={form.description} onChange={e => f('description', e.target.value)} />
        </div>
        <div className="form-group">
          <label className="form-label">Category Image</label>
          <ImageUpload
            value={form.imageUrl}
            onChange={url => f('imageUrl', url)}
            path="admin/categories"
            height={120}
          />
        </div>
        <div className="form-check">
          <input type="checkbox" id="cFeatured" checked={form.featured} onChange={e => f('featured', e.target.checked)} />
          <label htmlFor="cFeatured">Show on home / featured</label>
        </div>
      </Modal>

      {/* Delete confirm */}
      <ConfirmDialog
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        loading={deleting}
        title="Delete Category"
        message={`Delete "${deleteTarget?.name}"? Products in this category won't be deleted but will lose their category assignment.`}
      />
    </>
  );
}
