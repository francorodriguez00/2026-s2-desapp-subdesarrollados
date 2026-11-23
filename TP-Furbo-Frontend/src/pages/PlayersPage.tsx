import { useEffect, useState } from 'react';
import { getPlayers } from '../api/playerService';
import type { Player } from '../types/player';

export default function PlayersPage() {
  const [players, setPlayers] = useState<Player[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getPlayers()
      .then(setPlayers)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div>Cargando...</div>;

  return (
    <div>
      <h1>Catálogo de Jugadores</h1>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ borderBottom: '1px solid var(--color-border)' }}>
            <th style={{ textAlign: 'left', padding: '8px' }}>Nombre</th>
            <th style={{ textAlign: 'left', padding: '8px' }}>Posición</th>
            <th style={{ textAlign: 'left', padding: '8px' }}>Equipo</th>
          </tr>
        </thead>
        <tbody>
          {players.map(p => (
            <tr key={p.id} style={{ borderBottom: '1px solid var(--color-border)' }}>
              <td style={{ padding: '8px' }}>{p.name}</td>
              <td style={{ padding: '8px' }}>{p.position}</td>
              <td style={{ padding: '8px' }}>{p.team}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
