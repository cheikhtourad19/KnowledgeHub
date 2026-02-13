# 🚀 KnowledgeHub Quick Start

## Setup in 3 Commands

```bash
# 1. Copy template
cp .env .env

# 2. Edit your secrets
nano .env

# 3. Start everything
docker-compose up -d
```

## Minimum Required Variables

```env
# Database
MYSQL_ROOT_PASSWORD=your_password
MYSQL_USERNAME=root
MYSQL_PASSWORD=your_password

# MongoDB
MONGO_ROOT_USERNAME=admin
MONGO_ROOT_PASSWORD=your_mongo_pass

# JWT
JWT_SECRET=$(openssl rand -base64 32)
JWT_EXPIRATION=86400000

# MinIO
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin_secure

# Email (Optional)
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password
```

## Useful Commands

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f [service-name]

# Restart a service
docker-compose restart [service-name]

# Check status
docker-compose ps

# View configuration
docker-compose config

# Start specific services
docker-compose up mysql mongodb minio

# Rebuild after code changes
docker-compose up --build

# Remove volumes (⚠️ deletes data!)
docker-compose down -v
```

## Service URLs

| Service | URL |
|---------|-----|
| API Gateway | http://localhost:8080 |
| Auth Service | http://localhost:8081 |
| User Service | http://localhost:8082 |
| Resource Service | http://localhost:8083 |
| Notification Service | http://localhost:8084 |
| AI Search Service | http://localhost:8000 |
| MinIO Console | http://localhost:9001 |

## Health Checks

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8000/api/v1/health
```

## API Documentation (Swagger)

```bash
# After services start, visit:
http://localhost:8081/swagger-ui.html  # Auth
http://localhost:8082/swagger-ui.html  # User
http://localhost:8083/swagger-ui.html  # Resource
http://localhost:8084/swagger-ui.html  # Notification
http://localhost:8000/docs             # AI Search (FastAPI)
```

## Troubleshooting

### Service won't start
```bash
# Check logs
docker-compose logs [service-name]

# Common fixes:
# 1. Check .env file exists
# 2. Verify required variables are set
# 3. Ensure ports aren't already in use
# 4. Wait for dependencies (MySQL, MongoDB)
```

### Database connection failed
```bash
# Verify MySQL is healthy
docker-compose ps mysql

# Check MySQL logs
docker-compose logs mysql

# Connect to MySQL
docker-compose exec mysql mysql -uroot -p
```

### Port already in use
```bash
# Find what's using port 8080
lsof -i :8080

# Kill process or change port in .env
API_GATEWAY_PORT=8090
```

## Security Checklist

- [ ] `.env` is in `.gitignore`
- [ ] Strong passwords (not defaults)
- [ ] JWT_SECRET is 256+ bits
- [ ] Different configs for dev/prod
- [ ] SMTP credentials are app-specific
- [ ] Never commit `.env` to Git

## Generate Secure Secrets

```bash
# JWT Secret (256 bits)
openssl rand -base64 32

# Strong Password (192 bits)
openssl rand -base64 24

# UUID
uuidgen
```

## Production Deployment

```bash
# Use production env file
docker-compose --env-file .env.production up -d

# Scale services
docker-compose up -d --scale resource-service=3

# View production logs
docker-compose logs --tail=100 -f
```

## Backup & Restore

```bash
# Backup MySQL
docker-compose exec mysql mysqldump -uroot -p knowledgehub_auth > backup.sql

# Backup MongoDB
docker-compose exec mongodb mongodump --out=/backup

# Backup MinIO (volumes)
docker run --rm -v knowledgehub-minio-data:/data -v $(pwd):/backup alpine tar czf /backup/minio-backup.tar.gz /data
```

## Files Structure

```
KnowledgeHub/
├── .env                    # ❌ Your secrets (NOT in Git)
├── .env.example            # ✅ Template (in Git)
├── docker-compose.yml      # ✅ Orchestration
├── ENV_SETUP.md           # ✅ Full documentation
└── application.yml.example # ✅ Spring config template
```

## Need Help?

1. Read `ENV_SETUP.md` for detailed guide
2. Check `.env.example` for all variables
3. View logs: `docker-compose logs -f`
4. Verify config: `docker-compose config`

---

**Quick Links:**
- [Full Documentation](./ENV_SETUP.md)
- [API Endpoints](./API_ENDPOINTS.md)
- [Architecture](./PROJECT_ARCHITECTURE.md)
