import { useState, useEffect } from 'react';
import { getPlayers, getPlayerDetail } from '../api/playerService';
import { createOrder } from '../api/orderService';
import { getPrice, getOrderBook, getLastTrade } from '../api/marketService';
import type { Player } from '../types/player';
import type { OrderBook } from '../api/marketService';
import { Pagination } from '../components/Pagination';
import { useSearchParams } from 'react-router-dom';

export default function MarketPage() {
  const [searchParams] = useSearchParams();
  const [players, setPlayers] = useState<Player[]>([]);
  const initialPlayerId = searchParams.get('playerId') ? Number(searchParams.get('playerId')) : '';
  const [playerId, setPlayerId] = useState<number | ''>(initialPlayerId);
  const [price, setPrice] = useState<number | null>(null);
  const [lastTrade, setLastTrade] = useState<number | null>(null);
  const [orderBook, setOrderBook] = useState<OrderBook | null>(null);
  
  const [orderSide, setOrderSide] = useState<'BUY' | 'SELL'>('BUY');
  const [orderType, setOrderType] = useState<'MARKET' | 'LIMIT'>('MARKET');
  const [limitPrice, setLimitPrice] = useState<number | ''>('');
  const [quantity, setQuantity] = useState<number>(1);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  const [nameSearch, setNameSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Cálculo estimado de costos
  const currentEffectivePrice = orderType === 'LIMIT' && limitPrice !== '' ? Number(limitPrice) : (price || 0);
  const subtotal = currentEffectivePrice * quantity;
  const commission = subtotal * 0.005;
  const total = orderSide === 'BUY' ? subtotal + commission : subtotal - commission;

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
      const fetchMarketData = () => {
        getPrice(Number(playerId)).then(setPrice);
        getLastTrade(Number(playerId)).then(setLastTrade);
        getOrderBook(Number(playerId)).then(setOrderBook);
      };
      fetchMarketData();
      const interval = setInterval(fetchMarketData, 4000);
      return () => clearInterval(interval);
    } else {
      setPrice(null);
      setLastTrade(null);
      setOrderBook(null);
    }
  }, [playerId]);

  const handleSubmitOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!playerId || quantity <= 0) return;
    if (orderType === 'LIMIT' && (limitPrice === '' || Number(limitPrice) <= 0)) {
      setMessage({ text: 'Ingrese un precio límite válido', type: 'error' });
      return;
    }
    setLoading(true);
    setMessage(null);
    try {
      await createOrder({
        playerId: Number(playerId),
        side: orderSide,
        type: orderType,
        price: orderType === 'LIMIT' ? Number(limitPrice) : undefined,
        quantity: Number(quantity)
      });
      setMessage({ text: 'Orden ejecutada / colocada con éxito', type: 'success' });
      if (playerId) {
        getOrderBook(Number(playerId)).then(setOrderBook);
        getPrice(Number(playerId)).then(setPrice);
        getLastTrade(Number(playerId)).then(setLastTrade);
      }
    } catch (err: any) {
      setMessage({ text: err.response?.data || 'Error al procesar la orden', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>Mercado</h1>
      {message && <p style={{ color: message.type === 'error' ? 'var(--color-negative)' : 'var(--color-positive)' }}>{message.text}</p>}
      
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
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

          {price !== null && <p>Cotización de referencia: <strong>${price}</strong></p>}
          {lastTrade !== null && <p>Último precio operado: <strong>${lastTrade}</strong></p>}
          
          <form onSubmit={handleSubmitOrder} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            <div style={{ display: 'flex', gap: '10px' }}>
              <label>
                <input type="radio" value="BUY" checked={orderSide === 'BUY'} onChange={() => setOrderSide('BUY')} /> Comprar
              </label>
              <label>
                <input type="radio" value="SELL" checked={orderSide === 'SELL'} onChange={() => setOrderSide('SELL')} /> Vender
              </label>
            </div>

            <div style={{ display: 'flex', gap: '10px' }}>
              <label>
                <input type="radio" value="MARKET" checked={orderType === 'MARKET'} onChange={() => setOrderType('MARKET')} /> Precio Mercado
              </label>
              <label>
                <input type="radio" value="LIMIT" checked={orderType === 'LIMIT'} onChange={() => setOrderType('LIMIT')} /> Precio Límite
              </label>
            </div>

            {orderType === 'LIMIT' && (
              <input 
                type="number" 
                step="0.01" 
                placeholder="Precio límite (±50% banda)" 
                value={limitPrice} 
                onChange={(e) => setLimitPrice(e.target.value === '' ? '' : Number(e.target.value))} 
              />
            )}

            <input type="number" min="1" placeholder="Cantidad" value={quantity} onChange={(e) => setQuantity(Number(e.target.value))} />
            
            {subtotal > 0 && (
                <div style={{ padding: '10px', background: '#f0f0f0', borderRadius: '4px' }}>
                    <p>Subtotal: ${subtotal.toFixed(2)}</p>
                    <p>Comisión (0.5%): ${commission.toFixed(2)}</p>
                    <strong>{orderSide === 'BUY' ? 'Total a pagar' : 'Total a recibir'}: ${total.toFixed(2)}</strong>
                </div>
            )}
            
            <button type="submit" disabled={loading || !playerId}>
              {orderSide === 'BUY' ? 'Enviar Compra' : 'Enviar Venta'}
            </button>
          </form>
        </div>

        <div>
          <h3>Caja de Puntas</h3>
          {playerId ? (
            orderBook ? (
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <div style={{ border: '1px solid #ccc', padding: '10px' }}>
                  <h4 style={{ color: 'green' }}>Compra</h4>
                  <table style={{ width: '100%', fontSize: '14px' }}>
                    <thead><tr><th>Cant</th><th>Precio</th></tr></thead>
                    <tbody>
                      {orderBook.buyLevels.map((lvl, idx) => (
                        <tr key={idx}><td>{lvl.totalQuantity}</td><td>${lvl.price.toFixed(2)}</td></tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                <div style={{ border: '1px solid #ccc', padding: '10px' }}>
                  <h4 style={{ color: 'red' }}>Venta</h4>
                  <table style={{ width: '100%', fontSize: '14px' }}>
                    <thead><tr><th>Precio</th><th>Cant</th></tr></thead>
                    <tbody>
                      {orderBook.sellLevels.map((lvl, idx) => (
                        <tr key={idx}><td>${lvl.price.toFixed(2)}</td><td>{lvl.totalQuantity}</td></tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            ) : <p>Cargando libro...</p>
          ) : <p>Seleccione un jugador</p>}
        </div>
      </div>
    </div>
  );
}
