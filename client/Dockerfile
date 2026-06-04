# Stage 1: Build the application
FROM node:20.19.4-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci --legacy-peer-deps && \
    npm cache clean --force

COPY . .

RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:alpine

# Copy built assets from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy nginx configuration
COPY nginx/nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
