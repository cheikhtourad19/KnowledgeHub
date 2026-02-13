# Environment Variables Setup Guide

## 📋 Overview

This guide explains how to configure environment variables for the KnowledgeHub microservices architecture. **Environment variables are the recommended way to configure your microservices** instead of hardcoding values in `application.yml` files.

## 🔐 Why Use Environment Variables?

### Benefits:
1. **Security**: Keep secrets out of version control
2. **Flexibility**: Different configs for dev/staging/prod
3. **12-Factor App**: Industry best practice
4. **Docker-Ready**: Easy container orchestration
5. **CI/CD Friendly**: Simple deployment automation

## 🚀 Quick Start

### 1. Copy the Template

```bash
cp .env .env
```

### 2. Edit Your `.env` File

```bash
nano .env
# or
vim .env
# or use your favorite editor
```

### 3. Fill in Your Values

**Minimum required for local development:**

```env
# Database
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_USERNAME=root
MYSQL_PASSWORD=your_secure_password

# MongoDB
MONGO_ROOT_USERNAME=admin
MONGO_ROOT_PASSWORD=your_mongo_password

# JWT
JWT_SECRET=your_very_long_secret_key_at_least_256_bits
JWT_EXPIRATION=86400000

# MinIO
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin_secure

# Email (optional for testing)
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password
```

## 📁 File Structure

```
KnowledgeHub/
├── .env                    # YOUR LOCAL CONFIG (NOT IN GIT!)
├── .env.example            # Template (in Git)
├── docker-compose.yml      # References .env variables
└── */src/main/resources/
    └── application.yml     # Uses ${ENV_VAR} placeholders
```

## 🔧 How It Works

### 1. In application.yml (Spring Boot Services)

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${MYSQL_USERNAME}
    password: ${MYSQL_PASSWORD}
    
jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION}

minio:
  endpoint: ${MINIO_ENDPOINT}
  accessKey: ${MINIO_ACCESS_KEY}
  secretKey: ${MINIO_SECRET_KEY}
```

### 2. In docker-compose.yml

```yaml
services:
  auth-service:
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_AUTH_DB}
      MYSQL_USERNAME: ${MYSQL_USERNAME}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
```

### 3. Docker Compose automatically loads `.env`

When you run `docker-compose up`, it automatically reads `.env` file from the same directory.

## 🔑 Critical Variables by Service

### Auth Service
```env
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000
MYSQL_HOST=mysql
MYSQL_AUTH_DB=knowledgehub_auth
MYSQL_USERNAME=root
MYSQL_PASSWORD=your_password
```

### Resource Service
```env
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin_secure
MYSQL_RESOURCE_DB=knowledgehub_ressource
```

### Notification Service
```env
MONGO_URI=mongodb://admin:password@mongodb:27017/notification_db?authSource=admin
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password
```

### AI & Search Service
```env
MINIO_ENDPOINT=http://minio:9000
CHROMA_PERSIST_DIRECTORY=/app/chroma_db
EMBEDDING_MODEL=all-MiniLM-L6-v2
CHUNK_SIZE=1000
```

## 🛠️ Different Environments

### Local Development
```bash
cp .env .env
# Edit .env with local values
docker-compose up
```

### Staging
```bash
cp .env .env.staging
# Edit .env.staging
docker-compose --env-file .env.staging up
```

### Production
```bash
cp .env .env.production
# Edit .env.production with production values
docker-compose --env-file .env.production up -d
```

## 🔒 Security Best Practices

### ✅ DO:
- Keep `.env` in `.gitignore`
- Use strong, unique passwords
- Use different values for dev/staging/prod
- Rotate secrets regularly
- Use secret management tools in production (AWS Secrets Manager, HashiCorp Vault)

### ❌ DON'T:
- Commit `.env` files to Git
- Share `.env` files via email/Slack
- Use default passwords in production
- Store secrets in application.yml
- Copy production secrets to development

## 📝 Required Environment Variables Checklist

### Infrastructure
- [ ] `MYSQL_ROOT_PASSWORD` - MySQL root password
- [ ] `MONGO_ROOT_PASSWORD` - MongoDB root password
- [ ] `MINIO_ROOT_PASSWORD` - MinIO root password

### Security
- [ ] `JWT_SECRET` - JWT signing secret (min 256 bits)
- [ ] `JWT_EXPIRATION` - Token expiry time (ms)

### Email
- [ ] `SMTP_USERNAME` - Email username
- [ ] `SMTP_PASSWORD` - Email password (app-specific)

### Service URLs
- [ ] All inter-service URLs configured

## 🧪 Testing Your Configuration

### 1. Validate .env file
```bash
# Check if .env exists
ls -la .env

# Verify required variables are set
grep -E "JWT_SECRET|MYSQL_ROOT_PASSWORD|MONGO_ROOT_PASSWORD" .env
```

### 2. Test Docker Compose
```bash
# Validate compose file
docker-compose config

# Start only infrastructure
docker-compose up mysql mongodb minio

# Check logs
docker-compose logs -f mysql
```

### 3. Verify Services
```bash
# After starting all services
docker-compose ps

# Test health endpoints
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

## 🔧 Troubleshooting

### Problem: Environment variable not found

**Solution:**
```bash
# Check if .env exists
ls -la .env

# Verify variable is defined
cat .env | grep VARIABLE_NAME

# Restart docker-compose
docker-compose down
docker-compose up
```

### Problem: Can't connect to database

**Solution:**
```bash
# Check connection string
echo $SPRING_DATASOURCE_URL

# Verify MySQL is running
docker-compose ps mysql

# Check MySQL logs
docker-compose logs mysql
```

### Problem: Authentication fails

**Solution:**
```bash
# Verify JWT secret is set
echo $JWT_SECRET

# Should be at least 256 bits (32+ characters)
# Regenerate if needed:
openssl rand -base64 32
```

## 📚 Additional Resources

- [The Twelve-Factor App - Config](https://12factor.net/config)
- [Docker Compose Environment Variables](https://docs.docker.com/compose/environment-variables/)
- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)

## 🆘 Need Help?

1. Check `.env.example` for all available variables
2. Review docker-compose.yml for variable usage
3. Check service logs: `docker-compose logs [service-name]`
4. Verify variable precedence: .env → docker-compose.yml → application.yml

## 🎯 Production Deployment

For production, consider:

1. **AWS Systems Manager Parameter Store**
2. **HashiCorp Vault**
3. **Kubernetes Secrets**
4. **Azure Key Vault**
5. **Google Cloud Secret Manager**

Never use plain `.env` files in production servers!

---

**Last Updated:** February 2026  
**For:** KnowledgeHub Microservices Platform
