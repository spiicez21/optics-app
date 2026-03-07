import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { Glasses, Mail, Lock, LogIn } from 'lucide-react';
import { auth } from '../lib/firebase.js';
import { useAuth } from '../context/AuthContext.jsx';

const ERROR_MAP = {
  'auth/user-not-found':    'No account found with this email.',
  'auth/wrong-password':    'Incorrect password.',
  'auth/invalid-email':     'Invalid email address.',
  'auth/too-many-requests': 'Too many attempts. Try again later.',
  'auth/invalid-credential':'Invalid credentials.',
};

export default function Login() {
  const { user, loading } = useAuth();
  const navigate = useNavigate();

  const [email,    setEmail]    = useState('');
  const [password, setPassword] = useState('');
  const [error,    setError]    = useState('');
  const [busy,     setBusy]     = useState(false);

  useEffect(() => {
    if (!loading && user) navigate('/dashboard', { replace: true });
  }, [user, loading, navigate]);

  const handleLogin = async e => {
    e?.preventDefault();
    setError('');
    if (!email || !password) { setError('Please enter your email and password.'); return; }
    setBusy(true);
    try {
      await signInWithEmailAndPassword(auth, email, password);
      navigate('/dashboard', { replace: true });
    } catch (err) {
      setError(ERROR_MAP[err.code] ?? 'Sign-in failed. Please try again.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-logo">
          <div className="logo-icon">
            <Glasses size={22} color="#fff" />
          </div>
          <span>OpticShop</span>
        </div>

        <h2>Admin Portal</h2>
        <p>Sign in to manage your store</p>

        <form onSubmit={handleLogin}>
          <div className="form-group">
            <label className="form-label" htmlFor="email">Email Address</label>
            <div style={{ position: 'relative' }}>
              <Mail size={16} style={{ position: 'absolute', left: 13, top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)', pointerEvents: 'none' }} />
              <input
                id="email" type="email" className="form-control"
                style={{ paddingLeft: 38 }}
                placeholder="admin@opticshop.com" autoComplete="email"
                value={email} onChange={e => setEmail(e.target.value)}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">Password</label>
            <div style={{ position: 'relative' }}>
              <Lock size={16} style={{ position: 'absolute', left: 13, top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)', pointerEvents: 'none' }} />
              <input
                id="password" type="password" className="form-control"
                style={{ paddingLeft: 38 }}
                placeholder="••••••••" autoComplete="current-password"
                value={password} onChange={e => setPassword(e.target.value)}
              />
            </div>
          </div>

          <button type="submit" className="login-btn" disabled={busy}
            style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8 }}>
            {busy ? <span className="spinner" /> : <><LogIn size={17} />Sign In</>}
          </button>
        </form>

        <p className="login-error">{error}</p>
      </div>
    </div>
  );
}
