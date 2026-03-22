# Route Provider - Aviation Route Planning System

A production-grade Spring Boot backend for calculating multi-modal transportation routes in the aviation industry.

## Architecture

This project follows **Hexagonal Architecture (Ports & Adapters)** with **DDD-lite** principles.

```
┌─────────────────────────────────────────────────────────────────────┐
│                          API LAYER                                   │
│  (REST Controllers, DTOs, Mappers, Exception Handling, Security)    │
└─────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       APPLICATION LAYER                              │
│         (Use Cases, Ports, Services, Commands, Queries)             │
└─────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          DOMAIN LAYER                                │
│     (Entities, Value Objects, Domain Services, Route Engine)        │
└─────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       INFRASTRUCTURE LAYER                           │
│    (Persistence Adapters, JPA Entities, Redis Cache, Config)        │
└─────────────────────────────────────────────────────────────────────┘
```

## Technology Stack

| Component | Technology |
|-----------|------------|
| **Java** | 17 |
| **Framework** | Spring Boot 3.2.3 |
| **ORM** | Spring Data JPA / Hibernate |
| **Database** | PostgreSQL 15 |
| **Cache** | Redis 7 |
| **Security** | Spring Security + JWT |
| **API Doc** | SpringDoc OpenAPI 3 |
| **Container** | Docker |

## Project Structure

```
route-provider/
├── src/main/java/com/aviation/routeprovider/
│   ├── api/rest/                    # API Layer
│   │   ├── controller/              # REST Controllers
│   │   ├── dto/                     # Data Transfer Objects
│   │   ├── mapper/                  # DTO Mappers
│   │   └── exception/               # Exception Handling
│   ├── api/security/                # Security Components
│   ├── application/                 # Application Layer
│   │   ├── port/in/                 # Primary Ports (Use Cases)
│   │   ├── port/out/                # Secondary Ports (Repositories)
│   │   └── service/                 # Use Case Implementations
│   ├── domain/                      # Domain Layer
│   │   ├── model/entity/            # Domain Entities
│   │   ├── model/valueobject/       # Value Objects
│   │   ├── service/                 # Domain Services
│   │   └── exception/               # Domain Exceptions
│   └── infrastructure/              # Infrastructure Layer
│       ├── persistence/             # Database Adapters
│       ├── cache/                   # Cache Adapters
│       └── config/                  # Configuration
├── src/main/resources/
│   ├── application.yml              # Development Config
│   └── application-prod.yml         # Production Config
├── src/test/                        # Test Sources
├── Dockerfile                       # Docker Build File
├── docker-compose.yml               # Docker Compose Configuration
├── pom.xml                          # Maven Configuration
└── README.md                        # This File
```

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Option 1: Run with Docker Compose (Recommended)

```bash
# Build and start all services
docker-compose up -d

# Check logs
docker-compose logs -f app
```

The application will be available at: `http://localhost:8080/api/v1`

### Option 2: Run Locally

1. **Start Infrastructure Services:**
```bash
# PostgreSQL
docker run -d --name postgres \
  -p 5432:5432 \
  -e POSTGRES_DB=routeprovider \
  -e POSTGRES_PASSWORD=postgres \
  postgres:15-alpine

# Redis
docker run -d --name redis \
  -p 6379:6379 \
  redis:7-alpine
```

2. **Build and Run Application:**
```bash
# Build
mvn clean package -DskipTests

# Run
java -jar target/route-provider-1.0.0-SNAPSHOT.jar
```

## API Documentation

Access Swagger UI at: `http://localhost:8080/api/v1/swagger-ui.html`

## API Endpoints

### Authentication

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| POST | `/auth/login` | User login | Public |

### Locations (Admin Only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/locations` | List all locations |
| GET | `/locations/{id}` | Get location by ID |
| POST | `/locations` | Create location |
| PUT | `/locations/{id}` | Update location |
| DELETE | `/locations/{id}` | Delete location |

### Transportations (Admin Only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/transportations` | List all transportations |
| GET | `/transportations/{id}` | Get transportation by ID |
| POST | `/transportations` | Create transportation |
| PUT | `/transportations/{id}` | Update transportation |
| DELETE | `/transportations/{id}` | Delete transportation |

### Routes (Admin + Agency)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/routes/search` | Search valid routes |

## Route Calculation Rules

A valid route must satisfy ALL of the following:

1. **Exactly one FLIGHT** - No more, no less
2. **Maximum 3 transportations** total
3. **Maximum 1 pre-flight transfer** - Before the flight
4. **Maximum 1 post-flight transfer** - After the flight
5. **All transportations connected** - Arrival matches next departure
6. **All transportations operate on selected date**

### Valid Route Patterns

```
✓ FLIGHT only
✓ GROUND → FLIGHT
✓ FLIGHT → GROUND
✓ GROUND → FLIGHT → GROUND
```

### Invalid Route Patterns

```
✗ GROUND → GROUND (no flight)
✗ FLIGHT → FLIGHT (multiple flights)
✗ GROUND → GROUND → FLIGHT (multiple pre-flight)
✗ FLIGHT → GROUND → GROUND (multiple post-flight)
```

## Example Usage

### Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "role": "ADMIN",
  "username": "admin"
}
```

### Create Location

```bash
curl -X POST http://localhost:8080/api/v1/locations \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Istanbul Airport",
    "country": "Turkey",
    "city": "Istanbul",
    "locationCode": "IST"
  }'
```

### Search Routes

```bash
curl -X POST http://localhost:8080/api/v1/routes/search \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "originLocationId": 1,
    "destinationLocationId": 5,
    "travelDate": "2025-03-12"
  }'
```

## Default Users

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| agency | agency123 | AGENCY |

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | Database URL | `jdbc:postgresql://localhost:5432/routeprovider` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `postgres` |
| `SPRING_DATA_REDIS_HOST` | Redis host | `localhost` |
| `SPRING_DATA_REDIS_PORT` | Redis port | `6379` |
| `JWT_SECRET` | JWT signing key | (see application.yml) |
| `JWT_EXPIRATION` | Token expiration (ms) | `86400000` |

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Health Check

```bash
curl http://localhost:8080/api/v1/swagger-ui.html
```

## Troubleshooting

### Database Connection Issues

```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check connection
docker exec -it route-provider-db psql -U postgres -d routeprovider
```

### Redis Connection Issues

```bash
# Check Redis is running
docker ps | grep redis

# Test connection
docker exec -it route-provider-redis redis-cli ping
```

## License

MIT License

---

**Document Version:** 1.0.0  
**Last Updated:** 2025-01-21
