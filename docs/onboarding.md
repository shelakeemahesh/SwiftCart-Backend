# SwiftCart Backend — Developer Onboarding Guide

Welcome to the SwiftCart engineering team! This guide walks you through setting up your local development environment from scratch.

---

## 1. Prerequisites

Ensure you have the following installed on your workstation:
- **Java 21 JDK**: [Eclipse Temurin 21](https://adoptium.net/) recommended.
- **Maven 3.9+**: Installed via Homebrew (`brew install maven`) or SDKMAN (`sdk install maven`).
- **Docker & Docker Compose**: [Docker Desktop](https://www.docker.com/products/docker-desktop/) or OrbStack.
- **Git**: Installed and configured with your name and email.
- **GitHub CLI (`gh`)**: Authenticated with `gh auth login`.

---

## 2. Setting Up the Codebase

### 2.1 Clone Repository
```bash
git clone https://github.com/shelakeemahesh/SwiftCart-Backend.git
cd SwiftCart-Backend
```

### 2.2 Configure Local Environment
```bash
cp .env.example .env
```
Default settings in `.env.example` work out-of-the-box with the local Docker Compose configuration.

### 2.3 Start Backing Services
```bash
docker compose up -d
```
This starts:
- MySQL on `localhost:3306`
- Redis on `localhost:6379`
- Kafka on `localhost:9092`
- Elasticsearch on `localhost:9200`

---

## 3. Building & Running the Backend

### Run Test Suite
```bash
mvn clean test
```
All unit and fallback tests should pass cleanly (0 failures).

### Run Application Locally
```bash
mvn spring-boot:run
```
The application will boot on `http://localhost:8080`.
- Health check: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 4. Engineering Workflow

1. Create an issue or locate your assigned task on the **SwiftCart — Engineering** GitHub Project.
2. Branch from `main`:
   ```bash
   git checkout -b feat/your-feature-name main
   ```
3. Test before commit:
   ```bash
   mvn test
   ```
4. Follow Conventional Commits:
   ```bash
   git commit -m "feat(cart): add cart item discount calculation"
   ```
5. Open PR linking the issue:
   ```bash
   gh pr create
   ```
