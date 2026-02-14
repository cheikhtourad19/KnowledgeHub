// Production environment configuration (Docker)
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8080', // API Gateway URL - will be configured in Docker
  apiTimeout: 30000,
  enableDebugLogs: false
};
