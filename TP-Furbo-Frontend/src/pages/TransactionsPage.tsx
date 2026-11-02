import { useState, useEffect } from 'react';
import { getTransactions } from '../api/transactionService';
import type { Transaction } from '../api/transactionService';

export default function TransactionsPage() {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [type, setType] = useState('');
  const [playerName, setPlayerName] = useState('');
  const [sort, setSort] = useState('DESC');
  const [expanded, setExpanded] = useState<string | null>(null);

  useEffect(() => {
    getTransactions({ type, playerName, sort }).then(setTransactions).catch(console.error);
  }, [type, playerName, sort]);

  return (
    <div>
      <h1>Movimientos y Operaciones</h1>
      <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
        <input placeholder="Jugador..." value={playerName} onChange={(e) => setPlayerName(e.target.value)} />
        <select value={type} onChange={(e) => setType(e.target.value)}>
          <option value="">Todos</option>
          <option value="COMPRA">Compras</option>
          <option value="VENTA">Ventas</option>
        </select>
        <select value={sort} onChange={(e) => setSort(e.target.value)}>
          <option value="DESC">Más reciente</option>
          <option value="ASC">Menos reciente</option>
        </select>
      </div>

      <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '10px' }}>
        <thead>
          <tr style={{ borderBottom: '2px solid #ccc', textAlign: 'left' }}>
            <th style={{ padding: '8px' }}>Jugador</th>
            <th style={{ padding: '8px' }}>Tipo</th>
            <th style={{ padding: '8px' }}>Cantidad</th>
            <th style={{ padding: '8px' }}>Precio Unit.</th>
            <th style={{ padding: '8px' }}>Total</th>
            <th style={{ padding: '8px' }}>Fecha</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map(t => (
            <tr key={t.operationId} style={{ borderBottom: '1px solid #eee' }}>
              <td style={{ padding: '8px' }}>{t.playerName}</td>
              <td style={{ padding: '8px' }}>{t.type}</td>
              <td style={{ padding: '8px' }}>{t.totalQuantity}</td>
              <td style={{ padding: '8px' }}>{t.details[0]?.unitPrice} cr.</td>
              <td style={{ padding: '8px' }}>{t.totalAmount} cr.</td>
              <td style={{ padding: '8px' }}>{new Date(t.createdAt).toLocaleString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
