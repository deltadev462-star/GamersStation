# CI/CD Quick Start Guide

## 🚀 Quick Setup (5 minutes)

### On Your VPS Server

1. **Copy the setup script to your VPS:**
```bash
# Download from GitHub after you push, or manually copy setup-cicd.sh
wget https://raw.githubusercontent.com/YOUR_USERNAME/marketplace-web/main/setup-cicd.sh
chmod +x setup-cicd.sh
```

2. **Run the setup script:**
```bash
./setup-cicd.sh
```

The script will:
- ✅ Install Docker & Docker Compose (if needed)
- ✅ Generate SSH keys for GitHub Actions
- ✅ Create deployment directory
- ✅ Create .env template
- ✅ Configure firewall
- ✅ Display your server IP and SSH key

3. **Edit the .env file with your real credentials:**
```bash
nano ~/marketplace-api/.env
```

### On GitHub

1. **Go to your repository → Settings → Secrets and variables → Actions**

2. **Add 3 secrets:**
   - `VPS_HOST` - Your server IP (shown by setup script)
   - `VPS_USER` - Your username (shown by setup script)
   - `VPS_SSH_KEY` - SSH private key (shown by setup script)

### Deploy!

```bash
git add .
git commit -m "Setup CI/CD"
git push origin main
```

Watch the deployment: **GitHub → Actions tab**

---

## 📋 What Gets Deployed

### Your CI/CD Pipeline:

```
Push to main
    ↓
Run Tests (MySQL + Maven + npm)
    ↓ (only if tests pass)
Build Docker Images (backend + frontend)
    ↓
Transfer to VPS
    ↓
Deploy with docker-compose
    ↓
Health Checks
    ↓
✅ Done!
```

### Services Deployed:
- **Backend API** (Spring Boot) - Port 8080
- **Frontend** (React + Vite) - Port 3001  
- **MySQL Database** - Port 3307
- **Nginx** (Reverse Proxy) - Port 80/443

---

## 🔍 Monitoring

### Check deployment status:
```bash
ssh user@your-vps
cd ~/marketplace-api
docker-compose ps
```

### View logs:
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f app
docker-compose logs -f client
```

### Health check:
```bash
curl http://localhost:8080/api/v1/actuator/health
```

---

## 🛠️ Common Commands

### On VPS:
```bash
cd ~/marketplace-api

# Restart services
docker-compose restart

# Stop services
docker-compose down

# Start services
docker-compose up -d

# View running containers
docker-compose ps

# Clean old images
docker system prune -f
```

### On Your Machine:
```bash
# Trigger manual deployment
# Go to: GitHub → Actions → Deploy to VPS → Run workflow

# Check workflow status
gh run list

# Watch workflow in real-time
gh run watch
```

---

## 🚨 Troubleshooting

### Deployment Failed?

1. **Check GitHub Actions logs:**
   - Go to GitHub → Actions → Click failed run

2. **Check VPS logs:**
```bash
ssh user@your-vps
cd ~/marketplace-api
docker-compose logs --tail=100
```

3. **Verify environment variables:**
```bash
cat ~/marketplace-api/.env
```

### Tests Failing?

Run tests locally first:
```bash
mvn test
cd client && npm run lint && npm run build
```

### SSH Connection Issues?

Test SSH manually:
```bash
ssh -i ~/.ssh/github-actions user@your-vps
```

---

## 📖 Full Documentation

For detailed documentation, see:
- **[CI-CD-SETUP.md](CI-CD-SETUP.md)** - Complete setup guide
- **[DEPLOYMENT.md](DEPLOYMENT.md)** - Manual deployment guide

---

## ✅ Checklist

Before going live:

- [ ] GitHub secrets configured (VPS_HOST, VPS_USER, VPS_SSH_KEY)
- [ ] .env file on VPS has real credentials
- [ ] Database password is secure (not "CHANGE_THIS_PASSWORD")
- [ ] JWT secret is 64+ characters
- [ ] AWS S3 credentials are valid
- [ ] Firewall configured (ports 22, 80, 443)
- [ ] SSL certificates configured (see DEPLOYMENT.md)
- [ ] Test deployment works
- [ ] Health checks pass

---

## 🎯 Next Steps After Setup

1. **Set up SSL certificates:**
```bash
sudo certbot certonly --standalone -d yourdomain.com
```

2. **Configure automated backups:**
```bash
# Add to crontab
0 2 * * * docker exec marketplace-mysql mysqldump -u root -p$DB_PASSWORD gamers_station_marketplace | gzip > /backup/db-$(date +\%Y\%m\%d).sql.gz
```

3. **Set up monitoring** (optional):
   - Prometheus + Grafana
   - Uptime monitoring (UptimeRobot, etc.)

4. **Branch protection** (recommended):
   - GitHub → Settings → Branches
   - Add rule for `main` branch
   - Require status checks to pass

---

## 💡 Tips

- **Always test locally before pushing to main**
- **Monitor first few deployments closely**
- **Keep .env file secure (never commit it)**
- **Set up Slack/email notifications for failures**
- **Review logs regularly**

---

## 🆘 Support

- Check logs: `docker-compose logs -f`
- GitHub Actions status: Repository → Actions tab
- Full guide: [CI-CD-SETUP.md](CI-CD-SETUP.md)
- Issues? Check troubleshooting section above

---

**Your CI/CD is ready! 🎉**

Every push to `main` will now:
1. Run tests automatically
2. Deploy to VPS if tests pass
3. Health check the deployment
4. Notify you of status
