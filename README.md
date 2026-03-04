# Mini TNG - E-Wallet Practice Project

A Spring Boot microservices-based e-wallet application with production-grade OTP verification system. Built for learning modern Spring Boot development, security best practices, and microservices architecture.

## 🎯 Project Status

**Current Phase**: Wallet & Transaction Services / Observability ✅
- User Registration with OTP Verification
- JWT-based Authentication
- Wallet Management (Transfer, Idempotency)
- Observability Stack (Prometheus, Grafana)

## ✨ Features

### Implemented ✅

#### Authentication & Security
- User registration with email verification
- OTP-based email verification (BCrypt hashed)
- JWT access tokens (15 min expiry)
- Refresh tokens (7 day expiry)
- HTTP-only secure cookies
- Rate limiting with resend tracking

#### Wallet & Transactions
- Create Wallet for users
- Intra-wallet transfers (TransferOut -> TransferIn)
- **Saga Pattern** for distributed transaction safety
- **Idempotency** using Transaction IDs
- **Compensating Transactions** (Rollback) on failure
- Redis Distributed Locking (Redisson)

#### Observability & Monitoring
- **Prometheus** for metric scraping
- **Grafana** for dashboard visualization
- **Micrometer** for custom application metrics
- `@Timed` annotation for performance tracking
- Actuator endpoints for health/info

#### Email Service
- HTML email templates with Thymeleaf
- Asynchronous email sending
- MailHog integration for testing
- Welcome emails on successful verification
- OTP verification emails

#### OTP System (Production-Ready)
- Secure OTP generation (6 digits)
- BCrypt hashing for storage
- 10-minute expiration
- Retry limit (5 attempts per OTP)
- Resend limit (5 resends per hour with 60s cooldown)
- Automatic cleanup of expired tokens

#### Infrastructure
- PostgreSQL with JPA/Hibernate
- Virtual threads enabled (Java 21+)
- Docker Compose for services
- Scheduled tasks for maintenance
- **Kafka** for event-driven architecture

### Planned 🔜

- Payment Integration (Stripe)
- Enhanced Dashboard UI
- Mobile API optimization

## 🛠️ Tech Stack

- **Java**: 21+
- **Framework**: Spring Boot 4.0.1
- **Database**: PostgreSQL 18
- **Email**: MailHog (dev), SendGrid (prod)
- **Security**: Spring Security, JWT, BCrypt
- **Build**: Maven
- **Containerization**: Docker Compose
- **Template Engine**: Thymeleaf

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker & Docker Compose
- IDE (IntelliJ IDEA / VS Code recommended)

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd mhpractice
```

### 2. Start Services

**Option A: Development Mode (Hybrid)**
Runs infrastructure in Docker, Backend runs locally (Faster for coding).
```bash
docker-compose --profile dev up -d
```

Run the Application
```bash
mvn spring-boot:run
```

**Option B: Full Docker Mode (Production Simulation)**
Runs EVERYTHING in Docker containers (Best for final testing).
*Note: Stop local Maven run to free up port 8088 first!*
```bash
docker-compose --profile prod up --build -d
```

### 4. Access Services
- **API**: http://localhost:8088
- **Grafana**: http://localhost:3001 (Login: admin/admin)
- **Prometheus**: http://localhost:9091
- **MailHog UI**: http://localhost:8026
- **pgAdmin**: http://localhost:5055 (if using dev profile)
## 📊 Service Ports

| Service         | Port | Description      |
| --------------- | ---- | ---------------- |
| Spring Boot API | 8088 | Main application |
| PostgreSQL      | 5435 | Database         |
| Redis           | 6380 | Distributed Lock |
| Kafka Broker    | 9092 | Event Streaming  |
| Prometheus      | 9091 | Metrics Scraper  |
| Grafana         | 3001 | Dashboards       |
| MailHog SMTP    | 1026 | Email server     |
| MailHog Web UI  | 8026 | Email viewer     |
| pgAdmin         | 5055 | Database UI      |

## 🔐 API Endpoints

### Authentication

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "name": "John Doe",
  "password": "password123"
}
```

**Response**: OTP sent to email

#### Verify OTP
```http
POST /api/auth/verify
Content-Type: application/json

{
  "email": "user@example.com",
  "otpCode": "123456"
}
```

**Response**: `true` if verified, welcome email sent

#### Resend OTP
```http
POST /api/auth/resend-otp
Content-Type: application/json

{
  "email": "user@example.com"
}
```

**Rate Limits**: Max 5 resends/hour with 60s cooldown

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response**: JWT tokens in cookies

#### Refresh Token
```http
POST /api/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "your-refresh-token"
}
```

#### Logout
```http
POST /api/auth/logout
```

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    role VARCHAR(50) DEFAULT 'USER',
    status VARCHAR(50) DEFAULT 'ACTIVE',
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);
```

### Verification Tokens Table
```sql
CREATE TABLE verification_tokens (
    id UUID PRIMARY KEY,
    otp_code VARCHAR(255) NOT NULL,
    user_id UUID REFERENCES users(id),
    purpose VARCHAR(50) NOT NULL,
    attempt_count INT DEFAULT 0,
    max_attempts INT DEFAULT 5,
    used BOOLEAN DEFAULT FALSE,
    ip_address VARCHAR(45),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
```

### OTP Resend Tracking Table
```sql
CREATE TABLE otp_resend_tracking (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    resend_count INT DEFAULT 0,
    max_resend INT DEFAULT 5,
    resend_interval INT DEFAULT 60,
    last_resend_at TIMESTAMP,
    reset_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
```

## 🧪 Testing

### Manual Testing with MailHog

1. Start services: `docker-compose up -d`
2. Register a user via POST `/api/auth/register`
3. Open MailHog: http://localhost:8026
4. Copy OTP from email
5. Verify via POST `/api/auth/verify`
6. Check for welcome email

### Testing OTP Features

**Retry Limit Test:**
- Enter wrong OTP 5 times → Should reject 6th attempt

**Resend Limit Test:**
- Request OTP resend 5 times within an hour → 6th should fail

**Cooldown Test:**
- Resend OTP twice within 60s → Second should fail

## 📁 Project Structure

```
mhpractice/
├── src/main/java/com/example/mhpractice/
│   ├── common/
│   │   ├── exception/         # Error handling
│   │   ├── http/             # Response wrappers
│   │   └── service/          # CleanupService
│   └── features/
│       ├── user/
│       │   ├── models/       # User, VerificationToken
│       │   ├── repository/   # JPA repositories
│       │   ├── service/      # AuthService, OtpService
│       │   └── controller/   # AuthController
│       └── notification/
│           ├── model/        # Notification entity
│           ├── service/      # Email sending
│           └── templates/    # Thymeleaf templates
├── src/main/resources/
│   ├── application.yaml           # Base config
│   ├── application-local.yaml    # Development
│   ├── application-prod.yaml     # Production
│   └── templates/email/          # Email templates
├── compose.yaml                   # Docker services
├── pom.xml                       # Maven dependencies
└── README.md                     # This file
```

## ⚙️ Configuration

### Development (application-local.yaml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5435/mhpractice
  mail:
    host: localhost
    port: 1026

app:
  jwt:
    expiration-ms: 900000  # 15 mins
  scheduler:
    cleanup-cron: "0 0 0 * * ?"  # Daily at midnight
```

### Production (application-prod.yaml)
Use environment variables:
- `DATABASE_URL`
- `SENDGRID_API_KEY`
- `JWT_SECRET`

## 🔧 Maintenance

### Scheduled Jobs

**Daily Cleanup (Midnight)**
- Deletes expired OTP tokens
- Removes old resend tracking (24h+)

### Manual Cleanup
```bash
# Reset database (WARNING: deletes all data)
docker-compose down -v
docker-compose up -d
```

## 🐛 Common Issues

### Port Already in Use
```bash
# Check what's using the port
netstat -ano | findstr :5435

# Stop conflicting services
docker-compose down
```

### Email Not Sending
- Check MailHog is running: `docker ps`
- Verify config: `application-local.yaml`
- Check logs for errors

### Database Connection Failed
- Ensure PostgreSQL is running
- Check port mapping in `compose.yaml`
- Verify credentials match config

## 📚 Learning Resources

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Spring Security JWT](https://www.baeldung.com/spring-security-jwt)
- [Thymeleaf](https://www.thymeleaf.org/)
- [PostgreSQL](https://www.postgresql.org/docs/)

## 🎓 What I Learned

- Production-grade OTP implementation
- JWT authentication with refresh tokens
- Email templating with Thymeleaf
- Spring Security configuration
- Docker Compose for development
- Scheduled tasks in Spring
- Rate limiting strategies
- Database schema design
- CI/CD with GitHub Actions
- AWS EC2 deployment with Docker

## 🚀 Deployment

### CI/CD with GitHub Actions

This project includes automated deployment via GitHub Actions. On every push to `master`, the pipeline:

1. **Builds** the JAR using Maven
2. **Builds** a Docker image
3. **Pushes** the image to Docker Hub
4. **Deploys** to AWS EC2 via SSH

#### Required GitHub Secrets

| Secret               | Description                      |
| -------------------- | -------------------------------- |
| `DOCKERHUB_USERNAME` | Your Docker Hub username         |
| `DOCKERHUB_TOKEN`    | Docker Hub access token          |
| `SERVER_IP`          | AWS EC2 public IP address        |
| `SSH_PRIVATE_KEY`    | Contents of your `.pem` key file |

### AWS EC2 Deployment

**Instance Requirements:**
- Ubuntu 24.04 LTS
- Current using `m7i-flex.large` (min 4gb ram)
- 20GB+ storage
- Security Group: Ports 22 (SSH), 8088 (API) open

**Initial Server Setup:**
```bash
# Install Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker ubuntu

# Create project directory
mkdir ~/mhpractice && cd ~/mhpractice

# Create .env file with production secrets
nano .env
```

**Required `.env` variables on server:**
```env
POSTGRES_DB=mhpractice
POSTGRES_USER=your_user
POSTGRES_PASSWORD=your_password
JWT_SECRET=your_64_char_secret
JWT_ENCRYPTION_KEY=your_32_char_key
SENDGRID_API_KEY=your_sendgrid_key
DOCKERHUB_USERNAME=your_dockerhub_user
```

### Local Kubernetes (Minikube) Deployment

For local testing before cloud deployment, you can use Minikube to simulate a Kubernetes environment.

**1. Start Minikube & Load Image**
```bash
minikube start
docker build -t springboot-app:1.0 .
minikube image load springboot-app:1.0
```

**2. Deploy to Kubernetes**
```bash
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
```

**3. Verify**
```bash
kubectl get pods
```

*(Note: The `deployment.yaml` is configured to use `host.docker.internal` so Minikube pods can communicate with local Docker Compose infrastructure like Postgres and Kafka).*

### Enterprise Cloud Deployment (AWS EKS & ECR)

For a production environment, Kubernetes deployments rely on cloud container registries rather than local images.

**1. Push Image to Container Registry (AWS ECR)**
```bash
# Login to AWS ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <your-aws-account-id>.dkr.ecr.us-east-1.amazonaws.com

# Tag your local image
docker tag springboot-app:1.0 <your-aws-account-id>.dkr.ecr.us-east-1.amazonaws.com/mhpractice/springboot-app:1.0

# Push the image to the cloud
docker push <your-aws-account-id>.dkr.ecr.us-east-1.amazonaws.com/mhpractice/springboot-app:1.0
```

**2. Update `deployment.yaml`**
*   Update `image:` to point directly to your ECR URL: `<your-aws-account-id>.dkr.ecr.us-east-1.amazonaws.com/mhpractice/springboot-app:1.0`
*   Change `imagePullPolicy: Never` to `imagePullPolicy: Always`
*   Update your Database/Kafka/Redis URLs from `host.docker.internal` to the actual cloud provider endpoints (e.g., AWS RDS endpoint, AWS ElastiCache endpoint).

**3. Deploy to EKS**
```bash
# Connect kubectl to your AWS EKS cluster
aws eks update-kubeconfig --region us-east-1 --name my-cluster-name

# Apply the manifests
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
```

### Production Access

After deployment:
- **API**: `http://<server-ip>:8088/api/...`
- **Grafana**: `http://<server-ip>:3001`
- **Prometheus**: `http://<server-ip>:9091`

## 📝 License

This is a practice project for learning purposes.

## 👤 Author

Built as part of Spring Boot learning journey.

---

**Happy Coding!** 🚀
