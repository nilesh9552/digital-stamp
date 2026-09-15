import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import DashboardLayout from './components/layout/DashboardLayout.jsx';
import Home from './pages/public/Home.jsx';
import Login from './pages/public/Login.jsx';
import Register from './pages/public/Register.jsx';
import ShopPage from './pages/public/ShopPage.jsx';
import CustomerDashboard from './pages/customer/CustomerDashboard.jsx';
import CustomerProfile from './pages/customer/CustomerProfile.jsx';
import LoyaltyCards from './pages/customer/LoyaltyCards.jsx';
import CustomerRewards from './pages/customer/CustomerRewards.jsx';
import CustomerHistory from './pages/customer/CustomerHistory.jsx';
import OwnerDashboard from './pages/owner/OwnerDashboard.jsx';
import Scanner from './pages/owner/Scanner.jsx';
import OwnerCustomers from './pages/owner/OwnerCustomers.jsx';
import OwnerLoyalty from './pages/owner/OwnerLoyalty.jsx';
import OwnerRewards from './pages/owner/OwnerRewards.jsx';
import OwnerTransactions from './pages/owner/OwnerTransactions.jsx';
import OwnerRedemptions from './pages/owner/OwnerRedemptions.jsx';
import AdminDashboard from './pages/admin/AdminDashboard.jsx';
import AdminShops from './pages/admin/AdminShops.jsx';
import AdminOwners from './pages/admin/AdminOwners.jsx';
import AdminCustomers from './pages/admin/AdminCustomers.jsx';
import AdminStatistics from './pages/admin/AdminStatistics.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/shop/:shopSlug" element={<ShopPage />} />
      <Route path="/shop/:shopSlug/login" element={<Login />} />
      <Route path="/shop/:shopSlug/register" element={<Register />} />

      <Route element={<ProtectedRoute roles={['CUSTOMER']}><DashboardLayout /></ProtectedRoute>}>
        <Route path="/customer/dashboard" element={<CustomerDashboard />} />
        <Route path="/customer/profile" element={<CustomerProfile />} />
        <Route path="/customer/loyalty-cards" element={<LoyaltyCards />} />
        <Route path="/customer/rewards" element={<CustomerRewards />} />
        <Route path="/customer/history" element={<CustomerHistory />} />
      </Route>

      <Route element={<ProtectedRoute roles={['SHOP_OWNER']}><DashboardLayout /></ProtectedRoute>}>
        <Route path="/owner/dashboard" element={<OwnerDashboard />} />
        <Route path="/owner/scanner" element={<Scanner />} />
        <Route path="/owner/customers" element={<OwnerCustomers />} />
        <Route path="/owner/loyalty" element={<OwnerLoyalty />} />
        <Route path="/owner/rewards" element={<OwnerRewards />} />
        <Route path="/owner/transactions" element={<OwnerTransactions />} />
        <Route path="/owner/redemptions" element={<OwnerRedemptions />} />
      </Route>

      <Route element={<ProtectedRoute roles={['SUPER_ADMIN']}><DashboardLayout /></ProtectedRoute>}>
        <Route path="/admin/dashboard" element={<AdminDashboard />} />
        <Route path="/admin/shops" element={<AdminShops />} />
        <Route path="/admin/shop-owners" element={<AdminOwners />} />
        <Route path="/admin/customers" element={<AdminCustomers />} />
        <Route path="/admin/statistics" element={<AdminStatistics />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
