import api from './axiosConfig';

export const buyTokens = async (playerId: number, quantity: number) => {
  return await api.post('/orders/buy', { playerId, quantity });
};

export const sellTokens = async (playerId: number, quantity: number) => {
  return await api.post('/orders/sell', { playerId, quantity });
};

export const createOrder = async (order: { playerId: number; side: 'BUY' | 'SELL'; type: 'LIMIT' | 'MARKET'; price?: number; quantity: number }) => {
  const { data } = await api.post('/orders', order);
  return data;
};
