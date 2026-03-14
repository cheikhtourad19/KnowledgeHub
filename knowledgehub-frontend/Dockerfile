# Stage 1: Build with dependency caching
FROM node:20-alpine AS build

WORKDIR /app

COPY knowledgehub-frontend/package*.json ./

RUN npm ci --prefer-offline

COPY knowledgehub-frontend/ .

RUN npm run build -- --configuration=production

# Stage 2: Serve with Nginx
FROM nginx:alpine

COPY --from=build /app/dist/knowledgehub-frontend/browser /usr/share/nginx/html

COPY knowledgehub-frontend/nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]