import { useEffect, useRef, useState } from 'react';
import { Html5Qrcode } from 'html5-qrcode';
import toast from 'react-hot-toast';
import api from '../../api/client.js';

export default function Scanner() {
  const [customer, setCustomer] = useState(null);
  const [stamps, setStamps] = useState(1);
  const [lastQr, setLastQr] = useState('');
  const scannerRef = useRef(null);
  const running = useRef(false);

  useEffect(() => {
    const scanner = new Html5Qrcode('qr-reader');
    scannerRef.current = scanner;
    Html5Qrcode.getCameras().then((cameras) => {
      const id = cameras[0]?.id;
      if (!id) {
        toast.error('No camera found');
        return;
      }
      return scanner.start(
        { deviceId: { exact: id } },
        { fps: 8, qrbox: 220 },
        async (decoded) => {
          if (running.current) return;
          running.current = true;
          try {
            const { data } = await api.post('/stamps/scan', { qrPayload: decoded });
            setLastQr(decoded);
            setCustomer(data.data);
            toast.success('Customer found');
          } catch (err) {
            toast.error(err.response?.data?.message || 'Invalid QR');
          } finally {
            setTimeout(() => { running.current = false; }, 1500);
          }
        }
      );
    }).catch(() => toast.error('Unable to start camera'));
    return () => {
      scanner.stop().catch(() => undefined);
    };
  }, []);

  const add = async () => {
    try {
      await api.post('/stamps/add', { customerId: customer.customerId, stamps: Number(stamps), description: 'Counter scan' });
      toast.success('Stamp added');
      if (lastQr) {
        const { data } = await api.post('/stamps/scan', { qrPayload: lastQr });
        setCustomer(data.data);
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not add stamp');
    }
  };

  const reverse = async () => {
    if (!window.confirm('Reverse stamps for this customer?')) return;
    try {
      await api.post('/stamps/reverse', { customerId: customer.customerId, stamps: Number(stamps), description: 'Correction' });
      toast.success('Stamp reversed');
      setCustomer({ ...customer, currentStamps: Math.max(0, customer.currentStamps - Number(stamps)) });
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not reverse stamp');
    }
  };

  return (
    <div>
      <h2>QR scanner</h2>
      <div className="row g-4">
        <div className="col-lg-6">
          <div className="card stat-card p-3">
            <div id="qr-reader" style={{ width: '100%' }} />
          </div>
        </div>
        <div className="col-lg-6">
          {!customer && <div className="alert alert-light">Camera is on. Ask the customer to show their Digital Stamp QR.</div>}
          {customer && (
            <div className="card stat-card p-4">
              <h4>{customer.customerName}</h4>
              <p className="mb-1">{customer.email}</p>
              <p className="text-muted">{customer.mobile}</p>
              <p className="fs-5">{customer.currentStamps} / {customer.requiredStamps} stamps</p>
              <div className="mb-3">
                <label className="form-label">Stamps</label>
                <input className="form-control" type="number" min="1" value={stamps} onChange={(e) => setStamps(e.target.value)} />
              </div>
              <div className="d-flex gap-2">
                <button className="btn btn-primary" onClick={add}>Add stamp</button>
                <button className="btn btn-outline-danger" onClick={reverse}>Reverse</button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
