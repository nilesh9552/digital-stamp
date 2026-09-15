import { Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import api from '../../api/client.js';

export default function Home() {
  const [shops, setShops] = useState([]);
  useEffect(() => {
    api.get('/shops/public').then((res) => setShops(res.data.data || [])).catch(() => setShops([]));
  }, []);

  return (
    <div>
      <section className="hero-panel text-white">
        <nav className="container py-4 d-flex justify-content-between align-items-center">
          <strong>Digital Stamp</strong>
          <div className="d-flex gap-2">
            <Link className="btn btn-outline-light" to="/login">Login</Link>
            <Link className="btn btn-light" to="/register">Register</Link>
          </div>
        </nav>
        <div className="container py-5">
          <div className="row align-items-center g-5">
            <div className="col-lg-6">
              <p className="text-uppercase small text-info mb-2">Multi-shop loyalty SaaS</p>
              <h1 className="display-4 fw-bold">Replace paper stamp cards with a digital QR wallet.</h1>
              <p className="lead text-white-50 mt-3">
                Customers collect stamps across independent cafes. Shop owners scan a QR code, add stamps, and issue rewards — all from one platform.
              </p>
              <Link to="/register" className="btn btn-light btn-lg mt-3">Create a customer account</Link>
            </div>
            <div className="col-lg-6">
              <div className="glass-card p-4 text-dark">
                <h5>Featured shops</h5>
                {shops.length === 0 && <p className="text-muted mb-0">No shops are live yet.</p>}
                {shops.map((shop) => (
                  <Link key={shop.id} to={`/shop/${shop.slug}`} className="d-block border rounded-4 p-3 mb-2 text-decoration-none text-dark">
                    <div className="fw-semibold">{shop.name}</div>
                    <div className="small text-muted">{shop.address}</div>
                  </Link>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
