# Changelog

All notable changes to the SwiftCart Backend project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- Enterprise repository hygiene configuration (`.gitattributes`, `.editorconfig`, comprehensive `.gitignore`).
- CI/CD workflows: Maven build & test matrix (`ci.yml`), CodeQL security scanner (`codeql.yml`), Dependency Review (`dependency-review.yml`), tag-based release packaging (`release.yml`).
- Dependabot automated configuration for Maven, GitHub Actions, and Docker dependencies.
- GitHub collaboration templates (`PULL_REQUEST_TEMPLATE.md`, `bug_report.yml`, `feature_request.yml`).
- Community and governance standards (`LICENSE`, `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`).
- Multi-layer RBAC test suite and security expression utilities.
- Database fallback search service when Elasticsearch cluster is unavailable.
- Active order tracking fallback using JPA repository and automated refund requests for chatbot cancellation.

---

## [0.1.0] - 2026-09-21

### Added
- Initial enterprise repository foundation and baseline architecture.
- Modular architecture with Spring Boot 3, Spring Security 6, and MySQL/Redis/Kafka/Elasticsearch.
- Core checkout, Razorpay payment processing, and sentiment analytics pipeline.
