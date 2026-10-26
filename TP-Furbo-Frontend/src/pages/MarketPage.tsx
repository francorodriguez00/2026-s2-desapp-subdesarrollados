import { useState, useEffect } from 'react';
import { getPlayers } from '../api/playerService';
import { buyTokens, sellTokens } from '../api/orderService';
import type { Player } from '../types/player';

export default function MarketPage() {
  const [players, setPlayers] = useState<Player[]>([]);
  const [playerId, setPlayerId] = useState<number | ''>('');
  const [quantity, setQuantity] = useState<number>(1);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  useEffect(() => {
    getPlayers().then(setPlayers);
  }, []);

  const handleOrder = async (type: 'buy' | 'sell') => {
    if (!playerId || quantity <= 0) return;
    setLoading(true);
    setMessage(null);
    try {
      if (type === 'buy') await buyTokens(Number(playerId), quantity);
      else await sellTokens(Number(playerId), quantity);
      setMessage({ text: 'Operación realizada con éxito', type: 'success' });
    } catch {
      setMessage({ text: 'Error al realizar la operación', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>Mercado</h1>
      {message && <p style={{ color: message.type === 'error' ? 'var(--color-negative)' : 'var(--color-positive)' }}>{message.text}</p>}
      
      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', maxWidth: '300px' }}>
        <select onChange={(e) => setPlayerId(e.target.value === '' ? '' : Number(e.target.value))} value={playerId}>
          <option value="">Seleccionar jugador</option>
          {players.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
        </select>
        <input type="number" min="1" value={quantity} onChange={(e) => setQuantity(Number(e.target.value))} />
        <button disabled={loading} onClick={() => handleOrder('buy')}>Comprar</button>
        <button disabled={loading} onClick={() => handleOrder('sell')}>Vender</button>
      </div>
    </div>
  );
}
