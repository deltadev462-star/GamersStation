# Deployment Guide - Gamers Station Marketplace API

## 🚀 Production Deployment (Docker + VPS)

### Prerequisites
- VPS with Docker and Docker Compose installed (2GB RAM minimum)
- Domain name pointed to your VPS IP
- GitHub repository secrets configured

---

## 📋 Step 1: VPS Setup

### 1.1 Initial Server Setup
```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo apt install docker-compose -y

# Install Certbot for SSL
sudo apt install certbot -y
```

### 1.2 Create Application Directory
```bash
mkdir -p ~/marketplace-api
cd ~/marketplace-api
```

### 1.3 Configure Environment Variables
```bash
# Copy example and edit
cp .env.example .env
nano .env
```

**Required Environment Variables:**
- `DB_PASSWORD` - Strong MySQL password
- `JWT_SECRET` - Random 64+ character string
- `AWS_ACCESS_KEY_ID` & `AWS_SECRET_ACCESS_KEY` - AWS credentials
- `ABLY_API_KEY` - Ably API key
- `WEBSOCKET_ALLOWED_ORIGINS` - Your frontend domains

---

## 🔐 Step 2: SSL Certificate Setup

### Using Let's Encrypt (Recommended)
```bash
# Get SSL certificate
sudo certbot certonly --standalone -d yourdomain.com -d www.yourdomain.com

# Copy certificates to nginx directory
sudo cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem ~/marketplace-api/nginx/ssl/
sudo cp /etc/letsencrypt/live/yourdomain.com/privkey.pem ~/marketplace-api/nginx/ssl/
sudo chown $USER:$USER ~/marketplace-api/nginx/ssl/*
```

### Auto-renewal Cron Job
```bash
# Add to crontab
crontab -e

# Add this line:
0 0 1 * * sudo certbot renew && sudo cp /etc/letsencrypt/live/yourdomain.com/*.pem ~/marketplace-api/nginx/ssl/ && cd ~/marketplace-api && docker-compose restart nginx
```

---

## 🔧 Step 3: GitHub Actions Setup

### 3.1 Add Repository Secrets
Go to: **GitHub Repository → Settings → Secrets and variables → Actions**

Add these secrets:
- `VPS_HOST` - Your VPS IP address
- `VPS_USER` - SSH username (usually `root` or your user)
- `VPS_SSH_KEY` - Your private SSH key

### 3.2 Generate SSH Key (if needed)
```bash
# On your VPS
ssh-keygen -t ed25519 -C "github-actions"

# Add public key to authorized_keys
cat ~/.ssh/id_ed25519.pub >> ~/.ssh/authorized_keys

# Copy private key content for GitHub secret
cat ~/.ssh/id_ed25519
```

---

## 🐳 Step 4: Manual Deployment (First Time)

```bash
cd ~/marketplace-api

# Pull latest code or upload files
git clone https://github.com/The-Gamers-Station/marketplace-api.git .

# Build and start
docker-compose up -d --build

# Check logs
docker-compose logs -f app

# Check health
curl http://localhost:8080/api/v1/actuator/health
```

---

## 🔄 Step 5: Automated Deployments

After GitHub Actions is configured, deployments happen automatically:

1. Push to `main` branch
2. GitHub Actions builds Docker image
3. Image is transferred to VPS
4. Containers are updated automatically
5. Health check verifies deployment

### Manual Trigger
Go to: **Actions → Deploy to VPS → Run workflow**

---

## 📊 Monitoring & Maintenance

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f app
docker-compose logs -f mysql
docker-compose logs -f nginx
```

### Restart Services
```bash
# Restart all
docker-compose restart

# Restart specific service
docker-compose restart app
```

### Database Backup
```bash
# Backup
docker exec marketplace-mysql mysqldump -u root -p$DB_PASSWORD gamers_station_marketplace > backup-$(date +%Y%m%d).sql

# Restore
docker exec -i marketplace-mysql mysql -u root -p$DB_PASSWORD gamers_station_marketplace < backup-20250102.sql
```

### Update Application
```bash
# Pull latest changes
git pull origin main

# Rebuild and restart
docker-compose up -d --build
```

---

## 🛡️ Security Checklist

- [x] Strong database password
- [x] Unique JWT secret (64+ chars)
- [x] SSL/HTTPS enabled
- [x] Firewall configured (UFW)
- [x] Non-root Docker user
- [x] Regular backups
- [x] Environment variables not committed
- [x] API rate limiting enabled

### Configure Firewall
```bash
sudo ufw allow 22/tcp   # SSH
sudo ufw allow 80/tcp   # HTTP
sudo ufw allow 443/tcp  # HTTPS
sudo ufw enable
```

---

## 🔍 Troubleshooting

### Container won't start
```bash
docker-compose logs app
```

### Database connection error
```bash
# Check MySQL is running
docker-compose ps

# Check environment variables
docker-compose config
```

### SSL certificate issues
```bash
# Check certificate validity
sudo certbot certificates

# Renew manually
sudo certbot renew --force-renewal
```

### Out of disk space
```bash
# Clean up Docker
docker system prune -a --volumes

# Check disk usage
df -h
```

---

## 📈 Performance Tuning

### For 2GB RAM VPS
Already optimized in `docker-compose.yml` and `Dockerfile`

### For 4GB+ RAM VPS
Update `Dockerfile`:
```dockerfile
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=85.0", "-jar", "app.jar"]
```

---

## 🔗 Useful Commands

```bash
# Stop all containers
docker-compose down

# Remove everything (including volumes)
docker-compose down -v

# Rebuild without cache
docker-compose build --no-cache

# Check resource usage
docker stats

# Enter container shell
docker exec -it marketplace-api sh

# Enter MySQL
docker exec -it marketplace-mysql mysql -u root -p
```

---

## 📞 Support

For issues or questions:
- GitHub Issues: https://github.com/The-Gamers-Station/marketplace-api/issues
- Documentation: Check README.md

---

## 🎯 Next Steps

1. ✅ Deploy to VPS
2. ✅ Configure SSL
3. ✅ Setup automated backups
4. ⬜ Configure monitoring (optional: Prometheus + Grafana)
5. ⬜ Setup log aggregation (optional: ELK Stack)
6. ⬜ Add CDN for media files (CloudFlare)
