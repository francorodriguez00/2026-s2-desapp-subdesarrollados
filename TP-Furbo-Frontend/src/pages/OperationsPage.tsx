import { useState, useEffect } from 'react';
import { getMyOffers, cancelOffer, getMyOrders, cancelOrder } from '../api/marketService';
import type { SellOfferDTO, UserOrder } from '../api/marketService';

export default function OperationsPage() {
  const [offers, setOffers] = useState<SellOfferDTO[]>([]);
  const [orders, setOrders] = useState<UserOrder[]>([]);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = () => {
    getMyOffers().then(setOffers).catch(console.error);
    getMyOrders().then(setOrders).catch(console.error);
  };

  const handleCancelOffer = async (id: number) => {
    if (window.confirm("¿Seguro que deseas cancelar esta oferta?")) {
        await cancelOffer(id);
        loadData();
    }
  };

  const handleCancelOrder = async (id: number) => {
    if (window.confirm("¿Seguro que deseas cancelar esta orden límite?")) {
        await cancelOrder(id);
        loadData();
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>Mis Operaciones y Órdenes Activas</h1>
      
      <h2>Órdenes Límite / Mercado Abiertas</h2>
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

      <h2>Ofertas de Venta Tradicionales</h2>
      {offers.length === 0 ? (
        <p>No hay ofertas de venta</p>
      ) : (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid #ccc' }}>
              <th style={{ padding: '8px', textAlign: 'left' }}>Jugador</th>
              <th style={{ padding: '8px', textAlign: 'left' }}>Cantidad</th>
              <th style={{ padding: '8px', textAlign: 'left' }}>Precio Mercado</th>
              <th style={{ padding: '8px', textAlign: 'left' }}>Acción</th>
            </tr>
          </thead>
          <tbody>
            {offers.map(o => (
              <tr key={o.id} style={{ borderBottom: '1px solid #eee' }}>
                <td style={{ padding: '8px' }}>{o.playerName}</td>
                <td style={{ padding: '8px' }}>{o.quantityRemaining}</td>
                <td style={{ padding: '8px' }}>${o.currentPrice}</td>
                <td style={{ padding: '8px' }}>
                  <button onClick={() => handleCancelOffer(o.id)} style={{ color: 'red' }}>Cancelar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
