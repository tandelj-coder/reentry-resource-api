# Community Re-entry Support API

A full-stack REST API for managing client referrals to community service programs. Built with Java 21, Spring Boot 3.x, PostgreSQL, and JWT authentication.

## Features

- **Authentication & Authorization**: JWT-based token auth with role-based access control (Admin, Caseworker, Provider)
- **Client Management**: Register and manage clients with assigned caseworkers
- **Service Provider Network**: Database of service providers and programs
- **Program Search**: Find programs by city, category, availability, and age range
- **Referral Lifecycle**: Track referrals from creation through completion with full history audit trail
- **Follow-up Management**: Schedule and monitor follow-ups with status tracking
- **Error Handling**: Global exception handling with consistent error responses
- **API Documentation**: Swagger/OpenAPI 3.0 integrated
- **Testing**: Comprehensive unit, controller, and integration tests
- **Containerization**: Docker and Docker Compose support

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| API | Spring Web, REST |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA, Hibernate |
| Authentication | Spring Security, JWT (jjwt) |
| Validation | Jakarta Bean Validation |
| Migrations | Flyway |
| Testing | JUnit 5, Mockito, MockMvc, Testcontainers |
| Documentation | Springdoc OpenAPI (Swagger UI) |
| Build | Maven |
| Container | Docker, Docker Compose |

## Prerequisites

- Java 21 or higher
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 16 (or use Docker)

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/tandelj-coder/reentry-resource-api.git
cd reentry-resource-api
```

### 2. Start PostgreSQL with Docker Compose

```bash
docker-compose up -d
```

This starts PostgreSQL on `localhost:5432` with:
- Database: `reentry_db`
- User: `reentry_user`
- Password: `changeme` (set `DB_PASSWORD` env var in production)

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or:

```bash
java -jar target/reentry-api-1.0.0.jar
```

### 5. Access the API

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **Health**: http://localhost:8080/actuator/health

## API Endpoints Overview

### Authentication
```bash
POST   /api/auth/register      # Create new user
POST   /api/auth/login         # Get JWT token
POST   /api/auth/refresh       # Refresh token
```

### Clients
```bash
POST   /api/v1/clients         # Create client (CASEWORKER)
GET    /api/v1/clients         # List clients (filtered by role)
GET    /api/v1/clients/{id}    # Get client details
PUT    /api/v1/clients/{id}    # Update client
```

### Programs
```bash
GET    /api/v1/programs        # List programs
GET    /api/v1/programs/search # Search with filters
POST   /api/v1/programs        # Create program (ADMIN)
PUT    /api/v1/programs/{id}   # Update program (ADMIN)
```

### Service Providers
```bash
GET    /api/v1/providers       # List providers
POST   /api/v1/providers       # Create provider (ADMIN)
PUT    /api/v1/providers/{id}  # Update provider (ADMIN)
```

### Referrals
```bash
POST   /api/v1/referrals       # Create referral (CASEWORKER)
GET    /api/v1/referrals/{id}  # Get referral details
PUT    /api/v1/referrals/{id}/status  # Update status (PROVIDER, ADMIN)
GET    /api/v1/referrals/{id}/history # Get history
```

### Follow-ups
```bash
POST   /api/v1/follow-ups      # Create follow-up (CASEWORKER)
GET    /api/v1/follow-ups      # List follow-ups
GET    /api/v1/follow-ups/overdue # Get overdue follow-ups
PUT    /api/v1/follow-ups/{id} # Update status
```

## Sample API Requests

### Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "SecurePass123!",
    "role": "CASEWORKER"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePass123!"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600000,
  "user": {
    "id": 1,
    "email": "john.doe@example.com",
    "role": "CASEWORKER"
  }
}
```

### Create a Program

```bash
curl -X POST http://localhost:8080/api/v1/programs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "name": "Housing First Program",
    "description": "Immediate housing followed by supportive services",
    "category": "HOUSING",
    "city": "Toronto",
    "capacity": 50,
    "availableSlots": 10,
    "minimumAge": 18,
    "maximumAge": 65,
    "cost": 0.00,
    "intakeStatus": "OPEN"
  }'
```

### Create a Referral

```bash
curl -X POST http://localhost:8080/api/v1/referrals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "clientId": 1,
    "programId": 1,
    "referralNotes": "Client needs immediate housing assistance"
  }'
```

## Project Structure

```
src/main/java/com/reentry/
├── ReentryResourceApiApplication.java      # Main entry point
├── auth/                                   # Authentication logic
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── JwtService.java
│   └── dto/
├── config/                                 # Configuration classes
│   ├── SecurityConfig.java
│   ├── JwtAuthenticationFilter.java
│   ├── OpenApiConfig.java
│   └── DataSeeder.java
├── user/                                   # User management
│   ├── User.java
│   ├── Role.java
│   ├── UserRepository.java
│   ├── UserService.java
│   └── dto/
├── client/                                 # Client management
│   ├── Client.java
│   ├── ClientNeed.java
│   ├── ClientRepository.java
│   ├── ClientService.java
│   ├── ClientController.java
│   └── dto/
├── provider/                               # Service provider management
│   ├── ServiceProvider.java
│   ├── ServiceProviderRepository.java
│   ├── ServiceProviderService.java
│   ├── ServiceProviderController.java
│   └── dto/
├── program/                                # Program management
│   ├── Program.java
│   ├── ProgramCategory.java
│   ├── IntakeStatus.java
│   ├── ProgramRepository.java
│   ├── ProgramService.java
│   ├── ProgramController.java
│   └── dto/
├── referral/                               # Referral management
│   ├── Referral.java
│   ├── ReferralStatus.java
│   ├── ReferralHistory.java
│   ├── ReferralRepository.java
│   ├── ReferralHistoryRepository.java
│   ├── ReferralService.java
│   ├── ReferralController.java
│   └── dto/
├── followup/                               # Follow-up management
│   ├── FollowUp.java
│   ├── FollowUpStatus.java
│   ├── FollowUpRepository.java
│   ├── FollowUpService.java
│   ├── FollowUpController.java
│   └── dto/
├── common/                                 # Shared utilities
│   ├── BaseEntity.java
│   ├── exception/
│   │   ├── ResourceNotFoundException.java
│   │   ├── UnauthorizedException.java
│   │   ├── BadRequestException.java
│   │   ├── ForbiddenException.java
│   │   ├── DuplicateResourceException.java
│   │   ├── InvalidStatusTransitionException.java
│   │   └── GlobalExceptionHandler.java
│   └── dto/
│       └── ErrorResponse.java
└── audit/                                  # Audit logging
    ├── AuditLog.java
    ├── AuditLogRepository.java
    └── AuditService.java

src/main/resources/
├── application.yml                         # Main configuration
├── application-dev.yml                     # Development config
├── application-prod.yml                    # Production config
└── db/migration/                           # Flyway migrations
    ├── V1__create_users.sql
    ├── V2__create_clients.sql
    ├── V3__create_providers_and_programs.sql
    ├── V4__create_referrals.sql
    ├── V5__create_followups.sql
    └── V6__create_audit_logs.sql

src/test/java/com/reentry/
├── auth/AuthServiceTest.java
├── auth/AuthControllerTest.java
├── client/ClientServiceTest.java
├── client/ClientControllerTest.java
├── program/ProgramServiceTest.java
├── program/ProgramControllerTest.java
├── referral/ReferralServiceTest.java
├── referral/ReferralIntegrationTest.java
└── followup/FollowUpServiceTest.java
```

## Database Schema

The API uses PostgreSQL with the following main tables:

- **users** - User accounts with roles and authentication
- **clients** - Clients in the re-entry program
- **service_providers** - Community service organizations
- **programs** - Specific programs offered by providers
- **referrals** - Referrals from caseworkers to programs
- **referral_history** - Audit trail of referral status changes
- **follow_ups** - Follow-up tasks for caseworkers
- **audit_logs** - Application audit trail

## Running Tests

### Unit Tests
```bash
mvn test
```

### Specific Test Class
```bash
mvn test -Dtest=ClientServiceTest
```

### Integration Tests
```bash
mvn test -Dgroups="integration"
```

### Test Coverage
```bash
mvn clean test jacoco:report
# View report at: target/site/jacoco/index.html
```

## Building with Docker

### Build the Application Image

```bash
mvn clean package -DskipTests
docker build -t reentry-api:latest .
```

### Run with Docker Compose

```bash
docker-compose -f docker-compose.yml up
```

This starts:
- PostgreSQL on port 5432
- Reentry API on port 8080

### Stop Services

```bash
docker-compose down
```

## Environment Variables

Create a `.env` file (copy from `.env.example`):

```env
DB_PASSWORD=changeme
JWT_SECRET=your-super-secret-key-change-in-production
JWT_EXPIRATION_MS=3600000
SPRING_PROFILES_ACTIVE=dev
LOG_LEVEL=INFO
```

## Deployment

The application can be deployed to:
- **Railway**: `railway up`
- **Render**: Push to GitHub, auto-deploy
- **AWS**: ECS + RDS for production
- **Docker Hub**: Push image and deploy anywhere

## Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -am 'Add feature'`
3. Push to branch: `git push origin feature/your-feature`
4. Submit a Pull Request

## Privacy & Ethical Considerations

This API handles sensitive personal information about individuals in the criminal justice system. Key safeguards:

- All data is encrypted in transit (HTTPS/TLS)
- Passwords are hashed with BCrypt (never stored plain text)
- JWT tokens expire after 1 hour
- Role-based access control prevents unauthorized data access
- Audit logs track all data access and modifications
- Database is isolated in private network

**Important**: Before using this in production, consult legal and security professionals about data privacy compliance (GDPR, HIPAA, provincial privacy laws).

## License

MIT License - See LICENSE file

## Support

For issues, questions, or contributions, please open a GitHub issue or contact the development team.

---

**Built with ❤️ for community re-entry support**
