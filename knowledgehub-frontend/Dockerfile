# Stage 1: Build
FROM node:20-alpine AS build

WORKDIR /app

ENV npm_config_fetch_retries=5 \
	npm_config_fetch_retry_factor=2 \
	npm_config_fetch_retry_mintimeout=20000 \
	npm_config_fetch_retry_maxtimeout=120000 \
	npm_config_network_timeout=600000

COPY package*.json ./

RUN --mount=type=cache,target=/root/.npm \
	npm ci --prefer-offline --no-audit --no-fund

COPY . .

RUN npm run build -- --configuration=production

# Stage 2: Serve
FROM nginx:alpine

COPY --from=build /app/dist/knowledgehub-frontend/browser /usr/share/nginx/html

COPY nginx.conf /etc/nginx/nginx.conf

COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 80
ENTRYPOINT ["/docker-entrypoint.sh"]
