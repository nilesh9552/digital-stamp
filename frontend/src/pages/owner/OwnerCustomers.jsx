import { useEffect, useState } from 'react';
import api from '../../api/client.js';

export default function OwnerCustomers() {
  const [rows, setRows] = useState([]);
  const [q, setQ] = useState('');
  const load = () => api.get('/owner/customers', { params: { q } }).then((r) => setRows(r.data.data?.items || []));
  useEffect(() => { load(); }, []);
  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2>Customers</h2>
        <form className="d-flex gap-2" onSubmit={(e) => { e.preventDefault(); load(); }}>
          <input className="form-control" placeholder="Search name, email, mobile" value={q} onChange={(e) => setQ(e.target.value)} />
          <button className="btn btn-outline-primary">Search</button>
        </form>
      </div>
      <div className="table-responsive card stat-card">
        <table className="table mb-0">
          <thead>
            <tr><th>Customer</th><th>Email</th><th>Mobile</th><th>Current</th><th>Total</th><th>Last activity</th><th>Status</th></tr>
          </thead>
          <tbody>
            {rows.length === 0 && <tr><td colSpan="7" className="text-muted">No customers enrolled yet.</td></tr>}
            {rows.map((r) => (
              <tr key={r.loyaltyCardId}>
                <td>{r.name}</td><td>{r.email}</td><td>{r.mobile}</td>
                <td>{r.currentStamps}</td><td>{r.totalStamps}</td>
                <td>{r.lastActivity ? new Date(r.lastActivity).toLocaleString() : '-'}</td>
                <td><span className="badge text-bg-light">{r.status}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
