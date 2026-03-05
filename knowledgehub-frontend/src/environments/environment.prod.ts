// Production environment configuration (Docker)
export const environment = {
  production: true,
  apiUrl: '', // nefss l origine mta3 nginx li howa localhost:80, w nginx ydir reverse proxy l api gateway
  minioUrl: 'http://localhost:9000', // MinIO URL for browser access
  apiTimeout: 30000,
  enableDebugLogs: false,
};
