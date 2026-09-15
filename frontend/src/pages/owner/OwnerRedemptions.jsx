import { useEffect, useState } from 'react';
import api from '../../api/client.js';

export default function OwnerRedemptions() {
  const [items, setItems] = useState([]);
  useEffect(() => {
    api.get('/owner/redemptions').then((r) => setItems(r.data.data?.items || []));
  }, []);
  return (
    <div>
      <h2>Redemptions</h2>
      <div className="table-responsive card stat-card">
        <table className="table mb-0">
          <thead><tr><th>Code</th><th>Customer</th><th>Reward</th><th>When</th><th>Status</th></tr></thead>
          <tbody>
            {items.length === 0 && <tr><td colSpan="5" className="text-muted">No redemptions yet.</td></tr>}
            {items.map((t) => (
              <tr key={t.id}>
                <td>{t.redemptionCode}</td>
                <td>{t.customerName}</td>
                <td>{t.rewardName}</td>
                <td>{new Date(t.redeemedAt).toLocaleString()}</td>
                <td>{t.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
