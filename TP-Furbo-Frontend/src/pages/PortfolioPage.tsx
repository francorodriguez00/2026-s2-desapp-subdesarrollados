import { useEffect, useState } from 'react';
import { getPortfolio } from '../api/userService';
import { getPlayers } from '../api/playerService';
import { getPrice } from '../api/marketService';
import type { Portfolio, Position } from '../api/userService';
import type { Player } from '../types/player';

console.log('SE RENDERIZA PortfolioPage'); // sacar

export default function PortfolioPage() {

  console.log('PortfolioPage ejecutándose'); // sacar

  const [portfolio, setPortfolio] = useState<Portfolio | null>(null);
  const [players, setPlayers] = useState<Record<number, Player>>({});
  const [prices, setPrices] = useState<Record<number, number>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadData() {
      try {
        const [p, allPlayers] = await Promise.all([getPortfolio(), getPlayers()]);
        setPortfolio(p);
        
        const playerMap = allPlayers.reduce((acc, player) => ({ ...acc, [player.id]: player }), {} as Record<number, Player>);
        setPlayers(playerMap);

        const priceMap: Record<number, number> = {};
        for (const pos of (p?.posiciones || [])) {
          if (pos.cantidad > 0) {
            priceMap[pos.playerId] = await getPrice(pos.playerId);
          }
        }
        setPrices(priceMap);
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
      <pre>Portfolio Data: {JSON.stringify(portfolio, null, 2)}</pre>
      {(!portfolio?.posiciones || portfolio.posiciones.filter(p => p.cantidad > 0).length === 0) ? (
        <p>No tienes posiciones abiertas actualmente.</p>
      ) : (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--color-border)' }}>
              <th style={{ textAlign: 'left', padding: '8px' }}>Jugador</th>
              <th style={{ textAlign: 'left', padding: '8px' }}>Cantidad</th>
              <th style={{ textAlign: 'left', padding: '8px' }}>Precio Unit.</th>
            </tr>
          </thead>
          <tbody>
            {portfolio.posiciones.filter(p => p.cantidad > 0).map((pos: Position) => (
              <tr key={pos.playerId} style={{ borderBottom: '1px solid var(--color-border)' }}>
                <td style={{ padding: '8px' }}>{players[pos.playerId]?.name || `Jugador ID: ${pos.playerId}`}</td>
                <td style={{ padding: '8px' }}>{pos.cantidad}</td>
                <td style={{ padding: '8px' }}>{prices[pos.playerId] || 0} créditos</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
