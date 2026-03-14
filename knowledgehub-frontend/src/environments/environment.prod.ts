// Production environment configuration (Docker)
export const environment = {
  production: true,
  apiUrl: '', // nefss l origine mta3 nginx li howa localhost:80, w nginx ydir reverse proxy l api gateway
  apiTimeout: 30000,
  enableDebugLogs: false,
  app_name: 'KnowledgeHub',
  app_name_prefix: 'Knowledge',
  app_name_suffix: 'Hub',
};
