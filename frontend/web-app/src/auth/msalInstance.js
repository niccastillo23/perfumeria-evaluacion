import { PublicClientApplication } from '@azure/msal-browser';

import { msalConfig } from './authConfig.js';

// Keep a single MSAL instance for the whole React tree.
export const msalInstance = new PublicClientApplication(msalConfig);
