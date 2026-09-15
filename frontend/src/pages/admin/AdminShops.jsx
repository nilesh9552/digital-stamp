import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import api from '../../api/client.js';

const empty = { name: '', slug: '', description: '', address: '', phone: '', email: '', openingHours: '', ownerId: '', requiredStamps: 10, active: true };

export default function AdminShops() {
  const [items, setItems] = useState([]);
  const [owners, setOwners] = useState([]);
  const [form, setForm] = useState(empty);
  const [editing, setEditing] = useState(null);

  const load = () => {
    api.get('/admin/shops', { params: { size: 50 } }).then((r) => setItems(r.data.data?.items || []));
    api.get('/admin/shop-owners', { params: { size: 50 } }).then((r) => setOwners(r.data.data?.items || []));
  };
  useEffect(load, []);

  const save = async (e) => {
    e.preventDefault();
    const payload = { ...form, ownerId: form.ownerId ? Number(form.ownerId) : null };
    try {
      if (editing) await api.put(`/admin/shops/${editing}`, payload);
      else await api.post('/admin/shops', payload);
      toast.success('Shop saved');
      setForm(empty);
      setEditing(null);
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Save failed');
    }
  };

  const act = async (id, path) => {
    await api.put(`/admin/shops/${id}/${path}`);
    load();
  };

  const remove = async (id) => {
    if (!window.confirm('Delete this shop and its loyalty data?')) return;
    await api.delete(`/admin/shops/${id}`);
    load();
  };

  return (
    <div>
      <h2>Shop management</h2>
      <form className="card stat-card p-4 mb-4" onSubmit={save}>
        <div className="row g-3">
          {['name', 'slug', 'address', 'phone', 'email'].map((f) => (
            <div className="col-md-4" key={f}>
              <input className="form-control" placeholder={f} value={form[f]} onChange={(e) => setForm({ ...form, [f]: e.target.value })} required={f === 'name' || f === 'slug'} />
            </div>
          ))}
          <div className="col-md-8">
            <input className="form-control" placeholder="Description" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </div>
          <div className="col-md-4">
            <select className="form-select" value={form.ownerId} onChange={(e) => setForm({ ...form, ownerId: e.target.value })}>
              <option value="">Assign owner</option>
              {owners.map((o) => <option key={o.id} value={o.id}>{o.name}</option>)}
            </select>
          </div>
          <div className="col-md-3">
            <input type="number" min="1" className="form-control" value={form.requiredStamps} onChange={(e) => setForm({ ...form, requiredStamps: Number(e.target.value) })} />
          </div>
          <div className="col-md-3">
            <button className="btn btn-primary w-100">{editing ? 'Update shop' : 'Create shop'}</button>
          </div>
        </div>
      </form>
      <div className="table-responsive card stat-card">
        <table className="table mb-0">
          <thead><tr><th>Shop</th><th>Slug</th><th>Owner</th><th>Status</th><th /></tr></thead>
          <tbody>
            {items.map((s) => (
              <tr key={s.id}>
                <td>{s.name}<div className="small text-muted">{s.address}</div></td>
                <td>{s.slug}</td>
                <td>{s.ownerName || '-'}</td>
                <td>{s.active ? 'Active' : 'Inactive'}</td>
                <td className="text-end">
                  <button className="btn btn-sm btn-outline-secondary me-1" onClick={() => { setEditing(s.id); setForm({ ...empty, ...s, ownerId: s.ownerId || '' }); }}>Edit</button>
                  {s.active
                    ? <button className="btn btn-sm btn-outline-warning me-1" onClick={() => act(s.id, 'deactivate')}>Deactivate</button>
                    : <button className="btn btn-sm btn-outline-success me-1" onClick={() => act(s.id, 'activate')}>Activate</button>}
                  <button className="btn btn-sm btn-outline-danger" onClick={() => remove(s.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
