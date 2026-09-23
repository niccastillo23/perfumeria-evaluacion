import { InteractionRequiredAuthError } from '@azure/msal-browser';

import { protectedResources } from './authConfig.js';

export const getAccessToken = async ({ instance, account }) => {
  const scopes = protectedResources.api.scopes;

  if (!account) {
    throw new Error('Debe iniciar sesión antes de llamar a la API.');
  }

  if (scopes.length === 0) {
    throw new Error('VITE_API_SCOPE todavía no está configurado.');
  }

  const tokenRequest = { scopes, account };

  try {
    const tokenResponse = await instance.acquireTokenSilent(tokenRequest);
    return tokenResponse.accessToken;
  } catch (error) {
    if (error instanceof InteractionRequiredAuthError) {
      await instance.acquireTokenRedirect(tokenRequest);
    }

    throw error;
  }
};

export const apiFetch = async ({ instance, account, path, options = {} }) => {
  const accessToken = await getAccessToken({ instance, account });
  const headers = new Headers(options.headers);

  headers.set('Authorization', `Bearer ${accessToken}`);
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  return fetch(`${protectedResources.api.endpoint}${path}`, {
    ...options,
    headers,
  });
};
