# SwiftCart Backend ⚡

SwiftCart is a full-featured, enterprise-grade e-commerce platform designed to rival major online retail giants. This repository contains the robust, production-ready Spring Boot backend engine powering the platform.

---

## 🛠️ Tech Stack & Architecture

- **Core Framework**: Java 21 & Spring Boot 3.3.10
- **Database**: MySQL 8 (primary store) & H2 (for in-memory integration testing)
- **Caching & Idempotency**: Redis (Spring Data Redis)
- **Message Broker**: Apache Kafka (event-driven updates and live user activity feed)
- **Search Engine**: Elasticsearch (for lightning-fast full-text searches and autocomplete suggestions)
- **Payment Gateway**: Razorpay Integration (with strict HMAC signature verification and idempotency locks)
- **Notifications**: Twilio (SMS OTP) & Brevo (SMTP/Email notifications)
- **Security**: Spring Security (JWT-based session authentication with coarse-grained and fine-grained RBAC defense layers)

---

## 🔒 Security & Concurrency Defense

### 1. Two-Layer RBAC Defense
- **Coarse-Grained Gateway Filtering (`SecurityConfig.java`)**: Pre-filters routes based on role authorities (`CUSTOMER`, `SELLER`, `ADMIN`).
- **Fine-Grained Ownership Checks (`@PreAuthorize`)**: Custom Spring expressions mapping path parameters to authenticated users to prevent data modification/access by unauthorized stakeholders.

### 2. Double-Checkout & Order Concurrency Locks
- **Pessimistic Write Database Locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`)**: Enforced on product inventory, variant stock, user cart retrieval, and order status state-transitions. This prevents concurrency issues like double order placements on concurrent checkout requests and double stock restoration on concurrent cancellations.

### 3. Payment Webhook Idempotency
- Uses Redis keys (`setIfAbsent`) derived from the Razorpay webhook signature header to prevent duplicate webhook delivery processing.

---

## ⚙️ Environment Variables (`.env`)

Create a `.env` file in the root directory:

```properties
PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Database Configuration
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/swiftcart?useSSL=false
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_mysql_password
JPA_DDL_AUTO=update

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Kafka Configuration
SPRING_KAFKA_ENABLED=false
KAFKA_BROKERS=localhost:9092

# Elasticsearch Configuration
ELASTICSEARCH_ENABLED=false
ES_HOST=localhost
ES_PORT=9200

# Security (JWT)
JWT_SECRET=your_super_secret_base64_key_at_least_512_bits_long

# Razorpay Configuration
RAZORPAY_KEY_ID=your_razorpay_key_id
RAZORPAY_KEY_SECRET=your_razorpay_key_secret
RAZORPAY_WEBHOOK_SECRET=your_webhook_secret

# Twilio SMS
TWILIO_ACCOUNT_SID=your_twilio_sid
TWILIO_AUTH_TOKEN=your_twilio_token
TWILIO_PHONE_NUMBER=your_twilio_phone

# Mail (Brevo)
MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=587
MAIL_USERNAME=your_username
MAIL_PASSWORD=your_smtp_key
```

---

## 🚀 Getting Started

### Prerequisites
- Docker & Docker Compose
- Maven 3.9+
- JDK 21

### 1. Spin up Services (Local Development)
Use Docker Compose to launch MySQL, Redis, Elasticsearch, ZooKeeper, and Kafka:
```bash
docker-compose up -d
```

### 2. Run the Spring Boot Application
```bash
mvn spring-boot:run
```

### 3. Run Integration & Unit Tests
```bash
mvn clean test
```
*Note: `ConcurrentOrderLockTest` is configured to skip on the in-memory H2 database profile to respect locking mechanisms, but can run on a full MySQL database instance.*
