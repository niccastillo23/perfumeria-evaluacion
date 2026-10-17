import { apiFetch } from '../auth/apiClient.js';
import { protectedResources } from '../auth/authConfig.js';

export const createOrderPayload = (cart) => ({
  items: cart.map(({ id, name, brand, price, quantity }) => ({
    productId: id,
    name,
    brand,
    price,
    quantity,
  })),
  total: cart.reduce((sum, item) => sum + item.price * item.quantity, 0),
  payment: { type: 'cash_on_delivery' },
  carrier: 'dhl',
  destination: 'Por coordinar con el cliente',
});

export const submitOrder = ({ instance, account, cart }) => apiFetch({
  instance,
  account,
  path: '/api/v1/shop/checkout',
  scopes: protectedResources.api.scopesByOperation.orders,
  options: {
    method: 'POST',
    body: JSON.stringify(createOrderPayload(cart)),
  },
});
