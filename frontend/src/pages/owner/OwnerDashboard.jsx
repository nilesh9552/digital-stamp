import { useEffect, useState } from 'react';
import api from '../../api/client.js';

export default function OwnerDashboard() {
  const [stats, setStats] = useState(null);
  useEffect(() => {
    api.get('/owner/dashboard').then((r) => setStats(r.data.data));
  }, []);
  if (!stats) return <div>Loading dashboard...</div>;
  const cards = [
    ['Shop', stats.shopName],
    ['Customers', stats.totalCustomersForShop],
    ['Active cards', stats.activeLoyaltyCards],
    ['Stamps issued', stats.stampsIssued],
    ['Rewards redeemed', stats.rewardsRedeemed],
    ["Today's stamps", stats.todayStamps],
    ["Today's redemptions", stats.todayRedemptions],
  ];
  return (
    <div>
      <h2 className="mb-4">Shop overview</h2>
      <div className="row g-3">
        {cards.map(([label, value]) => (
          <div className="col-md-4 col-xl-3" key={label}>
            <div className="card stat-card p-4">
              <div className="text-muted small">{label}</div>
              <div className="fs-4 fw-semibold">{value ?? 0}</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
