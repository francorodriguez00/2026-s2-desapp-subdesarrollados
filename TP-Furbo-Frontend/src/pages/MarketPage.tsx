import { useState, useEffect } from 'react';
import { getPlayers, getPlayerDetail } from '../api/playerService';
import { buyTokens, sellTokens } from '../api/orderService';
import { getPrice } from '../api/marketService';
import type { Player } from '../types/player';
import { Pagination } from '../components/Pagination';
import { useSearchParams } from 'react-router-dom';

export default function MarketPage() {
  const [searchParams] = useSearchParams();
  const [players, setPlayers] = useState<Player[]>([]);
  const initialPlayerId = searchParams.get('playerId') ? Number(searchParams.get('playerId')) : '';
  const [playerId, setPlayerId] = useState<number | ''>(initialPlayerId);
  const [price, setPrice] = useState<number | null>(null);
  const [quantity, setQuantity] = useState<number>(1);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  const [nameSearch, setNameSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    if (initialPlayerId) {
        getPlayerDetail(initialPlayerId).then(p => {
            setPlayers(prev => prev.find(pl => pl.id === p.id) ? prev : [p, ...prev]);
        });
    }
  }, [initialPlayerId]);

  const fetchPlayers = async (search: string, pg: number) => {
    try {
      setLoading(true);
      const data = await getPlayers({ name: search, page: pg, size: 20 });
      let newPlayers = data.content;
      
      // Keep initial player in list if it's not in the new page
      if (initialPlayerId && !newPlayers.find(p => p.id === initialPlayerId)) {
         const p = await getPlayerDetail(initialPlayerId);
         newPlayers = [p, ...newPlayers];
      }
      setPlayers(newPlayers);
      setTotalPages(data.totalPages);
    } catch (err) {
      console.error("Error loading players:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPlayers(nameSearch, page);
  }, [nameSearch, page]);

  useEffect(() => {
    if (playerId) {
      getPrice(Number(playerId)).then(setPrice);
    } else {
      setPrice(null);
    }
  }, [playerId]);

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
        <input 
            placeholder="Buscar jugador..." 
            value={nameSearch} 
            onChange={(e) => { setNameSearch(e.target.value); setPage(0); }} 
        />
        
        <select 
          onChange={(e) => setPlayerId(e.target.value === '' ? '' : Number(e.target.value))} 
          value={playerId === '' ? '' : playerId}
        >
          <option value="">Seleccionar jugador</option>
          {players.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
        </select>
        
        <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />

        {price !== null && <p>Precio unitario: {price} créditos</p>}
        
        <input type="number" min="1" value={quantity} onChange={(e) => setQuantity(Number(e.target.value))} />
        <button disabled={loading || !playerId} onClick={() => handleOrder('buy')}>Comprar</button>
        <button disabled={loading || !playerId} onClick={() => handleOrder('sell')}>Vender</button>
      </div>
    </div>
  );
}
