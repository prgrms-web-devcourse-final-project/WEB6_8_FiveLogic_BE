# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure

This is a Java Spring Boot backend project (`WEB6_8_FiveLogic_BE`) using Gradle as the build system. The main application code is located in the `back/` directory.

### Key Architecture Components

- **Package Structure**: Uses domain-driven design with packages organized under `com.ll.back.domain.*`
- **Main Application**: `BackApplication.java` - Standard Spring Boot entry point
- **Domain Layer**: Member domain with repository, service, and entity layers
- **Global Layer**: Contains cross-cutting concerns like aspects, JPA base entities, request handling (`Rq`), and response data structures (`RsData`)
- **Security**: JWT-based authentication with configurable tokens
- **Database**: H2 database for development (configurable via profiles)

### Technology Stack

- Java 21
- Spring Boot 3.5.5
- Spring Security with JWT (jjwt library)
- Spring Data JPA with Hibernate
- H2 Database
- Lombok for boilerplate reduction
- SpringDoc OpenAPI (Swagger)
- Spring Boot Actuator

## Development Commands

All commands should be run from the `back/` directory:

### Build and Run
```bash
# Build the project
./gradlew build

# Run the application (development mode)
./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests with detailed output
./gradlew test --info
```

### Database and Development Tools
- H2 Console: http://localhost:8080/h2-console (when running)
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator endpoints: http://localhost:8080/actuator

## Configuration

The application uses profile-based configuration:
- `application.yml` - Base configuration
- `application-dev.yml` - Development profile
- `application-prod.yml` - Production profile  
- `application-test.yml` - Test profile

Environment variables can be loaded from `.env` file for sensitive configuration like JWT secret keys.

## Key Development Notes

- JWT secret key can be configured via `CUSTOM__JWT__SECRET_KEY` environment variable
- Frontend URL configured for CORS: http://localhost:3000
- Backend runs on port 8080
- Uses create-drop DDL strategy in development for clean database state
- Hibernate SQL logging is enabled for development debugging