import api from './axiosConfig';

export interface Position {
  playerId: number;
  playerName: string;
  cantidad: number;
  precioActual: number;
  valorTotal: number;
}

export interface Portfolio {
  posiciones: Position[];
  saldoDisponible: number;
  totalInvertido: number;
}

export const getPortfolio = async () => {
  try {
    const { data } = await api.get<Portfolio>('/users/portfolio');
    return data;
  } catch (error) {
    console.error("Error fetching portfolio:", error);
    throw error;
  }
};

export const getBalance = async () => {
  try {
    const { data } = await api.get<number>('/users/balance');
    return data;
  } catch (error) {
    console.error("Error fetching balance:", error);
    throw error;
  }
};
