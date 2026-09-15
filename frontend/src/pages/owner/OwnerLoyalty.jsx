import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import api from '../../api/client.js';

export default function OwnerLoyalty() {
  const [form, setForm] = useState({ name: '', description: '', requiredStamps: 10, active: true });
  useEffect(() => {
    api.get('/owner/loyalty').then((r) => {
      const s = r.data.data;
      setForm({
        name: s.loyaltyProgramName || '',
        description: s.loyaltyProgramDescription || '',
        requiredStamps: s.requiredStamps || 10,
        active: s.loyaltyProgramActive,
      });
    });
  }, []);
  const save = async (e) => {
    e.preventDefault();
    try {
      await api.put('/owner/loyalty', form);
      toast.success('Loyalty program saved');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Save failed');
    }
  };
  return (
    <div className="col-lg-7">
      <h2>Loyalty program</h2>
      <form className="card stat-card p-4" onSubmit={save}>
        <div className="mb-3"><label className="form-label">Name</label><input className="form-control" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></div>
        <div className="mb-3"><label className="form-label">Description</label><textarea className="form-control" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></div>
        <div className="mb-3"><label className="form-label">Required stamps</label><input type="number" min="1" className="form-control" value={form.requiredStamps} onChange={(e) => setForm({ ...form, requiredStamps: Number(e.target.value) })} /></div>
        <div className="form-check mb-3">
          <input className="form-check-input" type="checkbox" checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} />
          <label className="form-check-label">Active</label>
        </div>
        <button className="btn btn-primary">Save</button>
      </form>
    </div>
  );
}
