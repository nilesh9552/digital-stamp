import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import api from '../../api/client.js';
import { useAuth } from '../../context/AuthContext.jsx';

export default function ShopPage() {
  const { shopSlug } = useParams();
  const { isAuthenticated, role } = useAuth();
  const [payload, setPayload] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get(`/shops/slug/${shopSlug}`)
      .then((res) => setPayload(res.data.data))
      .catch((err) => setError(err.response?.data?.message || 'Shop not found'));
  }, [shopSlug]);

  const enroll = async () => {
    try {
      await api.post(`/shops/slug/${shopSlug}/enroll`);
      toast.success('You are now enrolled');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not enroll');
    }
  };

  if (error) {
    return <div className="container py-5"><div className="alert alert-danger">{error}</div></div>;
  }
  if (!payload) {
    return <div className="container py-5">Loading shop...</div>;
  }
  const shop = payload.shop;
  const rewards = payload.rewards || [];

  return (
    <div className="container py-5">
      <div className="glass-card p-4 p-md-5">
        <div className="d-flex flex-column flex-md-row gap-4 align-items-start">
          <div className="brand-gradient rounded-4 text-white d-flex align-items-center justify-content-center" style={{ width: 96, height: 96 }}>
            {shop.logo ? <img src={shop.logo} alt="" className="rounded-4" width="96" height="96" /> : <span className="fs-2">{shop.name?.[0]}</span>}
          </div>
          <div className="flex-grow-1">
            <h1>{shop.name}</h1>
            <p className="text-muted">{shop.description}</p>
            <p className="mb-1"><i className="bi bi-geo-alt" /> {shop.address}</p>
            <p className="mb-1"><i className="bi bi-telephone" /> {shop.phone}</p>
            <p className="mb-3"><i className="bi bi-clock" /> {shop.openingHours}</p>
            <div className="d-flex gap-2 flex-wrap">
              <Link className="btn btn-outline-primary" to={`/shop/${shopSlug}/login`}>Login</Link>
              <Link className="btn brand-gradient text-white" to={`/shop/${shopSlug}/register`}>Register</Link>
              {isAuthenticated && role === 'CUSTOMER' && (
                <button className="btn btn-success" onClick={enroll}>Join loyalty program</button>
              )}
            </div>
          </div>
        </div>
        <hr />
        <h5>Loyalty program</h5>
        <p>{shop.loyaltyProgramName} — collect {shop.requiredStamps} stamps to unlock rewards.</p>
        <h5 className="mt-4">Available rewards</h5>
        <div className="row g-3">
          {rewards.length === 0 && <p className="text-muted">No rewards yet.</p>}
          {rewards.map((r) => (
            <div className="col-md-4" key={r.id}>
              <div className="border rounded-4 p-3 h-100">
                <div className="fw-semibold">{r.name}</div>
                <div className="small text-muted">{r.description}</div>
                <span className="badge text-bg-light mt-2">{r.requiredStamps} stamps</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
