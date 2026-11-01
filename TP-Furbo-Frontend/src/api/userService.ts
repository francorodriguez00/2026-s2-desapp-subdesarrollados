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
}

export const getPortfolio = async () => {
  console.log('EJECUTANDO getPortfolio'); // sacar
  try {
    const { data } = await api.get<Portfolio>('/users/portfolio');
    return data;
  } catch (error) {
    console.error("Error fetching portfolio:", error);
    throw error;
    //return null; // Devuelve null si falla (ej: 404 o error interno)
  }
};
