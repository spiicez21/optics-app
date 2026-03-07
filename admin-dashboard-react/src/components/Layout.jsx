import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from './Sidebar.jsx';
import { useAuth } from '../context/AuthContext.jsx';

const PAGE_TITLES = {
  '/dashboard':  'Dashboard',
  '/products':   'Products',
  '/categories': 'Categories',
  '/orders':     'Orders',
};

export default function Layout() {
  const { user } = useAuth();
  const { pathname } = useLocation();
  const title = PAGE_TITLES[pathname] ?? 'Admin';

  return (
    <div className="layout">
      <Sidebar />
      <div className="main-wrapper">
        <header className="topbar">
          <span className="topbar-title">{title}</span>
          <div className="topbar-right">
            <span style={{ fontSize: 13, color: 'var(--text-muted)' }}>{user?.email}</span>
            <div className="admin-avatar">
              {(user?.email?.[0] ?? 'A').toUpperCase()}
            </div>
          </div>
        </header>
        <main className="page-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
