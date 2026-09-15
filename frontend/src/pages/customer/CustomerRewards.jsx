import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import api from '../../api/client.js';

export default function CustomerRewards() {
  const [rewards, setRewards] = useState([]);
  const [history, setHistory] = useState([]);
  const load = () => {
    api.get('/customers/rewards').then((r) => setRewards(r.data.data || []));
    api.get('/customers/redemptions').then((r) => setHistory(r.data.data?.items || []));
  };
  useEffect(load, []);
  const redeem = async (id) => {
    if (!window.confirm('Redeem this reward? Stamps will be consumed.')) return;
    try {
      const { data } = await api.post(`/rewards/${id}/redeem`);
      toast.success(`Redeemed: ${data.data.redemptionCode}`);
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Redemption failed');
    }
  };
  return (
    <div>
      <h2>Rewards</h2>
      <div className="row g-3 mb-4">
        {rewards.length === 0 && <p className="text-muted">No rewards available.</p>}
        {rewards.map((r) => (
          <div className="col-md-4" key={r.id}>
            <div className="card stat-card p-4 h-100">
              <h5>{r.name}</h5>
              <p className="small text-muted">{r.shopName} · {r.requiredStamps} stamps</p>
              <p>{r.description}</p>
              <button className="btn btn-primary" disabled={!r.eligible} onClick={() => redeem(r.id)}>
                {r.eligible ? 'Redeem' : 'Not eligible'}
              </button>
            </div>
          </div>
        ))}
      </div>
      <h4>Reward history</h4>
      <div className="table-responsive card stat-card">
        <table className="table mb-0">
          <thead><tr><th>Code</th><th>Reward</th><th>Shop</th><th>When</th></tr></thead>
          <tbody>
            {history.length === 0 && <tr><td colSpan="4" className="text-muted">No redemptions yet.</td></tr>}
            {history.map((h) => (
              <tr key={h.id}><td>{h.redemptionCode}</td><td>{h.rewardName}</td><td>{h.shopName}</td><td>{new Date(h.redeemedAt).toLocaleString()}</td></tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
