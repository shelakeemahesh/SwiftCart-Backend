# ADR 0001: Record Architecture Decisions

## Status
Accepted

## Context
SwiftCart Backend requires a clear mechanism to record architectural decisions, trade-offs, and design patterns so that maintainers, developers, and autonomous agents understand the rationale behind system boundaries.

## Decision
We adopt Architecture Decision Records (ADRs) as documented by Michael Nygard. Each ADR describes a architectural decision along with its context, options considered, and consequences.

ADRs are stored in `docs/adr/` in chronological numbering (`0001-...md`, `0002-...md`).

## Consequences
- Every significant architectural shift or policy change must be recorded via an ADR.
- Engineers and automated tools can review history and design decisions without ambiguity.

---

# ADR 0002: Dual-Layer RBAC and Security Evaluator

## Status
Accepted

## Context
E-commerce operations involve distinct roles (`ROLE_CUSTOMER`, `ROLE_SELLER`, `ROLE_ADMIN`). Standard route-based filtering alone is insufficient because customers and sellers must only access resources they own (e.g., viewing their own orders, updating their own products, managing their own addresses).

## Decision
We enforce a dual-layer security architecture:
1. **Coarse-Grained Gateway Filter**: `SecurityConfig` routes HTTP request paths based on user authority (`hasRole('ADMIN')`, `hasRole('SELLER')`, etc.).
2. **Fine-Grained Method Expression**: Custom Spring Expression evaluator `@swiftCartSecurity` (`SwiftCartSecurityExpression.java`) verifies resource ownership against the authenticated principal id or email (`@PreAuthorize("@swiftCartSecurity.isOwnerOrAdmin(#userId, authentication)")`).

## Consequences
- Prevents Horizontal Privilege Escalation (IDOR attacks).
- Clean separation between path filtering and domain ownership logic.
- Tested using unit test suites and `RbacMatrixIntegrationTest`.

---

# ADR 0003: Graceful Infrastructure Degradation Strategy

## Status
Accepted

## Context
Backing infrastructure components (Elasticsearch, Redis, Kafka) may experience transient network partitions or outages in production or local environments. E-commerce checkouts and catalog discovery must remain available.

## Decision
We implement proactive fallback strategies across all external dependencies:
1. **Catalog Search**: Fallback from Elasticsearch to MySQL JPA queries when Elasticsearch connection fails or is disabled via configuration.
2. **Session / Caching**: Wrap cache hits with safe fallback to persistent database queries.
3. **Event Streaming**: Isolate order commit transactions from Kafka producer broker availability.

## Consequences
- Core checkout, order placement, and catalog browsing remain functional even during complete outages of secondary infrastructure.
