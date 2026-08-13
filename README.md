# FreightTrack - Logistics Management System

A modern, full-stack logistics web application that manages shipments, customers, tracking events, drivers, delivery routes, exceptions/delays, and notifications.

## Overview

FreightTrack is a comprehensive logistics management system built with:
- **Backend**: Java Spring Boot REST API
- **Frontend**: Angular Single Page Application
- **Database**: PostgreSQL
- **Infrastructure**: Docker & Docker Compose
- **CI/CD**: GitHub Actions

## Features

### Core Functionality
- **Shipments Management**: Track and manage all shipments
- **Customer Management**: Store and manage customer information
- **Tracking Events**: Real-time tracking events for shipments
- **Driver Management**: Manage driver profiles and assignments
- **Delivery Routes**: Optimize and manage delivery routes
- **Exceptions/Delays**: Monitor and handle shipping exceptions and delays
- **Notifications**: Real-time notifications for shipments and events

## Project Structure

```
FreightTrack/
├── backend/                 # Spring Boot API
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                # Angular Application
│   ├── src/
│   ├── angular.json
│   ├── package.json
│   └── Dockerfile
├── docker-compose.yml       # Local development environment
├── init-db.sql             # Database initialization
└── .github/
    └── workflows/          # CI/CD pipelines
```

## Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL 14+ (or use Docker)
- Docker & Docker Compose
- Git

## Getting Started

### Option 1: Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/gh-demos/FreightTrack.git
cd FreightTrack

# Start all services
docker-compose up -d

# Backend API: http://localhost:8080
# Frontend: http://localhost:4200
# PostgreSQL: localhost:5432
```

### Option 2: Local Development

#### Backend Setup
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`

#### Frontend Setup
```bash
cd frontend
npm install
ng serve
```

Frontend runs on `http://localhost:4200`

#### Database
```bash
# PostgreSQL must be running on localhost:5432
# Create database and run init script
psql -U postgres -f init-db.sql
```

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### Endpoints

#### Shipments
- `GET /api/shipments` - List all shipments
- `GET /api/shipments/{id}` - Get shipment details
- `POST /api/shipments` - Create new shipment
- `PUT /api/shipments/{id}` - Update shipment
- `DELETE /api/shipments/{id}` - Delete shipment

#### Customers
- `GET /api/customers` - List all customers
- `GET /api/customers/{id}` - Get customer details
- `POST /api/customers` - Create new customer
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Delete customer

#### Drivers
- `GET /api/drivers` - List all drivers
- `GET /api/drivers/{id}` - Get driver details
- `POST /api/drivers` - Create new driver
- `PUT /api/drivers/{id}` - Update driver
- `DELETE /api/drivers/{id}` - Delete driver

#### Tracking Events
- `GET /api/tracking-events` - List tracking events
- `GET /api/tracking-events?shipmentId={id}` - Get events for shipment
- `POST /api/tracking-events` - Create tracking event

#### Routes
- `GET /api/routes` - List all routes
- `GET /api/routes/{id}` - Get route details
- `POST /api/routes` - Create new route
- `PUT /api/routes/{id}` - Update route

#### Exceptions
- `GET /api/exceptions` - List all exceptions
- `GET /api/exceptions/{id}` - Get exception details
- `POST /api/exceptions` - Create new exception
- `PUT /api/exceptions/{id}` - Update exception status

#### Notifications
- `GET /api/notifications` - List notifications
- `POST /api/notifications` - Create notification

## Database Schema

### Key Tables
- `customers` - Customer information
- `drivers` - Driver profiles
- `shipments` - Shipment records
- `tracking_events` - Event history
- `delivery_routes` - Route information
- `exceptions` - Exception/delay records
- `notifications` - Notification log

## Technology Stack

### Backend
- Spring Boot 3.x
- Spring Data JPA
- Spring Web
- PostgreSQL JDBC Driver
- Lombok
- MapStruct

### Frontend
- Angular 17+
- Angular Material
- RxJS
- TypeScript
- Bootstrap 5

### Infrastructure
- Docker
- Docker Compose
- PostgreSQL 14

### DevOps
- GitHub Actions
- Maven
- npm

## CI/CD Pipeline

The project includes automated GitHub Actions workflows:

- **Build & Test**: Runs on every push
  - Backend: Maven build and unit tests
  - Frontend: npm build and lint

- **Docker Build**: Builds and pushes Docker images
  - Backend API image
  - Frontend image

- **Integration Tests**: Runs integration tests
- **Deploy**: Auto-deploy to staging environment

## Configuration

### Backend Configuration
Environment variables (see `backend/src/main/resources/application.yml`):
- `DB_URL` - Database connection URL
- `DB_USERNAME` - Database user
- `DB_PASSWORD` - Database password
- `SERVER_PORT` - API server port (default: 8080)

### Frontend Configuration
Environment files (see `frontend/src/environments/`):
- `environment.ts` - Development
- `environment.prod.ts` - Production

## Development Workflow

1. Create a feature branch: `git checkout -b feature/my-feature`
2. Make changes and commit: `git commit -am "Add feature"`
3. Push to GitHub: `git push origin feature/my-feature`
4. Create a Pull Request
5. CI/CD pipeline runs automatically
6. After review, merge to main

## Deployment

### Docker Deployment
```bash
# Build images
docker-compose build

# Start services
docker-compose up -d
```

### Production Deployment
See GitHub Actions workflows for automated deployment setup.

## Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## Troubleshooting

### Database Connection Issues
- Ensure PostgreSQL is running on port 5432
- Check database credentials in `application.yml`
- Run `init-db.sql` to initialize schema

### Port Already in Use
- Backend (8080): `lsof -i :8080` and kill process
- Frontend (4200): `ng serve --port 4300`
- PostgreSQL (5432): Already in use by another instance

### CORS Issues
- Check `application.yml` for CORS configuration
- Ensure frontend URL is whitelisted

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to your fork
5. Submit a Pull Request

## License

MIT License

## Support

For issues, questions, or suggestions, please create a GitHub issue.

## Team

Built for logistics professionals by the gh-demos team.