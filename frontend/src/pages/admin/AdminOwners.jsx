import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import api from '../../api/client.js';

export default function AdminOwners() {
  const [items, setItems] = useState([]);
  const [shops, setShops] = useState([]);
  const [form, setForm] = useState({ name: '', email: '', password: '', mobile: '', shopId: '' });
  const load = () => {
    api.get('/admin/shop-owners', { params: { size: 50 } }).then((r) => setItems(r.data.data?.items || []));
    api.get('/admin/shops', { params: { size: 50 } }).then((r) => setShops(r.data.data?.items || []));
  };
  useEffect(load, []);
  const save = async (e) => {
    e.preventDefault();
    try {
      await api.post('/admin/shop-owners', { ...form, shopId: form.shopId ? Number(form.shopId) : null });
      toast.success('Shop owner created');
      setForm({ name: '', email: '', password: '', mobile: '', shopId: '' });
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Create failed');
    }
  };
  const toggle = async (u) => {
    await api.put(`/admin/shop-owners/${u.id}`, { active: !u.active });
    load();
  };
  return (
    <div>
      <h2>Shop owners</h2>
      <form className="card stat-card p-4 mb-4" onSubmit={save}>
        <div className="row g-3">
          <div className="col-md-3"><input className="form-control" placeholder="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required /></div>
          <div className="col-md-3"><input className="form-control" placeholder="Email" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required /></div>
          <div className="col-md-2"><input className="form-control" placeholder="Password" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required /></div>
          <div className="col-md-2"><input className="form-control" placeholder="Mobile" value={form.mobile} onChange={(e) => setForm({ ...form, mobile: e.target.value })} /></div>
          <div className="col-md-2">
            <select className="form-select" value={form.shopId} onChange={(e) => setForm({ ...form, shopId: e.target.value })}>
              <option value="">Shop (optional)</option>
              {shops.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
          </div>
          <div className="col-12"><button className="btn btn-primary">Create owner</button></div>
        </div>
      </form>
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
