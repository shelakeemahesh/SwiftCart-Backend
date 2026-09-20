# Security Policy

SwiftCart takes the security of our platform and user data seriously. We appreciate your efforts to responsibly disclose any vulnerabilities.

---

## 🛡️ Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 0.1.x   | :white_check_mark: |
| < 0.1.0 | :x:                |

---

## 🚨 Reporting a Vulnerability

**Please do NOT report security vulnerabilities via public GitHub issues.**

Instead, report vulnerabilities via one of the following channels:
1. **GitHub Security Advisories**: Submit a private advisory report at [SwiftCart-Backend Security Advisories](https://github.com/shelakeemahesh/SwiftCart-Backend/security/advisories/new).
2. **Direct Security Contact**: Send an encrypted email to `shelakeemahesh@users.noreply.github.com` with the subject prefix `[SECURITY VULNERABILITY - SwiftCart]`.

### Please Include:
- A clear description of the vulnerability and attack scenario.
- Steps to reproduce or proof-of-concept (PoC) exploit scripts.
- Impact assessment on customer data, authentication, or payment integrity.
- Proposed remediations or patches if available.

---

## ⏱️ Response SLA

- **Initial Acknowledgment**: Within 24 hours.
- **Triage & Assessment**: Within 48 hours.
- **Patch Release & Security Notice**: Dependent on severity, typically within 7 days for critical vulnerabilities.

---

## 🔐 Core Security Practices

- **Zero Secrets in Code**: Environment configuration is strictly externalized.
- **Role-Based Access Control**: Multi-layer authorization combining URL filters and fine-grained method expressions.
- **Payment Integrity**: Cryptographic webhook signature verification and pessimistic locks against double-checkout concurrency.
