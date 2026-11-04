import { useState, useEffect } from 'react';
import { getMyOrders, cancelOrder } from '../api/marketService';
import type { UserOrder } from '../api/marketService';

export default function OperationsPage() {
  const [orders, setOrders] = useState<UserOrder[]>([]);

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = () => {
    getMyOrders().then(setOrders).catch(console.error);
  };

  const handleCancelOrder = async (id: number) => {
    if (window.confirm("¿Seguro que deseas cancelar esta orden?")) {
        await cancelOrder(id);
        loadOrders();
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>Órdenes Abiertas</h1>
      
      {orders.length === 0 ? (
        <p>No hay órdenes abiertas</p>
      ) : (
        <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '20px' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid #ccc' }}>
              <th style={{ padding: '8px', textAlign: 'left' }}>Lado</th>
              <th style={{ padding: '8px', textAlign: 'left' }}>Tipo</th>
              <th style={{ padding: '8px', textAlign: 'left' }}>Precio</th>
              <th style={{ padding: '8px', textAlign: 'left' }}>Restante</th>
              <th style={{ padding: '8px', textAlign: 'left' }}>Estado</th>
              <th style={{ padding: '8px', textAlign: 'left' }}>Acción</th>
            </tr>
          </thead>
          <tbody>
            {orders.map(o => (
              <tr key={o.id} style={{ borderBottom: '1px solid #eee' }}>
                <td style={{ padding: '8px', color: o.side === 'BUY' ? 'green' : 'red' }}>{o.side}</td>
                <td style={{ padding: '8px' }}>{o.type}</td>
                <td style={{ padding: '8px' }}>${o.price}</td>
                <td style={{ padding: '8px' }}>{o.quantityRemaining}</td>
                <td style={{ padding: '8px' }}>{o.status}</td>
                <td style={{ padding: '8px' }}>
                  <button onClick={() => handleCancelOrder(o.id)} style={{ color: 'red' }}>Cancelar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
