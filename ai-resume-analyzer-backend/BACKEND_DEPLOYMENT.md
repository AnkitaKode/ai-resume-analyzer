# Backend Deployment Guide

## Docker Deployment (Recommended)

### 1. Using Docker Compose

```bash
# From project root
# Set your OpenAI API key
export OPENAI_API_KEY=your-openai-api-key

# Build and start all services
docker-compose up --build

# Or run in background
docker-compose up --build -d
```

### 2. Standalone Docker Build

```bash
# Navigate to backend directory
cd ai-resume-analyzer-backend

# Build the Docker image
docker build -t ai-resume-analyzer-backend .

# Run with environment variables
docker run -p 8080:8080 \
  -e OPENAI_API_KEY=your-openai-api-key \
  -e DATABASE_URL=jdbc:postgresql://your-db-host:5432/resumeai \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=your-password \
  ai-resume-analyzer-backend
```

## Environment Variables

### Required Variables

- `OPENAI_API_KEY`: Your OpenAI API key for AI functionality
- `DATABASE_URL`: PostgreSQL database connection string
- `DATABASE_USERNAME`: Database username
- `DATABASE_PASSWORD`: Database password

### Optional Variables

- `CORS_ALLOWED_ORIGINS`: Comma-separated list of allowed frontend origins
- `SPRING_PROFILES_ACTIVE`: Spring profile (default: prod)

## Database Setup

### PostgreSQL

The application uses PostgreSQL. Ensure you have:

1. **PostgreSQL Server** (version 12+)
2. **Database**: `resumeai`
3. **User**: `postgres` (or your preferred user)

#### Local Setup

```sql
CREATE DATABASE resumeai;
CREATE USER postgres WITH PASSWORD 'your-password';
GRANT ALL PRIVILEGES ON DATABASE resumeai TO postgres;
```

#### Docker PostgreSQL

```bash
docker run -d \
  --name postgres \
  -e POSTGRES_DB=resumeai \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=your-password \
  -p 5432:5432 \
  postgres:15-alpine
```

## Traditional Deployment

### 1. Build the Application

```bash
cd ai-resume-analyzer-backend

# Build JAR file
./mvnw clean package -DskipTests

# The JAR will be in target/ directory
```

### 2. Run with Java

```bash
# Set environment variables
export OPENAI_API_KEY=your-openai-api-key
export DATABASE_URL=jdbc:postgresql://localhost:5432/resumeai
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=your-password

# Run the application
java -jar target/ai-resume-analyzer-0.0.1-SNAPSHOT.jar
```

## Cloud Deployment

### AWS ECS

1. Push Docker image to AWS ECR
2. Create ECS task definition with environment variables
3. Deploy to ECS cluster

### Google Cloud Run

```bash
# Build and push to Google Container Registry
gcloud builds submit --tag gcr.io/PROJECT-ID/ai-resume-analyzer-backend

# Deploy to Cloud Run
gcloud run deploy ai-resume-analyzer-backend \
  --image gcr.io/PROJECT-ID/ai-resume-analyzer-backend \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars OPENAI_API_KEY=your-key,DATABASE_URL=your-db-url
```

### Azure Container Instances

```bash
# Create Azure Container Instance
az container create \
  --resource-group myResourceGroup \
  --name ai-resume-analyzer-backend \
  --image your-registry/ai-resume-analyzer-backend:latest \
  --cpu 1 --memory 2 \
  --environment-variables OPENAI_API_KEY=your-key DATABASE_URL=your-db-url
```

## Health Checks

The application exposes health endpoints:

- `GET /actuator/health` - Application health status
- `GET /actuator/info` - Application information

## API Documentation

Once deployed, API documentation is available at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

## Security Considerations

1. **Never commit API keys** to version control
2. **Use HTTPS** in production
3. **Configure firewall** to only allow necessary ports
4. **Use environment variables** for sensitive configuration
5. **Regularly update dependencies** for security patches

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Check database URL, username, password
   - Ensure database is running and accessible
   - Verify network connectivity

2. **OpenAI API Errors**
   - Verify API key is valid and has credits
   - Check OpenAI service status

3. **Memory Issues**
   - Increase JVM heap size: `-Xmx1g`
   - Check available system memory

### Debug Commands

```bash
# Check container logs
docker logs ai-resume-analyzer-backend

# Check health status
curl http://localhost:8080/actuator/health

# View application logs
docker-compose logs backend
```

## Production Optimization

1. **Use reverse proxy** (nginx/Apache) for SSL termination
2. **Enable connection pooling** for database
3. **Configure monitoring** and alerting
4. **Set up backup strategy** for database
5. **Use load balancer** for high availability
