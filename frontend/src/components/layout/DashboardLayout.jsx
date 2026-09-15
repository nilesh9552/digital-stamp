import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext.jsx';

const MENUS = {
  CUSTOMER: [
    { to: '/customer/dashboard', icon: 'bi-grid', label: 'Dashboard' },
    { to: '/customer/loyalty-cards', icon: 'bi-credit-card-2-front', label: 'Loyalty cards' },
    { to: '/customer/rewards', icon: 'bi-gift', label: 'Rewards' },
    { to: '/customer/history', icon: 'bi-clock-history', label: 'History' },
    { to: '/customer/profile', icon: 'bi-person', label: 'Profile' },
  ],
  SHOP_OWNER: [
    { to: '/owner/dashboard', icon: 'bi-speedometer2', label: 'Overview' },
    { to: '/owner/scanner', icon: 'bi-qr-code-scan', label: 'QR Scanner' },
    { to: '/owner/customers', icon: 'bi-people', label: 'Customers' },
    { to: '/owner/loyalty', icon: 'bi-stars', label: 'Loyalty program' },
    { to: '/owner/rewards', icon: 'bi-gift', label: 'Rewards' },
    { to: '/owner/transactions', icon: 'bi-receipt', label: 'Transactions' },
    { to: '/owner/redemptions', icon: 'bi-trophy', label: 'Redemptions' },
  ],
  SUPER_ADMIN: [
    { to: '/admin/dashboard', icon: 'bi-speedometer2', label: 'Dashboard' },
    { to: '/admin/shops', icon: 'bi-shop', label: 'Shops' },
    { to: '/admin/shop-owners', icon: 'bi-person-badge', label: 'Shop owners' },
    { to: '/admin/customers', icon: 'bi-people', label: 'Customers' },
    { to: '/admin/statistics', icon: 'bi-graph-up', label: 'Statistics' },
  ],
};

export default function DashboardLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const items = MENUS[user?.role] || [];

  return (
    <div className="d-lg-flex">
      <aside className="sidebar p-4">
        <div className="text-white fw-bold fs-5 mb-4">Digital Stamp</div>
        <div className="text-secondary small mb-3">{user?.name}<br />{user?.role}</div>
        <nav className="d-flex flex-column gap-1">
          {items.map((item) => (
            <NavLink key={item.to} to={item.to}>
              <i className={`bi ${item.icon}`} /> {item.label}
            </NavLink>
          ))}
        </nav>
        <button
          className="btn btn-outline-light btn-sm mt-4"
          onClick={async () => {
            await logout();
            navigate('/login');
          }}
        >
          Logout
        </button>
      </aside>
      <main className="flex-grow-1 p-4 p-lg-5">
        <Outlet />
      </main>
    </div>
  );
}
