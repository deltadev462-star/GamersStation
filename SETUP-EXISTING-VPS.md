# CI/CD Setup for Existing VPS Deployment

## Your Current Setup
- **App Location:** `/opt/marketplace-web`
- **Already deployed:** Yes
- **Docker:** Already running

---

## Quick Setup Steps

### Step 1: On Your VPS Server

SSH into your VPS:
```bash
ssh user@your-vps-ip
```

Then run these commands:

```bash
# Go to your app directory
cd /opt/marketplace-web

# Generate SSH key for GitHub Actions
ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/github-actions -N ""

# Add public key to authorized_keys
cat ~/.ssh/github-actions.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

# Display the private key (COPY THIS!)
echo "=========================================="
echo "COPY THIS ENTIRE KEY TO GITHUB SECRETS"
echo "=========================================="
cat ~/.ssh/github-actions
echo "=========================================="
```

**IMPORTANT:** Copy the entire SSH private key (including `-----BEGIN` and `-----END` lines)

---

### Step 2: Get Your Server Info

While still on your VPS, run:

```bash
# Get your server IP
curl -s ifconfig.me

# Get your username
whoami
```

Write down:
- ✅ Server IP: `_______________`
- ✅ Username: `_______________`
- ✅ SSH Key: (already copied above)

---

### Step 3: Configure GitHub Secrets

1. Go to your GitHub repository
2. Click: **Settings** → **Secrets and variables** → **Actions**
3. Click: **New repository secret**

Add these 3 secrets:

#### Secret 1: `VPS_HOST`
```
Name: VPS_HOST
Value: YOUR_SERVER_IP_HERE
```

#### Secret 2: `VPS_USER`
```
Name: VPS_USER
Value: YOUR_USERNAME_HERE
```

#### Secret 3: `VPS_SSH_KEY`
```
Name: VPS_SSH_KEY
Value: PASTE_THE_PRIVATE_KEY_HERE
```

---

### Step 4: Verify Your .env File

Make sure your `.env` file exists in `/opt/marketplace-web`:

```bash
# Check if .env exists
ls -la /opt/marketplace-web/.env

# If it doesn't exist, create it:
sudo nano /opt/marketplace-web/.env
```

Your `.env` should have:
```env
SPRING_PROFILE=prod
DB_NAME=gamers_station_marketplace
DB_USERNAME=marketplace_user
DB_PASSWORD=your_password
DB_URL=jdbc:mysql://mysql:3306/gamers_station_marketplace
JWT_SECRET=your_jwt_secret_64_chars_minimum
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000
AWS_ACCESS_KEY_ID=your_aws_key
AWS_SECRET_ACCESS_KEY=your_aws_secret
AWS_S3_BUCKET_NAME=gs-marketplace
AWS_S3_REGION=us-east-1
ABLY_API_KEY=your_ably_key
WEBSOCKET_ALLOWED_ORIGINS=https://yourdomain.com
```

---

### Step 5: Test the Pipeline

From your Windows machine:

```powershell
# Commit and push the CI/CD files
git add .
git commit -m "Setup CI/CD pipeline"
git push origin main
```

Then watch the deployment:
1. Go to GitHub → **Actions** tab
2. Click on the running workflow
3. Watch the progress

---

## What Happens on Each Push

```
1. You push to main
         ↓
2. GitHub Actions runs tests
   - Backend: Maven + MySQL
   - Frontend: npm lint + build
         ↓
3. If tests pass → Build Docker images
         ↓
4. Transfer images to /opt/marketplace-web
         ↓
5. Run: docker-compose down
         ↓
6. Run: docker-compose up -d
         ↓
7. Health checks
         ↓
8. Done! ✅
```

---

## Monitoring Your Deployment

### Check Status
```bash
cd /opt/marketplace-web
docker-compose ps
```

### View Logs
```bash
# All services
docker-compose logs -f

# Just backend
docker-compose logs -f app

# Just frontend
docker-compose logs -f client
```

### Health Check
```bash
curl http://localhost:8080/api/v1/actuator/health
```

---

## Troubleshooting

### If Deployment Fails

1. **Check GitHub Actions logs:**
   - Go to GitHub → Actions → Click failed run

2. **Check VPS logs:**
```bash
cd /opt/marketplace-web
docker-compose logs --tail=100
```

3. **Verify permissions:**
```bash
ls -la /opt/marketplace-web
# Should show your user as owner
# If not, fix with:
sudo chown -R $USER:$USER /opt/marketplace-web
```

### If SSH Connection Fails

Test the SSH key manually:
```bash
ssh -i ~/.ssh/github-actions user@your-vps-ip
```

If this doesn't work:
- Check the key was added to authorized_keys
- Verify firewall allows SSH (port 22)
- Check VPS_SSH_KEY secret has the complete key

### If Tests Fail

Run tests locally before pushing:
```powershell
# On Windows
mvn test
cd client
npm run lint
npm run build
```

---

## Manual Deployment (If Needed)

If you need to deploy manually without CI/CD:

```bash
cd /opt/marketplace-web

# Pull latest changes
git pull origin main

# Rebuild and restart
docker-compose down
docker-compose up -d --build

# Check health
sleep 30
curl http://localhost:8080/api/v1/actuator/health
```

---

## Rollback to Previous Version

If new deployment has issues:

```bash
cd /opt/marketplace-web

# Stop containers
docker-compose down

# Check available images
docker images | grep marketplace

# Tag previous version as latest
docker tag gs-marketplace-api:PREVIOUS_SHA gs-marketplace-api:latest
docker tag gs-marketplace-client:PREVIOUS_SHA gs-marketplace-client:latest

# Start with previous version
docker-compose up -d
```

---

## Common Commands

```bash
# Go to app directory
cd /opt/marketplace-web

# Restart all services
docker-compose restart

# Restart specific service
docker-compose restart app

# Stop all services
docker-compose down

# Start all services
docker-compose up -d

# Clean old Docker images
docker system prune -f

# Check disk space
df -h

# Check Docker resource usage
docker stats
```

---

## Security Checklist

Before going live:

- [ ] SSH key added to GitHub secrets
- [ ] `.env` file has real credentials (not defaults)
- [ ] Database password is strong
- [ ] JWT secret is 64+ characters
- [ ] Firewall configured (ports 22, 80, 443)
- [ ] SSL certificates configured
- [ ] Backups configured

---

## Next Steps

### 1. Set up automated backups:
```bash
# Create backup directory
sudo mkdir -p /backup

# Add to crontab
crontab -e

# Add this line (backup daily at 2 AM):
0 2 * * * docker exec marketplace-mysql mysqldump -u root -p$DB_PASSWORD gamers_station_marketplace | gzip > /backup/db-$(date +\%Y\%m\%d).sql.gz
```

### 2. Set up SSL (if not done):
```bash
sudo certbot certonly --standalone -d yourdomain.com
```

### 3. Monitor first deployment closely:
```bash
# Watch logs in real-time
cd /opt/marketplace-web
docker-compose logs -f
```

---

## Support

- **GitHub Actions logs:** Repository → Actions tab
- **VPS logs:** `docker-compose logs -f`
- **Health check:** `curl http://localhost:8080/api/v1/actuator/health`
- **Full guide:** See `CI-CD-SETUP.md`

---

## Summary

✅ Your deployment path: `/opt/marketplace-web`
✅ CI/CD will test before deploying
✅ Automatic deployments on push to main
✅ Health checks ensure deployment success
✅ Zero-downtime updates

**You're all set!** Just configure the GitHub secrets and push to main. 🚀
