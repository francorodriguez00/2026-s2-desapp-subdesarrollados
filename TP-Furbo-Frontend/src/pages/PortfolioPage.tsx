import { useEffect, useState } from 'react';
import { getPortfolio } from '../api/userService';
import type { Portfolio } from '../api/userService';

console.log('SE RENDERIZA PortfolioPage'); // sacar

export default function PortfolioPage() {

  console.log('PortfolioPage ejecutándose'); // sacar

  const [portfolio, setPortfolio] = useState<Portfolio | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadData() {
      try {
        const p = await getPortfolio();
        setPortfolio(p);
      } catch (err) {
        console.error("Error loading portfolio:", err);
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, []);

  if (loading) return <div>Cargando portfolio...</div>;

  return (
    <div>
      <h1>Mi Portfolio</h1>
      {(!portfolio?.posiciones || portfolio.posiciones.filter(p => p.cantidad > 0).length === 0) ? (
        <p>No tienes posiciones abiertas actualmente.</p>
      ) : (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--color-border)' }}>
              <th style={{ textAlign: 'left', padding: '8px' }}>Jugador</th>
              <th style={{ textAlign: 'left', padding: '8px' }}>Cantidad</th>
              <th style={{ textAlign: 'left', padding: '8px' }}>Precio Unit.</th>
              <th style={{ textAlign: 'left', padding: '8px' }}>Valor Total</th>
            </tr>
          </thead>
          <tbody>
            {portfolio.posiciones.filter(p => p.cantidad > 0).map((pos: any) => (
              <tr key={pos.playerId} style={{ borderBottom: '1px solid var(--color-border)' }}>
                <td style={{ padding: '8px' }}>{pos.playerName}</td>
                <td style={{ padding: '8px' }}>{pos.cantidad}</td>
                <td style={{ padding: '8px' }}>{pos.precioActual} créditos</td>
                <td style={{ padding: '8px' }}>{pos.valorTotal} créditos</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
