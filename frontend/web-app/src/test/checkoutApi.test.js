import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('../auth/apiClient.js', () => ({
  apiFetch: vi.fn().mockResolvedValue({ ok: true }),
}));

import { apiFetch } from '../auth/apiClient.js';
import { protectedResources } from '../auth/authConfig.js';
import { submitOrder } from '../checkout/checkoutApi.js';

describe('submitOrder', () => {
  beforeEach(() => vi.clearAllMocks());

  it('posts checkout through the protected BFF endpoint with the Orders.Create scope', async () => {
    const instance = { acquireTokenSilent: vi.fn() };
    const account = { username: 'customer@example.com' };
    const cart = [{ id: 1, name: 'Elysian', brand: 'PerfumerIA', price: 20, quantity: 2 }];

    await submitOrder({ instance, account, cart });

    expect(apiFetch).toHaveBeenCalledWith(expect.objectContaining({
      instance,
      account,
      path: '/api/v1/shop/checkout',
      scopes: protectedResources.api.scopesByOperation.orders,
      options: expect.objectContaining({ method: 'POST' }),
    }));
  });
});
