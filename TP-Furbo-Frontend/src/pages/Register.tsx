import { useState } from 'react';
import { register } from '../api/authService';
import { useNavigate } from 'react-router-dom';

export default function Register() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await register({ email, password, name });
    navigate('/login');
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', padding: '16px' }}>
      <form onSubmit={handleSubmit} className="card" style={{ display: 'flex', flexDirection: 'column', width: '100%', maxWidth: '400px', gap: '16px', padding: '32px' }}>
        <h1 style={{ textAlign: 'center', margin: '0 0 8px 0', borderBottom: 'none' }}>Registro</h1>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
          <label style={{ fontWeight: 500, fontSize: '14px' }}>Nombre</label>
          <input className="input-field" type="text" placeholder="Tu Nombre" style={{ padding: '12px 16px', fontSize: '15px', width: '100%', boxSizing: 'border-box' }} onChange={(e) => setName(e.target.value)} required />
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
          <label style={{ fontWeight: '500', fontSize: '14px' }}>Email</label>
          <input className="input-field" type="email" placeholder="tu@email.com" style={{ padding: '12px 16px', fontSize: '15px', width: '100%', boxSizing: 'border-box' }} onChange={(e) => setEmail(e.target.value)} required />
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
          <label style={{ fontWeight: 500, fontSize: '14px' }}>Contraseña</label>
          <input className="input-field" type="password" placeholder="••••••••" style={{ padding: '12px 16px', fontSize: '15px', width: '100%', boxSizing: 'border-box' }} onChange={(e) => setPassword(e.target.value)} required />
        </div>
        <button type="submit" className="btn btn-primary" style={{ padding: '14px', fontSize: '16px', fontWeight: 600, marginTop: '8px', width: '100%' }}>Registrarse</button>
        <p style={{ marginTop: '12px', textAlign: 'center', fontSize: '14px' }}>
          ¿Ya tenés cuenta? <a href="/login" style={{ color: 'var(--color-primary)', textDecoration: 'none', fontWeight: '500' }}>Iniciá sesión</a>
        </p>
      </form>
    </div>
  );
}
