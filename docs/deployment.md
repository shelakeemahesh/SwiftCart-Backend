# SwiftCart Backend — Deployment Guide

This guide details deployment topology, environment profiles, container orchestration, and production readiness guidelines.

---

## 1. Deployment Topology

SwiftCart Backend is packaged as a containerized Spring Boot service. In production, it can be deployed to:
- **Cloud Containers**: Docker containers orchestrated via AWS ECS, Google Cloud Run, Kubernetes, or Render.
- **Database**: Managed MySQL (AWS RDS, Aurora) or TiDB Cloud (Distributed SQL).
- **Cache**: Managed Redis (AWS ElastiCache, Upstash, or Redis Cloud).
- **Search**: Elastic Cloud or OpenSearch.
- **Message Broker**: Managed Kafka (Confluent Cloud, AWS MSK, or Upstash Kafka).

---

## 2. Spring Profiles

The application configures three distinct profiles:
- `dev`: Local development environment (default). Detailed SQL logging, development defaults.
- `test`: Automated CI/CD test execution with in-memory H2 database and disabled external brokers.
- `prod`: Hardened production environment. Minified logs, connection pooling optimizations, strict SSL, and enforced environment variables.

To activate the production profile:
```bash
export SPRING_PROFILES_ACTIVE=prod
```

---

## 3. Container Deployment (Docker)

### 3.1 Build Multi-Stage Container
```bash
docker build -t swiftcart-backend:latest .
```

### 3.2 Run Container with External Configuration
```bash
docker run -d \
  -p 8080:8080 \
  --env-file .env.production \
  --name swiftcart-backend \
  swiftcart-backend:latest
```

---

## 4. Production Readiness Checklist

- [ ] All passwords, API secrets, and JWT keys are provided via secret managers, never hardcoded.
- [ ] `SPRING_PROFILES_ACTIVE=prod` is set.
- [ ] `JPA_DDL_AUTO=validate` or `none` in production to prevent schema modification.
- [ ] Database connection pooling configured (`spring.datasource.hikari.maximum-pool-size: 20-30`).
- [ ] TLS / HTTPS termination configured at reverse proxy or load balancer.
- [ ] Actuator endpoints restricted: only `/actuator/health` and `/actuator/info` publicly exposed.
- [ ] Rate limiting enabled at ingress or API gateway.
