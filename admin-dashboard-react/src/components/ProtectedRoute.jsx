import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function ProtectedRoute() {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ minHeight: '100vh', display: 'grid', placeItems: 'center' }}>
        <div className="spinner" style={{ borderColor: 'rgba(0,0,0,.15)', borderTopColor: 'var(--accent)', width: 36, height: 36 }} />
      </div>
    );
  }

  return user ? <Outlet /> : <Navigate to="/login" replace />;
}
