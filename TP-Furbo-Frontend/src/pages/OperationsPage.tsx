import { useState, useEffect } from 'react';
import { getMyOffers, cancelOffer } from '../api/marketService';
import type { SellOfferDTO } from '../api/marketService';

export default function OperationsPage() {
  const [offers, setOffers] = useState<SellOfferDTO[]>([]);

  useEffect(() => {
    loadOffers();
  }, []);

  const loadOffers = () => {
    getMyOffers().then(setOffers).catch(console.error);
  };

  const handleCancel = async (id: number) => {
    if (window.confirm("¿Seguro que deseas cancelar esta orden?")) {
        await cancelOffer(id);
        loadOffers();
    }
  };

  return (
    <div>
      <h1>Mis Operaciones</h1>
      {offers.length === 0 ? (
        <p>No hay operaciones para mostrar</p>
      ) : (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid #ccc' }}>
              <th style={{ padding: '8px', textAlign: 'left' }}>Jugador</th>
              <th style={{ padding: '8px', textAlign: 'left' }}>Cantidad</th>
              <th style={{ padding: '8px', textAlign: 'left' }}>Precio Mercado</th>
              <th style={{ padding: '8px', textAlign: 'left' }}>Fecha</th>
              <th style={{ padding: '8px', textAlign: 'left' }}>Acción</th>
            </tr>
          </thead>
          <tbody>
            {offers.map(o => (
              <tr key={o.id} style={{ borderBottom: '1px solid #eee' }}>
                <td style={{ padding: '8px' }}>{o.playerName}</td>
                <td style={{ padding: '8px' }}>{o.quantityRemaining}</td>
                <td style={{ padding: '8px' }}>{o.currentPrice} créditos</td>
                <td style={{ padding: '8px' }}>{new Date(o.createdAt).toLocaleString()}</td>
                <td style={{ padding: '8px' }}>
                  <button onClick={() => handleCancel(o.id)} style={{ color: 'red' }}>Cancelar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
