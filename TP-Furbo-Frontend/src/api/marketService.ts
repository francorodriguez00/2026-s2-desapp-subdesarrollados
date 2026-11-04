import api from './axiosConfig';

export interface SellOfferDTO {
  id: number;
  playerName: string;
  quantityRemaining: number;
  currentPrice: number;
  createdAt: string;
}

export const getPrice = async (playerId: number) => {
  const { data } = await api.get<number>(`/market/price/${playerId}`);
  return data;
};

export const getMyOffers = async () => {
  const { data } = await api.get<SellOfferDTO[]>('/market/my-offers');
  return data;
};

export const cancelOffer = async (offerId: number) => {
  await api.delete(`/market/offers/${offerId}`);
};
