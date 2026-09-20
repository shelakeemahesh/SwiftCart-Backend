# SwiftCart Backend — Architecture Specification

This document details the software architecture, modular boundaries, security enforcement layers, data pipelines, and resilience fallbacks for the SwiftCart Backend platform.

---

## 1. System Overview & Context

SwiftCart Backend is an enterprise-grade e-commerce application platform powered by **Spring Boot 3 (Java 21)** and **Spring Security 6**. It manages product catalogs, shopping carts, transactional order state machines, Razorpay payment integrations, AI-powered conversational assistance, and Kafka-driven real-time live activity tracking.

```mermaid
graph TD
    Client["Client Apps (Web / React 18, Mobile)"]
    Gateway["Reverse Proxy / Nginx / Cloudflare"]
    
    subgraph SpringBootApp ["SwiftCart Core Platform (Spring Boot 3)"]
        SecFilter["Security Filter Chain & JWT / OAuth2"]
        Controllers["REST Controllers (API v1)"]
        Services["Domain Services (Order, Payment, Product, Cart)"]
        Resilience["Resilience & Fallback Adapters"]
    end

    subgraph DataStore ["Persistence & Cache Tier"]
        MySQL[("MySQL 8 / TiDB Cluster")]
        Redis[("Redis 7 (Session, Cache, Idempotency)")]
        ES[("Elasticsearch 8 (Search Catalog)")]
    end

    subgraph Messaging ["Event Streaming & External Integrations"]
        Kafka["Apache Kafka (Order & Activity Topics)"]
        Razorpay["Razorpay Payment Gateway API"]
        TwilioBrevo["Notification Services (SMS / Mail)"]
        OpenAI["Spring AI (LLM / Embeddings)"]
    end

    Client --> Gateway
    Gateway --> SecFilter
    SecFilter --> Controllers
    Controllers --> Services
    Services --> MySQL
    Services --> Redis
    Services --> Resilience
    Resilience --> ES
    Resilience --> Kafka
    Services --> Razorpay
    Services --> TwilioBrevo
    Services --> OpenAI
```

---

## 2. Layered Architecture

SwiftCart strictly enforces a package-by-feature layered architecture with clear boundaries:

1. **Controller Layer (`com.swiftcart.controller`)**:
   - HTTP request parsing, status codes, OpenAPI documentation.
   - Bean validation (`@Valid`) on all request DTOs.
   - Delegates business execution immediately to domain services.

2. **Security & Authorization Layer (`com.swiftcart.security`)**:
   - **Coarse-Grained Gateway Filtering**: Enforced via `SecurityConfig` route matchers (`/api/v1/admin/**`, `/api/v1/seller/**`, `/api/v1/user/**`).
   - **Fine-Grained Method Expressions**: Enforced via `@PreAuthorize("@swiftCartSecurity.isOwnerOrAdmin(#userId, authentication)")` using custom evaluator `SwiftCartSecurityExpression`.
   - **OAuth2 Bridge**: Custom authorization request repository (`HttpCookieOAuth2AuthorizationRequestRepository`) with user info extraction factory.

3. **Domain Service Layer (`com.swiftcart.service`)**:
   - Business state transitions, transactional workflows (`@Transactional`), and concurrency defense.
   - Graceful resilience fallbacks when backing systems (Redis, Kafka, Elasticsearch) fail.

4. **Persistence Layer (`com.swiftcart.repository`)**:
   - Spring Data JPA with Hibernate 6.
   - Pessimistic write locks (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) on inventory stock updates and order status transitions to prevent double-checkout and race conditions.

---

## 3. Data Flow & Transactional Workflows

### 3.1 Checkout & Payment State Machine

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Customer
    participant Checkout as OrderController
    participant OrderSvc as OrderService
    participant PaySvc as PaymentService
    participant RZP as Razorpay Gateway
    participant DB as MySQL DB
    participant Bus as Kafka Producer

    Customer->>Checkout: POST /api/v1/orders (Create Order)
    OrderSvc->>DB: Lock Cart & Inventory (Pessimistic Write)
    OrderSvc->>DB: Save Order (Status: PENDING)
    OrderSvc->>PaySvc: createPaymentOrder(orderId)
    PaySvc->>RZP: Create Order (Paise conversion)
    RZP-->>PaySvc: razorpay_order_id
    PaySvc->>DB: Save RazorpayPayment record
    Checkout-->>Customer: OrderDTO with razorpayOrderId & Key

    Customer->>RZP: Complete Payment in Frontend SDK
    Customer->>Checkout: POST /api/v1/payments/verify
    PaySvc->>PaySvc: Verify HmacSHA256 Signature
    PaySvc->>DB: Update Payment (Status: SUCCESS)
    OrderSvc->>DB: Transition Order (Status: CONFIRMED)
    OrderSvc->>Bus: Publish OrderCreatedEvent
    Checkout-->>Customer: Payment Verification Success
```

### 3.2 Search & Discovery Resilience Pattern

```mermaid
flowchart TD
    Req["Search Request (keyword, category, price)"] --> CheckES{"Elasticsearch Enabled & Healthy?"}
    CheckES -- Yes --> QueryES["Query Elasticsearch Cluster"]
    QueryES -- Success --> Return["Return Formatted Search Results"]
    QueryES -- Exception --> FallbackDB["Degrade Gracefully: Fallback to MySQL JPA Query"]
    CheckES -- No --> FallbackDB
    FallbackDB --> Return
```

---

## 4. Resilience & Fault Tolerance Guarantees

1. **Database Fallback for Search**: When Elasticsearch is unavailable or disabled, search queries dynamically degrade to database wildcard/index queries without failing customer traffic.
2. **Redis Caching Degradation**: Key-value operations wrap in fallback guards (`RedisFallbackService`); if Redis drops, the application falls back directly to database queries.
3. **Kafka Disconnect Tolerance**: Kafka producers log events asynchronously with retry policies; failure to publish events does not abort transactional order commits.
4. **Idempotent Webhooks**: Redis locks guard webhook callbacks (`razorpay_signature`) preventing duplicate delivery charges or double fulfillment.
