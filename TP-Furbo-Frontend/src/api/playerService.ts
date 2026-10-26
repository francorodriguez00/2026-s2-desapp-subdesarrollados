import api from './axiosConfig';
import type { Player } from '../types/player';

export const getPlayers = async (params?: { name?: string; position?: string; team?: string; league?: string }) => {
  const { data } = await api.get<{ content: Player[] }>('/players', { params });
  return data.content;
};

export const getPlayerDetail = async (id: number) => {
  const { data } = await api.get<Player>(`/players/${id}`);
  return data;
};
