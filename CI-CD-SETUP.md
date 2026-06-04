# CI/CD Pipeline Setup Guide

## Overview
This project uses **GitHub Actions** for automated testing and deployment to a VPS server. The pipeline runs tests in Docker containers and deploys only if all tests pass.

---

## Pipeline Architecture

```
┌─────────────────┐
│  Push to main   │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Test Job (Parallel)                │
│  ├─ Backend tests (MySQL + Maven)   │
│  ├─ Frontend tests (npm)            │
│  └─ Frontend build                  │
└────────┬────────────────────────────┘
         │ (Only if tests pass)
         ▼
┌─────────────────────────────────────┐
│  Build & Deploy Job                 │
│  ├─ Build backend JAR               │
│  ├─ Build Docker images             │
│  ├─ Transfer to VPS                 │
│  ├─ Load images on VPS              │
│  ├─ docker-compose down/up          │
│  └─ Health checks                   │
└─────────────────────────────────────┘
```

---

## Setup Instructions

### 1. Configure GitHub Repository Secrets

Go to: **GitHub Repository → Settings → Secrets and variables → Actions → New repository secret**

Add these secrets:

| Secret Name | Description | Example |
|-------------|-------------|---------|
| `VPS_HOST` | Your VPS IP address or domain | `123.45.67.89` |
| `VPS_USER` | SSH username (usually root) | `root` |
| `VPS_SSH_KEY` | Private SSH key for authentication | Contents of `~/.ssh/id_rsa` |

#### Generate SSH Key (if needed)

On your **VPS server**:
```bash
# Generate new SSH key
ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/github-actions

# Add public key to authorized_keys
cat ~/.ssh/github-actions.pub >> ~/.ssh/authorized_keys

# Display private key (copy this to GitHub secret VPS_SSH_KEY)
cat ~/.ssh/github-actions
```

### 2. VPS Server Preparation

SSH into your VPS and prepare the deployment directory:

```bash
# Create deployment directory
mkdir -p ~/marketplace-api
cd ~/marketplace-api

# Create .env file (IMPORTANT: Set real values)
cat > .env << EOF
# Application
SPRING_PROFILE=prod

# Database
DB_NAME=gamers_station_marketplace
DB_USERNAME=marketplace_user
DB_PASSWORD=YOUR_SECURE_PASSWORD_HERE
DB_URL=jdbc:mysql://mysql:3306/gamers_station_marketplace

# JWT (Generate a random 64+ char string)
JWT_SECRET=YOUR_JWT_SECRET_HERE_64_CHARS_MINIMUM
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# AWS S3
AWS_ACCESS_KEY_ID=YOUR_AWS_KEY
AWS_SECRET_ACCESS_KEY=YOUR_AWS_SECRET
AWS_S3_BUCKET_NAME=gs-marketplace
AWS_S3_REGION=us-east-1

# Ably (Real-time messaging)
ABLY_API_KEY=YOUR_ABLY_KEY

# WebSocket CORS
WEBSOCKET_ALLOWED_ORIGINS=https://yourdomain.com
EOF

# Secure the .env file
chmod 600 .env
```

### 3. Verify Docker & Docker Compose

```bash
# Check Docker is installed
docker --version

# Check Docker Compose
docker-compose --version

# If not installed:
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo apt install docker-compose -y
```

### 4. Test Manual Deployment (Optional)

Before setting up CI/CD, verify manual deployment works:

```bash
cd ~/marketplace-api

# Clone repository (first time only)
git clone https://github.com/YOUR_USERNAME/marketplace-web.git .

# Start services
docker-compose up -d

# Check logs
docker-compose logs -f

# Check health
curl http://localhost:8080/api/v1/actuator/health
```

---

## Workflows

### Workflow 1: `ci.yml` - Continuous Integration
**Trigger:** Push or PR to `main` or `develop`

**Jobs:**
1. **Backend Tests** - Runs Maven tests with MySQL service
2. **Frontend Tests** - Runs ESLint and builds React app
3. **Code Quality** - Checks for TODOs, formatting, vulnerabilities
4. **Docker Build** - Tests Docker image builds
5. **CI Summary** - Reports overall status

### Workflow 2: `deploy.yml` - Deployment
**Trigger:** Push to `main` or manual dispatch

**Jobs:**
1. **Test Job** - Runs tests (backend + frontend)
2. **Build & Deploy Job** - Only runs if tests pass:
   - Builds Docker images
   - Transfers to VPS
   - Deploys with zero-downtime
   - Runs health checks

---

## Usage

### Automatic Deployment
Push to `main` branch:
```bash
git add .
git commit -m "Your changes"
git push origin main
```

GitHub Actions will:
1. Run all tests
2. Build Docker images
3. Deploy to VPS (if tests pass)
4. Send notifications

### Manual Deployment
Go to: **Actions → Deploy to VPS → Run workflow**

Select branch and click "Run workflow"

### View Logs
Go to: **Actions** tab → Click on workflow run → View job logs

---

## Monitoring

### Check Deployment Status

On your VPS:
```bash
cd ~/marketplace-api

# View running containers
docker-compose ps

# Check logs
docker-compose logs -f

# Check specific service
docker-compose logs -f app
docker-compose logs -f client
docker-compose logs -f mysql

# Check health
curl http://localhost:8080/api/v1/actuator/health
```

### Rollback on Failure

If deployment fails:
```bash
cd ~/marketplace-api

# Stop current deployment
docker-compose down

# Check available images
docker images

# Run previous version (replace with your version)
docker tag gs-marketplace-api:previous gs-marketplace-api:latest
docker-compose up -d
```

---

## Troubleshooting

### Issue: Tests Failing
**Solution:**
- Check test logs in GitHub Actions
- Run tests locally: `mvn test`
- Verify environment variables are set correctly

### Issue: SSH Connection Failed
**Solution:**
- Verify `VPS_HOST` secret is correct
- Ensure SSH key is correctly copied (including BEGIN/END lines)
- Test SSH manually: `ssh -i ~/.ssh/github-actions user@host`
- Check VPS firewall allows port 22

### Issue: Docker Build Failed
**Solution:**
- Check Dockerfile syntax
- Verify all dependencies in `pom.xml` are valid
- Check disk space on VPS: `df -h`

### Issue: Health Check Failed
**Solution:**
```bash
# On VPS, check why service is failing
docker-compose logs app

# Common causes:
# - Database not ready (wait longer)
# - Missing environment variables
# - Port conflicts
docker-compose ps
netstat -tulpn | grep 8080
```

### Issue: Out of Disk Space
**Solution:**
```bash
# Clean old Docker images
docker system prune -a --volumes

# Check space
df -h

# Remove old logs
docker-compose logs --tail=100 > logs.txt
truncate -s 0 /var/lib/docker/containers/*/*-json.log
```

---

## Best Practices

### 1. Branch Protection
Enable branch protection on `main`:
- Require status checks to pass
- Require reviews before merging
- Prevent force pushes

### 2. Environment Variables
Never commit `.env` file:
```bash
# .gitignore already includes:
.env
```

### 3. Database Backups
Set up automated backups:
```bash
# Add to crontab
crontab -e

# Backup daily at 2 AM
0 2 * * * docker exec marketplace-mysql mysqldump -u root -p$DB_PASSWORD gamers_station_marketplace | gzip > /backup/db-$(date +\%Y\%m\%d).sql.gz
```

### 4. Monitoring Alerts
Set up GitHub Actions notifications:
- **Settings → Notifications → Actions**
- Enable email/Slack notifications for failures

### 5. Security
- Rotate SSH keys regularly
- Keep Docker images updated
- Review dependency vulnerabilities weekly
- Use secrets for all sensitive data

---

## Advanced Configuration

### Add Slack Notifications

Add to end of `deploy.yml`:
```yaml
    - name: Notify Slack
      if: always()
      uses: 8398a7/action-slack@v3
      with:
        status: ${{ job.status }}
        webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

### Add Database Migrations Check

Before deployment, add:
```yaml
    - name: Check migrations
      run: |
        mvn flyway:info
        mvn flyway:validate
```

### Add Performance Tests

Create `performance-test.yml`:
```yaml
name: Performance Tests
on:
  schedule:
    - cron: '0 2 * * *'  # Daily at 2 AM
jobs:
  load-test:
    runs-on: ubuntu-latest
    steps:
      - name: Run k6 load test
        run: k6 run load-test.js
```

---

## Pipeline Metrics

Track these metrics in GitHub Actions:
- **Test Pass Rate**: Target 100%
- **Deployment Time**: Target < 5 minutes
- **Deployment Success Rate**: Target > 95%
- **Mean Time to Recovery**: Target < 15 minutes

---

## Support

### Logs Location
- **GitHub Actions**: Repository → Actions tab
- **VPS Backend**: `docker-compose logs app`
- **VPS Frontend**: `docker-compose logs client`
- **VPS Nginx**: `docker-compose logs nginx`

### Useful Commands
```bash
# View all workflows
gh workflow list

# View workflow runs
gh run list

# Watch workflow in real-time
gh run watch

# Re-run failed workflow
gh run rerun <run-id>
```

---

## Next Steps

1. ✅ Set up GitHub secrets
2. ✅ Test manual deployment on VPS
3. ✅ Push code and verify CI/CD works
4. ⬜ Set up monitoring (Prometheus + Grafana)
5. ⬜ Configure automated backups
6. ⬜ Add integration tests
7. ⬜ Set up staging environment
8. ⬜ Configure blue-green deployment

---

## Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Maven Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)
