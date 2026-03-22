import { useEffect, useState } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { doc, getDoc } from 'firebase/firestore';
import { signOut } from 'firebase/auth';
import { useAuth } from '../context/AuthContext.jsx';
import { auth, db } from '../lib/firebase.js';

export default function ProtectedRoute() {
  const { user, loading } = useAuth();
  const [checking, setChecking] = useState(true);
  const [isAdmin,  setIsAdmin]  = useState(false);

  useEffect(() => {
    if (loading) return;
    if (!user) { setChecking(false); return; }

    getDoc(doc(db, 'admins', user.uid))
      .then(snap => {
        if (snap.exists()) {
          setIsAdmin(true);
        } else {
          // Not in admins collection — sign out to break redirect loop
          signOut(auth);
          setIsAdmin(false);
        }
      })
      .catch(() => {
        // Firestore read failed (e.g. rules) — let through to avoid lockout
        setIsAdmin(true);
      })
      .finally(() => setChecking(false));
  }, [user, loading]);

  if (loading || checking) {
    return (
      <div style={{ minHeight: '100vh', display: 'grid', placeItems: 'center' }}>
        <div className="spinner" style={{ borderColor: 'rgba(0,0,0,.15)', borderTopColor: 'var(--accent)', width: 36, height: 36 }} />
      </div>
    );
  }

  if (!user || !isAdmin) return <Navigate to="/login" replace />;
  return <Outlet />;
}
