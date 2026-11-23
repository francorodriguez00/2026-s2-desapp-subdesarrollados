export const login = async (credentials: any) => {
  const { data } = await api.post('/auth/login', credentials);
  sessionStorage.setItem('token', data.token);
  return data;
};

export const register = async (userData: any) => {
  return await api.post('/auth/register', userData);
};

export const logout = () => {
  sessionStorage.removeItem('token');
  window.location.href = '/login';
};

import api from './axiosConfig';
