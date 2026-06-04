#!/bin/bash

# CI/CD Setup Script for VPS
# Run this script on your VPS server to prepare for CI/CD deployment

set -e

echo "=========================================="
echo "Marketplace CI/CD Setup Script"
echo "=========================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if running on VPS (not Windows)
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    echo -e "${RED}❌ This script should be run on your VPS server, not Windows${NC}"
    exit 1
fi

# Function to print status
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

echo "Step 1: Checking prerequisites..."
echo ""

# Check if Docker is installed
if command -v docker &> /dev/null; then
    print_status "Docker is installed: $(docker --version)"
else
    print_error "Docker is not installed"
    echo "Installing Docker..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    print_status "Docker installed successfully"
fi

# Check if Docker Compose is installed
if command -v docker-compose &> /dev/null; then
    print_status "Docker Compose is installed: $(docker-compose --version)"
else
    print_error "Docker Compose is not installed"
    echo "Installing Docker Compose..."
    sudo apt update
    sudo apt install docker-compose -y
    print_status "Docker Compose installed successfully"
fi

echo ""
echo "Step 2: Creating deployment directory..."
echo ""

# Use existing deployment directory
DEPLOY_DIR="/opt/marketplace-web"
if [ ! -d "$DEPLOY_DIR" ]; then
    print_error "Directory $DEPLOY_DIR does not exist"
    echo "Creating directory..."
    sudo mkdir -p "$DEPLOY_DIR"
    sudo chown $USER:$USER "$DEPLOY_DIR"
    print_status "Created directory: $DEPLOY_DIR"
else
    print_status "Using existing directory: $DEPLOY_DIR"
fi
cd "$DEPLOY_DIR"

echo ""
echo "Step 3: Setting up SSH key for GitHub Actions..."
echo ""

# Generate SSH key for GitHub Actions
SSH_KEY_PATH="$HOME/.ssh/github-actions"
if [ -f "$SSH_KEY_PATH" ]; then
    print_warning "SSH key already exists at $SSH_KEY_PATH"
    read -p "Do you want to regenerate it? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        ssh-keygen -t ed25519 -C "github-actions" -f "$SSH_KEY_PATH" -N ""
        print_status "SSH key regenerated"
    fi
else
    ssh-keygen -t ed25519 -C "github-actions" -f "$SSH_KEY_PATH" -N ""
    print_status "SSH key generated"
fi

# Add public key to authorized_keys
cat "$SSH_KEY_PATH.pub" >> "$HOME/.ssh/authorized_keys"
chmod 600 "$HOME/.ssh/authorized_keys"
print_status "Public key added to authorized_keys"

echo ""
echo "=========================================="
echo "IMPORTANT: Copy this private key to GitHub"
echo "=========================================="
echo ""
echo "Go to: GitHub → Settings → Secrets → New secret"
echo "Name: VPS_SSH_KEY"
echo "Value: Copy the content below (including BEGIN/END lines)"
echo ""
echo "---BEGIN PRIVATE KEY---"
cat "$SSH_KEY_PATH"
echo "---END PRIVATE KEY---"
echo ""
read -p "Press Enter after you've copied the key to GitHub..."

echo ""
echo "Step 4: Creating .env file..."
echo ""

ENV_FILE="$DEPLOY_DIR/.env"
if [ -f "$ENV_FILE" ]; then
    print_warning ".env file already exists"
    read -p "Do you want to recreate it? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_status "Keeping existing .env file"
    else
        create_env=true
    fi
else
    create_env=true
fi

if [ "$create_env" = true ]; then
    cat > "$ENV_FILE" << 'EOF'
# Application Configuration
SPRING_PROFILE=prod

# Database Configuration
DB_NAME=gamers_station_marketplace
DB_USERNAME=marketplace_user
DB_PASSWORD=CHANGE_THIS_PASSWORD
DB_URL=jdbc:mysql://mysql:3306/gamers_station_marketplace?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC

# JWT Configuration (IMPORTANT: Change these!)
JWT_SECRET=CHANGE_THIS_TO_A_RANDOM_64_CHARACTER_STRING
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000
JWT_ISSUER=gamers-station

# AWS S3 Configuration
AWS_ACCESS_KEY_ID=YOUR_AWS_ACCESS_KEY
AWS_SECRET_ACCESS_KEY=YOUR_AWS_SECRET_KEY
AWS_S3_BUCKET_NAME=gs-marketplace
AWS_S3_REGION=us-east-1

# Ably Configuration
ABLY_API_KEY=YOUR_ABLY_API_KEY

# WebSocket CORS
WEBSOCKET_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
EOF

    chmod 600 "$ENV_FILE"
    print_status ".env file created at $ENV_FILE"
    print_warning "IMPORTANT: Edit .env file and set your real credentials!"
    echo ""
    echo "Run: nano $ENV_FILE"
fi

echo ""
echo "Step 5: Setting up firewall..."
echo ""

# Configure UFW firewall
if command -v ufw &> /dev/null; then
    read -p "Configure firewall? (recommended) (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        sudo ufw allow 22/tcp comment 'SSH'
        sudo ufw allow 80/tcp comment 'HTTP'
        sudo ufw allow 443/tcp comment 'HTTPS'
        sudo ufw --force enable
        print_status "Firewall configured"
    fi
else
    print_warning "UFW not installed, skipping firewall setup"
fi

echo ""
echo "Step 6: GitHub Secrets Summary..."
echo ""

# Get server IP
SERVER_IP=$(curl -s ifconfig.me)

echo "=========================================="
echo "GitHub Secrets to Configure"
echo "=========================================="
echo ""
echo "Go to: GitHub → Settings → Secrets and variables → Actions"
echo ""
echo "Add these 3 secrets:"
echo ""
echo "1. VPS_HOST"
echo "   Value: $SERVER_IP"
echo ""
echo "2. VPS_USER"
echo "   Value: $USER"
echo ""
echo "3. VPS_SSH_KEY"
echo "   Value: (Already copied above)"
echo ""

echo ""
echo "=========================================="
echo "Setup Complete!"
echo "=========================================="
echo ""
print_status "Deployment directory: $DEPLOY_DIR"
print_status "SSH key: $SSH_KEY_PATH"
print_status "Server IP: $SERVER_IP"
echo ""
echo "Next steps:"
echo "1. Edit .env file: nano $ENV_FILE"
echo "2. Configure GitHub secrets (see above)"
echo "3. Push to main branch to trigger deployment"
echo ""
echo "Useful commands:"
echo "  - View logs: cd $DEPLOY_DIR && docker-compose logs -f"
echo "  - Restart: cd $DEPLOY_DIR && docker-compose restart"
echo "  - Status: cd $DEPLOY_DIR && docker-compose ps"
echo ""
