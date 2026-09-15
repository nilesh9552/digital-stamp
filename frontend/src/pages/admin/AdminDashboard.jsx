import { useEffect, useState } from 'react';
import api from '../../api/client.js';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  useEffect(() => {
    api.get('/admin/dashboard').then((r) => setStats(r.data.data));
  }, []);
  if (!stats) return <div>Loading...</div>;
  const cards = [
    ['Total shops', stats.totalShops],
    ['Active shops', stats.activeShops],
    ['Customers', stats.totalCustomers],
    ['Shop owners', stats.totalShopOwners],
    ['Stamps issued', stats.totalStamps],
    ['Rewards redeemed', stats.totalRewardsRedeemed],
  ];
  return (
    <div>
      <h2 className="mb-4">Platform dashboard</h2>
      <div className="row g-3">
        {cards.map(([label, value]) => (
          <div className="col-md-4" key={label}>
            <div className="card stat-card p-4">
              <div className="text-muted small">{label}</div>
              <div className="fs-3 fw-semibold">{value}</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
