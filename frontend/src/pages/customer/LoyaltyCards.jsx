import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/client.js';

export default function LoyaltyCards() {
  const [cards, setCards] = useState([]);
  useEffect(() => {
    api.get('/customers/loyalty-cards').then((r) => setCards(r.data.data || []));
  }, []);
  return (
    <div>
      <h2>Loyalty cards</h2>
      <div className="row g-3">
        {cards.length === 0 && <p className="text-muted">No cards yet.</p>}
        {cards.map((card) => (
          <div className="col-md-6" key={card.id}>
            <div className="card stat-card p-4 h-100">
              <h5>{card.shopName}</h5>
              <p>{card.currentStamps} / {card.requiredStamps} stamps</p>
              <Link to={`/shop/${card.shopSlug}`}>View shop</Link>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
