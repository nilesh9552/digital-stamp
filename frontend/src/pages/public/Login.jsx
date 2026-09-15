import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { useAuth } from '../../context/AuthContext.jsx';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const { shopSlug } = useParams();
  const [form, setForm] = useState({ email: '', password: '' });
  const [loading, setLoading] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const user = await login(form.email, form.password);
      toast.success('Welcome back');
      if (user.role === 'SUPER_ADMIN') navigate('/admin/dashboard');
      else if (user.role === 'SHOP_OWNER') navigate('/owner/dashboard');
      else navigate(shopSlug ? `/shop/${shopSlug}` : '/customer/dashboard');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Invalid login');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="hero-panel d-flex align-items-center">
      <div className="container py-5">
        <div className="row justify-content-center">
          <div className="col-md-5">
            <form className="glass-card p-4 p-md-5" onSubmit={onSubmit}>
              <h3 className="mb-1">Sign in</h3>
              <p className="text-muted">Access your Digital Stamp workspace.</p>
              <div className="mb-3">
                <label className="form-label">Email</label>
                <input className="form-control" type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
              </div>
              <div className="mb-4">
                <label className="form-label">Password</label>
                <input className="form-control" type="password" required value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
              </div>
              <button className="btn brand-gradient text-white w-100" disabled={loading}>{loading ? 'Signing in...' : 'Login'}</button>
              <p className="small mt-3 mb-0">
                New customer? <Link to={shopSlug ? `/shop/${shopSlug}/register` : '/register'}>Create an account</Link>
              </p>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
