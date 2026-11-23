import api from './axiosConfig';

export const buyTokens = async (playerId: number, quantity: number) => {
  return await api.post('/orders/buy', { playerId, quantity });
};

export const sellTokens = async (playerId: number, quantity: number) => {
  return await api.post('/orders/sell', { playerId, quantity });
};
