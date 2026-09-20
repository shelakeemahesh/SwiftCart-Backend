---
trigger: glob
globs: ["**/*.java", "**/pom.xml", "**/build.gradle*", "**/application*.yml", "**/application*.properties"]
---

# Backend standards (Spring Boot 3)

- Package-by-feature. Layers: controller → service → repository, plus dto, mapper, config. Never expose entities in API responses.
- Central error handling with @ControllerAdvice and one error JSON shape. Bean validation on request DTOs.
- Flyway migration for every schema change; never edit an applied migration. dev/test/prod profiles; config via env vars.
- springdoc OpenAPI on all endpoints; Actuator health/readiness.
- Structured logs with correlation IDs. Never log secrets, tokens, or full payment payloads.
- Security: @EnableMethodSecurity; authorization and ownership checks in the backend; webhooks verified against the raw body with a constant-time compare (MessageDigest.isEqual); idempotent payment/webhook handling.
- Tests: JUnit 5 + MockMvc for web/security; Testcontainers for MySQL/Redis/Kafka/Elasticsearch; JaCoCo coverage; Spotless/Checkstyle must pass.
- Multi-stage, non-root Dockerfile; docker-compose for local MySQL/Redis/Kafka/Elasticsearch.
