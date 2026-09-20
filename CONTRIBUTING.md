# Contributing to SwiftCart Backend

Thank you for your interest in contributing to SwiftCart! This document outlines our development workflow, coding standards, branch conventions, and pull request procedures.

---

## 🌿 Branching Strategy

We follow a strict trunk-based feature branching model:
- `main` — Production branch. All code on `main` must pass automated CI checks. Direct commits to `main` are prohibited.
- `feat/<feature-name>` — New feature implementations.
- `fix/<issue-name>` — Bug fixes and security patches.
- `chore/<task-name>` — Infrastructure, dependencies, hygiene, and tooling.
- `docs/<doc-name>` — Documentation and architectural records.
- `ci/<workflow-name>` — CI/CD and automation updates.

---

## 🛠️ Local Development & Setup

### Prerequisites
- **Java 21** (Eclipse Temurin recommended)
- **Maven 3.9+**
- **Docker & Docker Compose** (for MySQL, Redis, Kafka, Elasticsearch)

### Quick Start
1. Clone the repository:
   ```bash
   git clone https://github.com/shelakeemahesh/SwiftCart-Backend.git
   cd SwiftCart-Backend
   ```
2. Configure local environment:
   ```bash
   cp .env.example .env
   ```
3. Start backing infrastructure:
   ```bash
   docker compose up -d
   ```
4. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```

---

## 🧪 Testing Before Commit

Every change must be verified locally before opening a pull request:
```bash
# Run unit and integration tests
mvn clean test

# Verify packaging and artifact builds
mvn package -DskipTests
```
Never commit red code, skip tests, or lower assertion thresholds.

---

## 📝 Commit Conventions

We enforce [Conventional Commits](https://www.conventionalcommits.org/):
- `feat(scope): add order cancellation refund logic`
- `fix(auth): correct OAuth2 cookie persistence and token issuance`
- `chore(deps): bump spring-boot-starter to latest patch`
- `docs(api): document search and recommendation endpoints`

---

## 🚀 Pull Request Process

1. Link an existing GitHub Issue in the PR description using `Closes #<issue_number>`.
2. Follow `.github/PULL_REQUEST_TEMPLATE.md`.
3. Ensure CI passes cleanly (100% green tests, CodeQL clean).
4. Address review comments with clean follow-up commits on the same branch.
