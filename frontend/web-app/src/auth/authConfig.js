const clientId = import.meta.env.VITE_AZURE_CLIENT_ID;
const tenantId = import.meta.env.VITE_AZURE_TENANT_ID;
const apiScope = import.meta.env.VITE_API_SCOPE;

export const isEntraConfigured = Boolean(clientId && tenantId && apiScope);

export const msalConfig = {
  auth: {
    // The fallback keeps the SPA buildable before Entra is configured.
    clientId: clientId || '00000000-0000-0000-0000-000000000000',
    authority: `https://login.microsoftonline.com/${tenantId || 'common'}`,
    redirectUri: window.location.origin,
    postLogoutRedirectUri: window.location.origin,
  },
  cache: {
    cacheLocation: 'sessionStorage',
  },
};

export const loginRequest = {
  scopes: ['openid', 'profile', 'email'],
};

export const protectedResources = {
  api: {
    endpoint: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    scopes: apiScope ? [apiScope] : [],
  },
};

const ROLE_PRIORITY = ['ADMIN', 'EXECUTIVE', 'CLIENT'];

export const accountToUser = (account) => {
  const claims = account?.idTokenClaims ?? {};
  const roles = (Array.isArray(claims.roles) ? claims.roles : [])
    .map((role) => String(role).toUpperCase());

  return {
    username: account?.username ?? claims.preferred_username ?? 'usuario',
    email: account?.username ?? claims.email ?? '',
    name: account?.name ?? claims.name ?? '',
    role: ROLE_PRIORITY.find((role) => roles.includes(role)) ?? 'CLIENT',
    roles,
    oid: claims.oid,
    subject: claims.sub,
  };
};
