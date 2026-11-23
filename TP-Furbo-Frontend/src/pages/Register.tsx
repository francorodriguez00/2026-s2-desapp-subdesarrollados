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
    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', maxWidth: '300px', margin: 'auto', gap: '8px' }}>
      <h1>Registro</h1>
      <input type="text" placeholder="Nombre" onChange={(e) => setName(e.target.value)} required />
      <input type="email" placeholder="Email" onChange={(e) => setEmail(e.target.value)} required />
      <input type="password" placeholder="Password" onChange={(e) => setPassword(e.target.value)} required />
      <button type="submit">Registrarse</button>
      <p style={{ marginTop: '16px', textAlign: 'center' }}>
        ¿Ya tenés cuenta? <a href="/login">Iniciá sesión</a>
      </p>
    </form>
  );
}
