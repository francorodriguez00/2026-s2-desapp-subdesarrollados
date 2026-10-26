import api from './axiosConfig';

export const getPrice = async (playerId: number) => {
  const { data } = await api.get<number>(`/market/price/${playerId}`);
  return data;
};
