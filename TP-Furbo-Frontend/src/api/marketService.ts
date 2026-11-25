import api from './axiosConfig';

export interface BookLevel {
  price: number;
  totalQuantity: number;
}

export interface OrderBook {
  buyLevels: BookLevel[];
  sellLevels: BookLevel[];
  lastPrice: number;
}

export interface SellOfferDTO {
  id: number;
  playerName: string;
  quantityRemaining: number;
  currentPrice: number;
  createdAt: string;
}

export interface UserOrder {
  id: number;
  playerId: number;
  playerName: string;
  side: 'BUY' | 'SELL';
  type: 'LIMIT' | 'MARKET';
  status: 'OPEN' | 'PARTIAL' | 'FILLED' | 'CANCELLED';
  price: number;
  quantityOriginal: number;
  quantityRemaining: number;
  createdAt: string;
}

export const getPrice = async (playerId: number) => {
  const { data } = await api.get<number>(`/market/price/${playerId}`);
  return data;
};

export const getOrderBook = async (playerId: number) => {
  const { data } = await api.get<OrderBook>(`/market/book/${playerId}`);
  return data;
};

export const getLastTrade = async (playerId: number) => {
  const { data } = await api.get<number>(`/market/last-trade/${playerId}`);
  return data;
};

export const getMyOffers = async () => {
  const { data } = await api.get<SellOfferDTO[]>('/market/my-offers');
  return data;
};

export const getMyOrders = async () => {
  const { data } = await api.get<UserOrder[]>('/orders/my');
  return data;
};

export const cancelOffer = async (offerId: number) => {
  await api.delete(`/market/offers/${offerId}`);
};

export const cancelOrder = async (orderId: number) => {
  await api.delete(`/orders/${orderId}`);
};
