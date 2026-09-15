import { useEffect, useState } from 'react';
import api from '../../api/client.js';

export default function OwnerTransactions() {
  const [items, setItems] = useState([]);
  useEffect(() => {
    api.get('/owner/transactions').then((r) => setItems(r.data.data?.items || []));
  }, []);
  return (
    <div>
      <h2>Stamp transactions</h2>
      <div className="table-responsive card stat-card">
        <table className="table mb-0">
          <thead><tr><th>When</th><th>Customer</th><th>Type</th><th>Stamps</th><th>Notes</th></tr></thead>
          <tbody>
            {items.length === 0 && <tr><td colSpan="5" className="text-muted">No transactions yet.</td></tr>}
            {items.map((t) => (
              <tr key={t.id}>
                <td>{new Date(t.createdAt).toLocaleString()}</td>
                <td>{t.customerName}</td>
                <td>{t.transactionType}</td>
                <td>{t.stampsAdded}</td>
                <td>{t.description}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
