import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { useAuth } from '../../context/AuthContext.jsx';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const { shopSlug } = useParams();
  const [form, setForm] = useState({ name: '', email: '', password: '', mobile: '' });
  const [loading, setLoading] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await register({ ...form, shopSlug });
      toast.success('Account created');
      navigate(shopSlug ? `/customer/dashboard` : '/customer/dashboard');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="hero-panel d-flex align-items-center">
      <div className="container py-5">
        <div className="row justify-content-center">
          <div className="col-md-6">
            <form className="glass-card p-4 p-md-5" onSubmit={onSubmit}>
              <h3>Create customer account</h3>
              {shopSlug && <p className="text-muted">You will be enrolled in {shopSlug}.</p>}
              {['name', 'email', 'mobile', 'password'].map((field) => (
                <div className="mb-3" key={field}>
                  <label className="form-label text-capitalize">{field}</label>
                  <input
                    className="form-control"
                    type={field === 'password' ? 'password' : field === 'email' ? 'email' : 'text'}
                    required
                    value={form[field]}
                    onChange={(e) => setForm({ ...form, [field]: e.target.value })}
                  />
                </div>
              ))}
              <button className="btn brand-gradient text-white w-100" disabled={loading}>{loading ? 'Creating...' : 'Register'}</button>
              <p className="small mt-3 mb-0">Already have an account? <Link to={shopSlug ? `/shop/${shopSlug}/login` : '/login'}>Login</Link></p>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
