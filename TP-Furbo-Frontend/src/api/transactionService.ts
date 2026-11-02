import api from '../api/axiosConfig';

export interface TransactionDetail {
  quantity: number;
  unitPrice: number;
}

export interface Transaction {
  operationId: string;
  playerName: string;
  totalQuantity: number;
  totalAmount: number;
  createdAt: string;
  type: 'COMPRA' | 'VENTA';
  details: TransactionDetail[];
}

export const getTransactions = async (params: { type?: string; playerName?: string; sort?: string }) => {
  const { data } = await api.get<Transaction[]>('/users/transactions', { params });
  return data;
};
