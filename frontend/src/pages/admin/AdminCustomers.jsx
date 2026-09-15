import { useEffect, useState } from 'react';
import api from '../../api/client.js';

export default function AdminCustomers() {
  const [items, setItems] = useState([]);
  const [q, setQ] = useState('');
  const load = () => api.get('/admin/customers', { params: { q, size: 50 } }).then((r) => setItems(r.data.data?.items || []));
  useEffect(() => { load(); }, []);
  const toggle = async (u) => {
    await api.put(`/admin/customers/${u.id}`, { active: !u.active });
    load();
  };
  return (
    <div>
      <div className="d-flex justify-content-between mb-3">
        <h2>Customers</h2>
        <form className="d-flex gap-2" onSubmit={(e) => { e.preventDefault(); load(); }}>
          <input className="form-control" value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search" />
          <button className="btn btn-outline-primary">Search</button>
        </form>
      </div>
      <div className="table-responsive card stat-card">
        <table className="table mb-0">
          <thead><tr><th>Name</th><th>Email</th><th>Mobile</th><th>Status</th><th /></tr></thead>
          <tbody>
            {items.map((u) => (
              <tr key={u.id}>
                <td>{u.name}</td><td>{u.email}</td><td>{u.mobile}</td>
                <td>{u.active ? 'Active' : 'Inactive'}</td>
                <td><button className="btn btn-sm btn-outline-secondary" onClick={() => toggle(u)}>{u.active ? 'Deactivate' : 'Activate'}</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
