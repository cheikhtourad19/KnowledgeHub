# Stage 1: Build with dependency caching
FROM node:20-alpine AS build

WORKDIR /app

# Copy only package files first
COPY package*.json ./

# Install dependencies (cached layer)
RUN npm ci --prefer-offline

# Copy source code
COPY . .

# Build the application for production
RUN npm run build -- --configuration=production

# Stage 2: Serve with Nginx
FROM nginx:alpine

# Copy built files from build stage
COPY --from=build /app/dist/knowledgehub-frontend/browser /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
