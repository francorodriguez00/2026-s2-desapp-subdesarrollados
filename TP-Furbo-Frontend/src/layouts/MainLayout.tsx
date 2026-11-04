import { NavLink, Outlet } from 'react-router-dom';
import { logout } from '../api/authService';

export default function MainLayout() {
  const navStyle = ({ isActive }: { isActive: boolean }) => ({
    padding: '10px',
    color: isActive ? 'var(--color-positive)' : 'var(--color-primary)',
    textDecoration: 'none',
    fontWeight: isActive ? 'bold' : 'normal',
  });

  return (
    <div>
      <nav style={{ padding: '20px', borderBottom: '1px solid var(--color-border)', display: 'flex', gap: '20px', alignItems: 'center' }}>
        <strong style={{ fontSize: '20px' }}>FURBO</strong>
        <NavLink style={navStyle} to="/">Inicio</NavLink>
        <NavLink style={navStyle} to="/players">Catálogo</NavLink>
        <NavLink style={navStyle} to="/portfolio">Portfolio</NavLink>
        <NavLink style={navStyle} to="/market">Mercado</NavLink>
        <NavLink style={navStyle} to="/transactions">Movimientos</NavLink>
        <NavLink style={navStyle} to="/operations">Operaciones</NavLink>
        <button onClick={logout} style={{ marginLeft: 'auto' }}>Cerrar Sesión</button>
      </nav>
      <main style={{ padding: '20px' }}>
        <Outlet />
      </main>
    </div>
  );
}
