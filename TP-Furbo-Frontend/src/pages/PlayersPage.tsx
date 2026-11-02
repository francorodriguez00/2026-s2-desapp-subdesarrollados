import { useEffect, useState } from 'react';
import { getPlayers } from '../api/playerService';
import type { Player } from '../types/player';

export default function PlayersPage() {
  const [players, setPlayers] = useState<Player[]>([]);
  const [loading, setLoading] = useState(true);
  const [nameSearch, setNameSearch] = useState('');
  const [league, setLeague] = useState('');
  const [position, setPosition] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchPlayers = async () => {
    try {
      setLoading(true);
      const data = await getPlayers({ name: nameSearch, league, position, page, size: 20 });
      setPlayers(data.content);
      setTotalPages(data.totalPages);
    } catch (err) {
      console.error("Error loading players:", err);
    } finally {
      setLoading(false);
    }
  };


  useEffect(() => {
    fetchPlayers();
  }, [nameSearch, league, position, page]);

  if (loading && players.length === 0) return <div>Cargando...</div>;

  return (
    <div>
      <h1>Catálogo de Jugadores</h1>
      
      <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
        <input placeholder="Nombre..." value={nameSearch} onChange={(e) => { setNameSearch(e.target.value); setPage(0); }} />
        <select value={league} onChange={(e) => { setLeague(e.target.value); setPage(0); }}>
            <option value="">Todas las ligas</option>
            {['PL', 'PD', 'SA', 'BL1', 'FL1'].map(l => <option key={l} value={l}>{l}</option>)}
        </select>
        <select value={position} onChange={(e) => { setPosition(e.target.value); setPage(0); }}>
            <option value="">Todas las pos.</option>
            {['Goalkeeper', 'Defence', 'Midfield', 'Offence'].map(p => <option key={p} value={p}>{p}</option>)}
        </select>
      </div>

      <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '20px' }}>
        <thead>
          <tr style={{ borderBottom: '1px solid var(--color-border)' }}>
            <th style={{ textAlign: 'left', padding: '8px' }}>Nombre</th>
            <th style={{ textAlign: 'left', padding: '8px' }}>Posición</th>
            <th style={{ textAlign: 'left', padding: '8px' }}>Equipo</th>
            <th style={{ textAlign: 'left', padding: '8px' }}>Liga</th>
            <th style={{ textAlign: 'left', padding: '8px' }}>Cotización</th>
          </tr>
        </thead>
        <tbody>
          {players.map(p => (
            <tr key={p.id} style={{ borderBottom: '1px solid var(--color-border)' }}>
              <td style={{ padding: '8px' }}>{p.name}</td>
              <td style={{ padding: '8px' }}>{p.position}</td>
              <td style={{ padding: '8px' }}>{p.team}</td>
              <td style={{ padding: '8px' }}>{p.league}</td>
              <td style={{ padding: '8px' }}>{p.price} créditos</td>
            </tr>
          ))}
        </tbody>
      </table>
      <div style={{ marginTop: '20px', display: 'flex', gap: '10px' }}>
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>Anterior</button>
          <span>Página {page + 1} de {totalPages}</span>
          <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Siguiente</button>
      </div>
    </div>
  );
}
