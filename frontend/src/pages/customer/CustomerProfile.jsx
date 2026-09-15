import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import api from '../../api/client.js';

export default function CustomerProfile() {
  const [form, setForm] = useState({ name: '', email: '', mobile: '' });
  useEffect(() => {
    api.get('/customers/profile').then((r) => setForm(r.data.data));
  }, []);
  const save = async (e) => {
    e.preventDefault();
    try {
      await api.put('/customers/profile', form);
      toast.success('Profile updated');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Update failed');
    }
  };
  return (
    <div className="col-lg-6">
      <h2>Profile</h2>
      <form className="card stat-card p-4" onSubmit={save}>
        <div className="mb-3"><label className="form-label">Name</label><input className="form-control" value={form.name || ''} onChange={(e) => setForm({ ...form, name: e.target.value })} /></div>
        <div className="mb-3"><label className="form-label">Email</label><input className="form-control" value={form.email || ''} disabled /></div>
        <div className="mb-3"><label className="form-label">Mobile</label><input className="form-control" value={form.mobile || ''} onChange={(e) => setForm({ ...form, mobile: e.target.value })} /></div>
        <button className="btn btn-primary">Save</button>
      </form>
    </div>
  );
}
