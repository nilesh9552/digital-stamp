import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import api from '../../api/client.js';

const empty = { name: '', description: '', requiredStamps: 10, active: true };

export default function OwnerRewards() {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(empty);
  const [editing, setEditing] = useState(null);
  const load = () => api.get('/owner/rewards').then((r) => setItems(r.data.data || []));
  useEffect(load, []);
  const save = async (e) => {
    e.preventDefault();
    try {
      if (editing) await api.put(`/owner/rewards/${editing}`, form);
      else await api.post('/owner/rewards', form);
      toast.success('Reward saved');
      setForm(empty);
      setEditing(null);
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Save failed');
    }
  };
  const remove = async (id) => {
    if (!window.confirm('Delete this reward?')) return;
    await api.delete(`/owner/rewards/${id}`);
    load();
  };
  return (
    <div>
      <h2>Rewards</h2>
      <form className="card stat-card p-4 mb-4" onSubmit={save}>
        <div className="row g-3">
          <div className="col-md-4"><input className="form-control" placeholder="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required /></div>
          <div className="col-md-4"><input className="form-control" placeholder="Description" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></div>
          <div className="col-md-2"><input type="number" min="1" className="form-control" value={form.requiredStamps} onChange={(e) => setForm({ ...form, requiredStamps: Number(e.target.value) })} /></div>
          <div className="col-md-2"><button className="btn btn-primary w-100">{editing ? 'Update' : 'Create'}</button></div>
        </div>
      </form>
      <div className="table-responsive card stat-card">
        <table className="table mb-0">
          <thead><tr><th>Name</th><th>Stamps</th><th>Active</th><th /></tr></thead>
          <tbody>
            {items.map((r) => (
              <tr key={r.id}>
                <td>{r.name}<div className="small text-muted">{r.description}</div></td>
                <td>{r.requiredStamps}</td>
                <td>{r.active ? 'Yes' : 'No'}</td>
                <td className="text-end">
                  <button className="btn btn-sm btn-outline-secondary me-2" onClick={() => { setEditing(r.id); setForm(r); }}>Edit</button>
                  <button className="btn btn-sm btn-outline-danger" onClick={() => remove(r.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
