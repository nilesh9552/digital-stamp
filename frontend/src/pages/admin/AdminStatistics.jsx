import { useEffect, useState } from 'react';
import api from '../../api/client.js';

export default function AdminStatistics() {
  const [rows, setRows] = useState([]);
  const [tx, setTx] = useState([]);
  const [redemptions, setRedemptions] = useState([]);
  useEffect(() => {
    api.get('/admin/statistics').then((r) => setRows(r.data.data || []));
    api.get('/admin/transactions').then((r) => setTx(r.data.data?.items || []));
    api.get('/admin/redemptions').then((r) => setRedemptions(r.data.data?.items || []));
  }, []);
  return (
    <div>
      <h2>Statistics</h2>
      <div className="table-responsive card stat-card mb-4">
        <table className="table mb-0">
          <thead><tr><th>Shop</th><th>Customers</th><th>Stamps</th><th>Redemptions</th><th>Status</th></tr></thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.shopId}>
                <td>{r.shopName}<div className="small text-muted">{r.slug}</div></td>
                <td>{r.customers}</td>
                <td>{r.stamps}</td>
                <td>{r.redemptions}</td>
                <td>{r.active ? 'Active' : 'Inactive'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="row g-4">
        <div className="col-lg-6">
          <h5>Recent stamp activity</h5>
          <div className="table-responsive card stat-card">
            <table className="table mb-0">
              <thead><tr><th>Shop</th><th>Customer</th><th>Type</th><th>Qty</th></tr></thead>
              <tbody>
                {tx.slice(0, 12).map((t) => (
                  <tr key={t.id}><td>{t.shopName}</td><td>{t.customerName}</td><td>{t.transactionType}</td><td>{t.stampsAdded}</td></tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
        <div className="col-lg-6">
          <h5>Recent redemptions</h5>
          <div className="table-responsive card stat-card">
            <table className="table mb-0">
              <thead><tr><th>Shop</th><th>Customer</th><th>Reward</th></tr></thead>
              <tbody>
                {redemptions.slice(0, 12).map((t) => (
                  <tr key={t.id}><td>{t.shopName}</td><td>{t.customerName}</td><td>{t.rewardName}</td></tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
