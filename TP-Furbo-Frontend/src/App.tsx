import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import MainLayout from './layouts/MainLayout';
import PlayersPage from './pages/PlayersPage';
import MarketPage from './pages/MarketPage';
import PortfolioPage from './pages/PortfolioPage';
import TransactionsPage from './pages/TransactionsPage';
import { useEffect, useState } from 'react';
import { getBalance } from './api/userService';

const PrivateRoute = ({ children }: { children: React.ReactNode }) => {
  return sessionStorage.getItem('token') ? children : <Navigate to="/login" />;
};

const HomePage = () => {
  const [balance, setBalance] = useState<number | null>(null);
  useEffect(() => {
    getBalance().then(setBalance).catch(console.error);
  }, []);
  return (
    <div style={{ padding: '20px' }}>
      <h1>Bienvenido a FURBO</h1>
      <div style={{ padding: '15px', border: '1px solid #ccc', borderRadius: '5px', display: 'inline-block' }}>
        <h3>Tu Saldo Disponible: {balance !== null ? `${balance} créditos` : 'Cargando...'}</h3>
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
          </Route>
        </Routes>
      </BrowserRouter>
  );
}
