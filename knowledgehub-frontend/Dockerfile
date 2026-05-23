# Stage 1: Build
FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci --prefer-offline

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
