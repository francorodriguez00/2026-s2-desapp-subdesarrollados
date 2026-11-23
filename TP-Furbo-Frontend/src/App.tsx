import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import MainLayout from './layouts/MainLayout';
import PlayersPage from './pages/PlayersPage';
import MarketPage from './pages/MarketPage';
import PortfolioPage from './pages/PortfolioPage';
import TransactionsPage from './pages/TransactionsPage';

const PrivateRoute = ({ children }: { children: React.ReactNode }) => {
  return sessionStorage.getItem('token') ? children : <Navigate to="/login" />;
};

export default function App() {
  return (
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route element={<PrivateRoute><MainLayout /></PrivateRoute>}>
            <Route path="/" element={<div>Bienvenido a FURBO</div>} />
            <Route path="/portfolio" element={<PortfolioPage />} />
            <Route path="/players" element={<PlayersPage />} />
            <Route path="/market" element={<MarketPage />} />
            <Route path="/transactions" element={<TransactionsPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
  );
}