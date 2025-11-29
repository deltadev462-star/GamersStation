#!/bin/bash

# Gamers Station Marketplace API - Deployment Script
# Usage: ./deploy.sh [environment]
# Example: ./deploy.sh prod

set -e

ENVIRONMENT=${1:-prod}
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "🚀 Starting deployment for environment: $ENVIRONMENT"

# Check if .env file exists
if [ ! -f .env ]; then
    echo "❌ .env file not found! Copy .env.example to .env and configure it."
    exit 1
fi

# Build Docker image
echo "📦 Building Docker image..."
docker-compose build

# Stop existing containers
echo "🛑 Stopping existing containers..."
docker-compose down

# Start new containers
echo "▶️  Starting new containers..."
docker-compose up -d

# Wait for services to be healthy
echo "⏳ Waiting for services to be healthy..."
sleep 30

# Check health
echo "🏥 Checking application health..."
if curl -f http://localhost:8080/api/v1/actuator/health > /dev/null 2>&1; then
    echo "✅ Deployment successful!"
    echo "🌐 API is running at http://localhost:8080"
else
    echo "❌ Health check failed! Rolling back..."
    docker-compose logs app
    exit 1
fi

# Show logs
echo ""
echo "📋 Recent logs:"
docker-compose logs --tail=50 app

echo ""
echo "✨ Deployment completed at $(date)"
echo "📊 To view logs: docker-compose logs -f app"
echo "🛑 To stop: docker-compose down"
