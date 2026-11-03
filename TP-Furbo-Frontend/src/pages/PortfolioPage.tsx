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
        <>
          <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '20px' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--color-border)' }}>
                <th style={{ textAlign: 'left', padding: '8px' }}>Jugador</th>
                <th style={{ textAlign: 'left', padding: '8px' }}>Cantidad</th>
                <th style={{ textAlign: 'left', padding: '8px' }}>Precio Prom.</th>
                <th style={{ textAlign: 'left', padding: '8px' }}>Precio Actual</th>
                <th style={{ textAlign: 'left', padding: '8px' }}>Valor Total</th>
                <th style={{ textAlign: 'left', padding: '8px' }}>Ganancia/Pérdida</th>
              </tr>
            </thead>
            <tbody>
              {portfolio.posiciones.filter(p => p.cantidad > 0).map((pos: any) => {
                const color = pos.porcentajeGanancia > 0 ? 'green' : pos.porcentajeGanancia < 0 ? 'red' : 'black';
                return (
                  <tr key={pos.playerId} style={{ borderBottom: '1px solid var(--color-border)' }}>
                    <td style={{ padding: '8px' }}>{pos.playerName}</td>
                    <td style={{ padding: '8px' }}>{pos.cantidad}</td>
                    <td style={{ padding: '8px' }}>{pos.precioPromedio.toFixed(2)}</td>
                    <td style={{ padding: '8px' }}>{pos.precioActual.toFixed(2)}</td>
                    <td style={{ padding: '8px' }}>{pos.valorTotal.toFixed(2)}</td>
                    <td style={{ padding: '8px', color }}>{pos.porcentajeGanancia.toFixed(2)}%</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
          <div style={{ textAlign: 'right', padding: '10px', backgroundColor: '#f0f0f0', borderRadius: '5px' }}>
            <p><strong>Saldo Disponible:</strong> {portfolio.saldoDisponible} créditos</p>
            <p><strong>Total Invertido:</strong> {portfolio.totalInvertido} créditos</p>
          </div>
        </>
      )}
    </div>
  );

}
