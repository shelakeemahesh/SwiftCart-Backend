# SwiftCart Backend — Operational Runbook

This guide describes operational procedures, health diagnosis, triage workflows, and incident response for the SwiftCart Backend service.

---

## 1. Service Health & Readiness

### Health Check Endpoint
```bash
curl -f http://localhost:8080/actuator/health
```
Expected response:
```json
{"status":"UP"}
```

### Key Subsystem Health Checks
- **Database (MySQL/TiDB)**: Spring Actuator validates connection pooling through HikariCP.
- **Redis Cache**: Evaluated via Redis connection ping.
- **Elasticsearch**: Checked when `elasticsearch.enabled=true`.
- **Kafka**: Broker connection validated via cluster metadata.

---

## 2. Common Incidents & Resolution

### Incident A: High Error Rate on `/api/v1/products/search`
- **Symptom**: 500 or timeout errors when customers query products.
- **Root Cause**: Elasticsearch node unreachable or cluster in Red state.
- **Immediate Mitigation**:
  Disable Elasticsearch via environment configuration to trigger graceful JPA fallback:
  ```bash
  export ELASTICSEARCH_ENABLED=false
  # Restart service container
  ```
  The application automatically switches to `ProductRepository` database search.

### Incident B: Razorpay Webhook Processing Failures
- **Symptom**: Customer payments confirmed in Razorpay, but orders remain `PENDING`.
- **Investigation**:
  1. Check application logs for `PaymentVerificationException`:
     ```bash
     docker compose logs backend | grep -i "signature"
     ```
  2. Verify that `RAZORPAY_WEBHOOK_SECRET` matches the webhook secret configured in the Razorpay merchant dashboard.
  3. Verify Redis is up (used for webhook idempotency locks).

### Incident C: HikariCP Connection Pool Exhaustion
- **Symptom**: `ConnectionTimeoutException: Connection is not available, request timed out after 30000ms`.
- **Mitigation**:
  1. Inspect long-running locks in MySQL:
     ```sql
     SHOW PROCESSLIST;
     SELECT * FROM performance_schema.data_locks;
     ```
  2. Increase connection pool size in `application-prod.yml`:
     ```yaml
     spring.datasource.hikari.maximum-pool-size: 25
     ```

---

## 3. Cache & Session Maintenance

### Flush Redis Application Cache
```bash
docker exec -it swiftcart-redis redis-cli FLUSHDB
```

---

## 4. Rollback Procedure
If a production deployment introduces regressions:
1. Revert container image to previous tag:
   ```bash
   docker compose pull backend:v<previous-stable-tag>
   docker compose up -d backend
   ```
2. Verify `/actuator/health` is `UP`.
3. Check error rates in application logs.
