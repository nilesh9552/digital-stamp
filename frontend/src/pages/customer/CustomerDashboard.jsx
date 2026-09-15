import { useEffect, useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import api from '../../api/client.js';
import { useAuth } from '../../context/AuthContext.jsx';

export default function CustomerDashboard() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [cards, setCards] = useState([]);
  const [enlarged, setEnlarged] = useState(false);

  const load = () => {
    api.get('/customers/profile').then((r) => setProfile(r.data.data));
    api.get('/customers/loyalty-cards').then((r) => setCards(r.data.data || []));
  };
  useEffect(load, []);

  const downloadQr = () => {
    const svg = document.getElementById('customer-qr');
    if (!svg) return;
    const blob = new Blob([svg.outerHTML], { type: 'image/svg+xml' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'digital-stamp-qr.svg';
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div>
      <h2 className="mb-4">Hello, {user?.name}</h2>
      <div className="row g-4">
        <div className="col-lg-4">
          <div className="card stat-card p-4 text-center">
            <h5>Your QR card</h5>
            <p className="small text-muted">Show this at the counter. No passwords or tokens are stored in the code.</p>
            {profile && (
              <div className="qr-box mx-auto" onClick={() => setEnlarged(true)} role="button">
                <QRCodeSVG id="customer-qr" value={profile.qrPayload} size={180} />
              </div>
            )}
            <button className="btn btn-outline-primary btn-sm mt-3" onClick={downloadQr}>Download QR</button>
          </div>
        </div>
        <div className="col-lg-8">
          {cards.length === 0 && <div className="alert alert-light">You are not enrolled in any shop yet. Open a shop page and join its program.</div>}
          {cards.map((card) => {
            const pct = Math.min(100, Math.round((card.currentStamps / card.requiredStamps) * 100));
            return (
              <div className="card stat-card p-4 mb-3" key={card.id}>
                <div className="d-flex justify-content-between">
                  <div>
                    <h5 className="mb-1">{card.shopName}</h5>
                    <div className="text-muted">{card.programName}</div>
                  </div>
                  <strong>{card.currentStamps} / {card.requiredStamps} Stamps</strong>
                </div>
                <div className="progress progress-stamp mt-3">
                  <div className="progress-bar brand-gradient" style={{ width: `${pct}%` }} />
                </div>
                <p className="small mt-2 mb-0">
                  {card.rewardEligible ? 'You are eligible for a reward.' : `${card.stampsRemaining} more stamps to unlock your reward.`}
                </p>
              </div>
            );
          })}
        </div>
      </div>
      {enlarged && profile && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.55)' }} onClick={() => setEnlarged(false)}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content p-4 text-center">
              <QRCodeSVG value={profile.qrPayload} size={280} />
              <div className="mt-3">{profile.qrPayload}</div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
