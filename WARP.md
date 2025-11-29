# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

**Gamers Station Marketplace API** - A Spring Boot 3.5.5 REST API for a gaming marketplace platform.

- **Technology Stack**: Java 21, Spring Boot, Spring Data JPA, MySQL, Lombok, Maven
- **Base Package**: `com.thegamersstation.marketplace`
- **Database**: MySQL (configured in application-dev.yaml and application-prod.yaml)

## Build & Run Commands

### Building the Application
```powershell
# Clean and build
.\mvnw.cmd clean install

# Build without tests
.\mvnw.cmd clean install -DskipTests

# Compile only
.\mvnw.cmd compile
```

### Running the Application
```powershell
# Run with default profile (dev)
.\mvnw.cmd spring-boot:run

# Run with specific profile
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod

# Run packaged JAR
java -jar target\gs-marketplace-api-0.0.1-SNAPSHOT.jar
```

### Testing
```powershell
# Run all tests
.\mvnw.cmd test

# Run specific test class
.\mvnw.cmd test -Dtest=GamersStationMarketplaceApiApplicationTests

# Run tests with coverage
.\mvnw.cmd test jacoco:report
```

### Maven Profiles
- **dev**: Development environment (active by default)
- **prod**: Production environment

## Architecture & Code Organization

### Package Structure
The codebase follows a layered architecture pattern:

```
com.thegamersstation.marketplace/
├── config/          - Spring configuration classes (DatabaseConfig)
├── controller/      - REST controllers (AuthenticationController)
├── service/         - Business logic layer (AuthenticationService)
├── repository/      - Data access layer with JPA repositories
│   └── user/       - User domain entities and repositories
├── model/          - DTOs and request/response objects
│   ├── input/      - Request DTOs (RegisterRequest)
│   └── response/   - Response DTOs
└── util/           - Utility classes
```

### Key Architectural Patterns

#### 1. Database Configuration
- Custom `DatabaseConfig` class handles JPA and transaction management
- Entity scanning: `com.thegamersstation.marketplace.repository`
- JPA repositories enabled with custom entity manager and transaction manager
- Hibernate is configured to show SQL and use `update` DDL strategy in dev mode

#### 2. Entity Modeling
- JPA entities are located in `repository/` package alongside their repositories (domain-driven structure)
- Entities use Lombok for getters/setters (`@Getter`, `@Setter`)
- Example: `User` entity in `repository/user/` package

#### 3. Controller Layer
- Controllers are annotated with `@RestController`
- Use `@Autowired` for dependency injection
- Return `ResponseEntity<T>` for consistent HTTP responses

#### 4. Repository Pattern
- Repositories extend `JpaRepository<Entity, ID>`
- Annotated with `@Repository`
- Follow naming convention: `{Entity}Repository`

### Configuration Files

#### Application Profiles
- `application.yaml` - Base configuration with profile fallback to dev
- `application-dev.yaml` - Development settings (MySQL localhost, DEBUG logging)
- `application-prod.yaml` - Production settings

**Important**: Database credentials are in `application-dev.yaml`. When working with database connections, always reference profile-specific configurations.

### Dependencies

Key Spring Boot starters:
- `spring-boot-starter-web` - REST API capabilities
- `spring-boot-starter-data-jpa` - Database ORM
- `spring-boot-starter-actuator` - Health checks and monitoring
- `spring-boot-starter-test` - Testing framework
- `lombok` - Boilerplate reduction (requires annotation processing)
- `mysql-connector-j` - MySQL JDBC driver

## Development Notes

### Working with Entities
- Entities are JPA entities with `@Entity` and `@Table` annotations
- Place new entities in `repository/{domain}/` package alongside their repositories
- Use Lombok annotations (`@Getter`, `@Setter`) to reduce boilerplate
- Override `toString()` for debugging purposes

### Working with Repositories
- Extend `JpaRepository<EntityType, IdType>` for standard CRUD operations
- Place repositories in the same package as their entities (`repository/{domain}/`)
- Custom queries can use `@Query` annotation or method name conventions

### Working with Controllers
- Place controllers in `controller/` package
- Use `@RestController` for REST endpoints
- Inject repositories or services with `@Autowired`
- Return `ResponseEntity<T>` for proper HTTP status codes

### Database Schema Management
- Hibernate DDL mode is set to `update` in dev environment
- DDL mode is `validate` in base config (for production safety)
- Manual schema migrations may be needed for production

### Lombok Configuration
- Lombok annotation processing is configured in `pom.xml`
- When adding Lombok dependencies, ensure annotation processor path is updated in the Maven compiler plugin

## Windows-Specific Notes

This project is developed on Windows. Use PowerShell commands:
- Maven wrapper: `.\mvnw.cmd` (not `./mvnw`)
- Path separators: backslash `\` in Windows paths
- Environment variables: `$env:VARIABLE_NAME` in PowerShell
