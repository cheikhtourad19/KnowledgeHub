// Production environment configuration (Docker)
export const environment = {
  production: true,
  apiUrl: '', // Empty = same origin, nginx proxies all API requests to api-gateway
  apiTimeout: 30000,
  enableDebugLogs: false
};
