import { useEffect, useState } from 'react';
import { collection, getDocs, setDoc, deleteDoc, doc, serverTimestamp } from 'firebase/firestore';
import { initializeApp } from 'firebase/app';
import { getAuth, createUserWithEmailAndPassword, signOut as fbSignOut } from 'firebase/auth';
import { UserPlus, Trash2, ShieldCheck, Eye, EyeOff } from 'lucide-react';
import { db } from '../lib/firebase.js';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import Modal from '../components/Modal.jsx';

// Secondary Firebase app — creates users without signing out the current admin
const secondaryApp = initializeApp({
  apiKey:            import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain:        import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId:         import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket:     import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
}, 'admin-creator');
const secondaryAuth = getAuth(secondaryApp);

export default function Admins() {
  const { user: currentUser } = useAuth();
  const { showToast } = useToast();

  const [admins,   setAdmins]   = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [delTarget, setDelTarget] = useState(null);

  // Form state
  const [email,     setEmail]     = useState('');
  const [password,  setPassword]  = useState('');
  const [showPass,  setShowPass]  = useState(false);
  const [name,      setName]      = useState('');
  const [saving,    setSaving]    = useState(false);
  const [formError, setFormError] = useState('');

  useEffect(() => { load(); }, []);

  async function load() {
    setLoading(true);
    try {
      const snap = await getDocs(collection(db, 'admins'));
      const list = snap.docs.map(d => ({ id: d.id, ...d.data() }));
      list.sort((a, b) => (b.createdAt?.seconds ?? 0) - (a.createdAt?.seconds ?? 0));
      setAdmins(list);
    } catch (e) {
      showToast('Failed to load admins: ' + e.message, 'error');
    } finally {
      setLoading(false);
    }
  }

  async function handleAdd() {
    setFormError('');
    if (!email.trim())         return setFormError('Email is required.');
    if (password.length < 6)   return setFormError('Password must be at least 6 characters.');
    if (!name.trim())          return setFormError('Display name is required.');

    setSaving(true);
    try {
      // Create Firebase Auth account on secondary app (won't affect current session)
      const cred = await createUserWithEmailAndPassword(secondaryAuth, email.trim(), password);
      const uid  = cred.user.uid;

      // Sign out the secondary app immediately
      await fbSignOut(secondaryAuth);

      // Save admin record to Firestore
      await setDoc(doc(db, 'admins', uid), {
        uid,
        email:     email.trim(),
        name:      name.trim(),
        createdBy: currentUser?.email ?? 'unknown',
        createdAt: serverTimestamp(),
      });

      showToast(`Admin "${name.trim()}" created successfully.`);
      setEmail(''); setPassword(''); setName('');
      setShowForm(false);
      load();
    } catch (e) {
      const msg = e.code === 'auth/email-already-in-use'
        ? 'An account with this email already exists.'
        : e.message;
      setFormError(msg);
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!delTarget) return;
    try {
      await deleteDoc(doc(db, 'admins', delTarget.id));
      setAdmins(prev => prev.filter(a => a.id !== delTarget.id));
      showToast(`Admin "${delTarget.name ?? delTarget.email}" removed.`);
    } catch (e) {
      showToast('Failed to remove admin: ' + e.message, 'error');
    } finally {
      setDelTarget(null);
    }
  }

  const initials = str => (str ?? '?').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <div>
          <h2 style={{ margin: 0, fontSize: 20, fontWeight: 700 }}>Admin Accounts</h2>
          <p style={{ margin: '4px 0 0', fontSize: 13, color: 'var(--text-muted)' }}>
            Manage who has access to this dashboard.
          </p>
        </div>
        <button className="btn btn-primary" onClick={() => { setShowForm(true); setFormError(''); }}>
          <UserPlus size={15} style={{ marginRight: 6 }} />
          Add Admin
        </button>
      </div>

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        {loading ? (
          <div style={{ textAlign: 'center', padding: 48 }}>
            <div className="spinner" style={{ width: 28, height: 28, borderColor: 'rgba(0,0,0,.1)', borderTopColor: 'var(--accent)', margin: '0 auto' }} />
          </div>
        ) : admins.length === 0 ? (
          <div className="empty-state" style={{ padding: 48 }}>
            <ShieldCheck size={44} color="var(--border)" style={{ marginBottom: 12 }} />
            <p>No admins found.</p>
          </div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Admin</th>
                <th>Email</th>
                <th>Added By</th>
                <th>Date Added</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {admins.map(admin => (
                <tr key={admin.id}>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                      <div style={{
                        width: 36, height: 36, borderRadius: '50%',
                        background: 'var(--accent)', color: '#fff',
                        display: 'grid', placeItems: 'center',
                        fontSize: 13, fontWeight: 700, flexShrink: 0
                      }}>
                        {initials(admin.name ?? admin.email)}
                      </div>
                      <span style={{ fontWeight: 600, fontSize: 14 }}>{admin.name ?? '—'}</span>
                    </div>
                  </td>
                  <td className="text-muted fs-13">{admin.email}</td>
                  <td className="text-muted fs-13">{admin.createdBy ?? '—'}</td>
                  <td className="text-muted fs-12">
                    {admin.createdAt?.toDate?.().toLocaleDateString('en-US', {
                      month: 'short', day: 'numeric', year: 'numeric'
                    }) ?? '—'}
                  </td>
                  <td>
                    {admin.uid === currentUser?.uid ? (
                      <span style={{ fontSize: 11, color: 'var(--text-muted)', fontStyle: 'italic' }}>You</span>
                    ) : (
                      <button
                        className="btn btn-danger btn-sm"
                        onClick={() => setDelTarget(admin)}
                      >
                        <Trash2 size={13} style={{ marginRight: 4 }} />
                        Remove
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Add Admin Modal */}
      <Modal
        open={showForm}
        onClose={() => setShowForm(false)}
        title="Add New Admin"
        footer={
          <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end' }}>
            <button className="btn btn-secondary" onClick={() => setShowForm(false)}>Cancel</button>
            <button className="btn btn-primary" onClick={handleAdd} disabled={saving}>
              {saving ? <span className="spinner" /> : 'Create Admin'}
            </button>
          </div>
        }
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {formError && (
            <div style={{
              background: 'rgba(239,68,68,.1)', color: '#dc2626',
              padding: '10px 14px', borderRadius: 8, fontSize: 13
            }}>
              {formError}
            </div>
          )}
          <div>
            <label style={{ fontSize: 13, fontWeight: 600, display: 'block', marginBottom: 6 }}>
              Display Name
            </label>
            <input
              className="form-control"
              placeholder="e.g. John Doe"
              value={name}
              onChange={e => setName(e.target.value)}
            />
          </div>
          <div>
            <label style={{ fontSize: 13, fontWeight: 600, display: 'block', marginBottom: 6 }}>
              Email Address
            </label>
            <input
              className="form-control"
              type="email"
              placeholder="admin@example.com"
              value={email}
              onChange={e => setEmail(e.target.value)}
            />
          </div>
          <div>
            <label style={{ fontSize: 13, fontWeight: 600, display: 'block', marginBottom: 6 }}>
              Password
            </label>
            <div style={{ position: 'relative' }}>
              <input
                className="form-control"
                type={showPass ? 'text' : 'password'}
                placeholder="Min. 6 characters"
                value={password}
                onChange={e => setPassword(e.target.value)}
                style={{ paddingRight: 40 }}
              />
              <button
                onClick={() => setShowPass(p => !p)}
                style={{
                  position: 'absolute', right: 10, top: '50%', transform: 'translateY(-50%)',
                  background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)'
                }}
              >
                {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
          </div>
          <p style={{ fontSize: 12, color: 'var(--text-muted)', margin: 0 }}>
            The new admin will be able to log in with these credentials immediately.
          </p>
        </div>
      </Modal>

      {/* Confirm Delete Modal */}
      <Modal
        open={!!delTarget}
        onClose={() => setDelTarget(null)}
        title="Remove Admin"
        footer={
          <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end' }}>
            <button className="btn btn-secondary" onClick={() => setDelTarget(null)}>Cancel</button>
            <button className="btn btn-danger" onClick={handleDelete}>Remove</button>
          </div>
        }
      >
        <p style={{ fontSize: 14, margin: 0 }}>
          Remove <strong>{delTarget?.name ?? delTarget?.email}</strong> from the admin panel?
          They will lose dashboard access immediately.
        </p>
      </Modal>
    </>
  );
}
