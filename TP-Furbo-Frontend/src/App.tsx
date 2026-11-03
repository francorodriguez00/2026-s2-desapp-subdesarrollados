import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import MainLayout from './layouts/MainLayout';
import PlayersPage from './pages/PlayersPage';
import MarketPage from './pages/MarketPage';
import PortfolioPage from './pages/PortfolioPage';
import TransactionsPage from './pages/TransactionsPage';
import OperationsPage from './pages/OperationsPage';
import { useEffect, useState } from 'react';
import { depositCredits, getPortfolio } from './api/userService';

const PrivateRoute = ({ children }: { children: React.ReactNode }) => {
  return sessionStorage.getItem('token') ? children : <Navigate to="/login" />;
};

const HomePage = () => {
  const [data, setData] = useState<{ balance: number, totalInvertido: number } | null>(null);
  const [deposito, setDeposito] = useState<number>(0);

  const loadData = async () => {
      try {
          const port = await getPortfolio();
          setData({ balance: port.saldoDisponible, totalInvertido: port.totalInvertido });
      } catch (e) {
          console.error(e);
      }
  };

  useEffect(() => { loadData(); }, []);

  const handleDeposit = async () => {
      await depositCredits(deposito);
      setDeposito(0);
      loadData();
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>Bienvenido a FURBO</h1>
      <div style={{ padding: '15px', border: '1px solid #ccc', borderRadius: '5px' }}>
        <p>Créditos Disponibles: {data ? `${data.balance.toFixed(2)} créditos` : '...'}</p>
        <p>Créditos Invertidos: {data ? `${data.totalInvertido.toFixed(2)} créditos` : '...'}</p>
        <p><strong>Total Valorizado: {data ? `${(data.balance + data.totalInvertido).toFixed(2)} créditos` : '...'}</strong></p>
      </div>
      <div style={{ marginTop: '20px' }}>
          <input type="number" value={deposito} onChange={(e) => setDeposito(Number(e.target.value))} />
          <button onClick={handleDeposit}>Depositar Créditos</button>
      </div>
    </div>
  );
};

export default function App() {
  return (
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route element={<PrivateRoute><MainLayout /></PrivateRoute>}>
            <Route path="/" element={<HomePage />} />
            <Route path="/portfolio" element={<PortfolioPage />} />
            <Route path="/players" element={<PlayersPage />} />
            <Route path="/market" element={<MarketPage />} />
            <Route path="/transactions" element={<TransactionsPage />} />
            <Route path="/operations" element={<OperationsPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
  );
}
