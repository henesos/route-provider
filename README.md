# Aviation Route Planning System

Full-stack application for aviation route planning with React frontend and Spring Boot backend.

## Quick Start with Docker

```bash
# Start everything with one command
docker-compose up --build

# Or run in background
docker-compose up --build -d
```

**Access the application:**
- Frontend: http://localhost
- Backend API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/api/v1/swagger-ui.html

## Default Users

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| agency | agency123 | AGENCY |

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Frontend                             │
│                    React + Vite + Tailwind                   │
│                      (Port 80/NGINX)                         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                         Backend                              │
│                  Spring Boot + Spring Security               │
│                       (Port 8080)                            │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │    API       │  │ Application  │  │   Domain     │      │
│  │   Layer      │  │   Layer      │  │   Layer      │      │
│  │  (REST API)  │  │  (Services)  │  │  (Entities)  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                              │                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Infrastructure Layer                     │   │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐     │   │
│  │  │ PostgreSQL │  │   Redis    │  │   JWT      │     │   │
│  │  │  (JPA)     │  │  (Cache)   │  │  (Auth)    │     │   │
│  │  └────────────┘  └────────────┘  └────────────┘     │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Tech Stack

### Frontend
- React 18
- Vite 5
- Tailwind CSS 3
- React Router 6
- Axios

### Backend
- Java 17
- Spring Boot 3.2
- Spring Security + JWT
- Spring Data JPA
- Spring Data Redis
- PostgreSQL
- Hexagonal Architecture (DDD)

## Development Setup

### Prerequisites
- Node.js 18+
- Java 17+
- Docker & Docker Compose

### Run Locally (Without Docker)

```bash
# Start database services only
docker-compose up postgres redis -d

# Backend
cd backend
./mvnw spring-boot:run

# Frontend (in another terminal)
cd frontend
npm install
npm run dev
```

- Frontend: http://localhost:5173
- Backend: http://localhost:8080

## API Endpoints

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| POST | /auth/login | User authentication | Public |
| GET | /locations | List locations (paginated) | Public |
| POST | /locations | Create location | ADMIN |
| PUT | /locations/{id} | Update location | ADMIN |
| DELETE | /locations/{id} | Delete location | ADMIN |
| GET | /transportations | List transportations (paginated) | ADMIN |
| POST | /transportations | Create transportation | ADMIN |
| PUT | /transportations/{id} | Update transportation | ADMIN |
| DELETE | /transportations/{id} | Delete transportation | ADMIN |
| POST | /routes/search | Search routes | AGENCY |

## Environment Variables

### Backend
| Variable | Default | Description |
|----------|---------|-------------|
| JWT_SECRET | - | JWT signing key (min 32 chars) |
| JWT_EXPIRATION | 86400000 | Token expiration (ms) |
| DB_HOST | localhost | PostgreSQL host |
| DB_PORT | 5432 | PostgreSQL port |
| DB_NAME | routeprovider | Database name |
| DB_USERNAME | postgres | Database user |
| DB_PASSWORD | postgres | Database password |
| REDIS_HOST | localhost | Redis host |
| REDIS_PORT | 6379 | Redis port |

## Project Structure

```
route-provider-final/
├── docker-compose.yml      # Root compose file
├── README.md
├── backend/
│   ├── src/
│   │   ├── main/java/
│   │   │   └── com/aviation/routeprovider/
│   │   │       ├── api/           # REST controllers, DTOs
│   │   │       ├── application/   # Services, Ports
│   │   │       ├── domain/        # Entities, Value Objects
│   │   │       └── infrastructure/# Adapters, Config
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
└── frontend/
    ├── src/
    │   ├── components/
    │   ├── contexts/
    │   ├── layouts/
    │   ├── pages/
    │   └── services/
    ├── package.json
    ├── vite.config.js
    ├── tailwind.config.js
    └── Dockerfile
```

---
**Version**: Final
**Date**: 2026-03-21
