import { NavLink, useNavigate } from 'react-router-dom';
import { LayoutDashboard, ShoppingBag, Tag, Package, LogOut, Glasses, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';

const NAV = [
  { to: '/dashboard',  icon: LayoutDashboard, label: 'Dashboard'  },
  { to: '/products',   icon: ShoppingBag,     label: 'Products'   },
  { to: '/categories', icon: Tag,             label: 'Categories' },
  { to: '/orders',     icon: Package,         label: 'Orders'     },
  { to: '/admins',     icon: ShieldCheck,     label: 'Admins'     },
];

export default function Sidebar() {
  const { logout } = useAuth();
  const navigate   = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <div className="logo-icon">
          <Glasses size={20} color="#fff" />
        </div>
        <span>OpticShop</span>
      </div>

      <nav className="sidebar-nav">
        <span className="nav-section-label">Main</span>
        {NAV.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
          >
            <Icon size={18} className="nav-icon" />
            {label}
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-footer">
        <button className="nav-link" onClick={handleLogout} style={{ width: '100%' }}>
          <LogOut size={18} className="nav-icon" />
          Logout
        </button>
      </div>
    </aside>
  );
}
