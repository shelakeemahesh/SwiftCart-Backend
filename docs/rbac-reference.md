# SwiftCart Role-Based Access Control (RBAC) Reference

This document serves as the reference guide for SwiftCart's **Two-Layer Defense Strategy** for securing HTTP endpoints and resources based on user roles (`CUSTOMER`, `SELLER`, `ADMIN`).

---

## Access Control Matrix

| Component / Action | CUSTOMER | SELLER | ADMIN | Enforcement Mechanism |
| :--- | :---: | :---: | :---: | :--- |
| **Catalog Browsing (GET)** | ✅ | ✅ | ✅ | Public Route (Whitelisted in `SecurityConfig`) |
| **Reviews Submission (POST/PUT)** | ✅ | ❌ | ✅ | `SecurityConfig` requestMatchers + Method Verification |
| **Cart Management** | ✅ | ❌ | ✅ | `SecurityConfig` requestMatchers + Principal User ID lookup |
| **Wishlist Management** | ✅ | ❌ | ✅ | `SecurityConfig` requestMatchers + Principal User ID lookup |
| **Own Order Management** | ✅ | ❌ | ✅ | `@PreAuthorize("@swiftSecurity.canCustomerManageOrder(#orderUuid)")` |
| **Product Inventory (CRUD)** | ❌ | ✅ | ✅ | `@PreAuthorize("@swiftSecurity.canManageProduct(#productId)")` |
| **Storefront Dispatching (Shipping)** | ❌ | ✅ | ✅ | `@PreAuthorize("@swiftSecurity.canSellerManageOrder(#orderUuid)")` |
| **Seller Dashboard Analytics** | ❌ | ✅ | ✅ | `SecurityConfig` requestMatchers + Principal User ID lookup |
| **Admin Console & Users List** | ❌ | ❌ | ✅ | `@PreAuthorize("hasRole('ADMIN')")` at Controller class level |

---

## Two-Layer Defense Strategy

### Layer 1: Coarse-Grained Gateway Filtering (`SecurityConfig.java`)
Spring Security's filter chain intercepts incoming HTTP requests and enforces path-based access controls before any method controller processing:

```java
// Role-restricted routes
.requestMatchers("/api/v1/seller/**").hasAnyRole("SELLER", "ADMIN")
.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

// General authenticated requests (Restricted to Customer and Admin)
.requestMatchers("/api/v1/users/**").hasAnyRole("CUSTOMER", "ADMIN")
.requestMatchers("/api/v1/cart/**").hasAnyRole("CUSTOMER", "ADMIN")
.requestMatchers("/api/v1/orders/**").hasAnyRole("CUSTOMER", "ADMIN")
.requestMatchers("/api/v1/wishlist/**").hasAnyRole("CUSTOMER", "ADMIN")
.requestMatchers("/api/v1/coupons/**").hasAnyRole("CUSTOMER", "ADMIN")
```

### Layer 2: Fine-Grained Ownership Checks (`@PreAuthorize`)
At the controller layer, dynamic SQL checks are run against the path parameters to ensure users do not access or modify resources belonging to other stakeholders:

1. `@PreAuthorize("@swiftSecurity.canManageProduct(#productId)")`
   - Checks if the authenticated seller (or admin) is the author/owner of the product.
2. `@PreAuthorize("@swiftSecurity.canCustomerManageOrder(#orderUuid)")`
   - Restricts order detail queries, cancellations, or PDF invoice downloads to the user who placed the order or the admin.
3. `@PreAuthorize("@swiftSecurity.canSellerManageOrder(#orderUuid)")`
   - Authorizes order fulfillment (shipping) to sellers who own at least one item present in the order, or administrators.
4. `@PreAuthorize("@swiftSecurity.isAdminOrSellerOwner(#sellerId)")`
   - Restricts viewing seller profiles to the seller themselves, or an admin.
5. `@PreAuthorize("@swiftSecurity.isAdminOrCustomerOwner(#customerId)")`
   - Restricts customer-specific profile lookups.

---

## JWT Claims Structure

To prevent hitting the database on every single API request, authentication details are embedded inside the JWT Access Token claims. During request filtering, `JwtAuthenticationFilter` reads these claims and instantly populates the `SecurityContext` in-memory.

**JWT Payload Claims**:
```json
{
  "sub": "9503072201",         // Username (Phone number)
  "userId": 2,                 // Database Primary Key ID
  "role": "ADMIN",             // System Role Authority
  "iat": 1780918191,
  "exp": 1780919091
}
```
This payload is parsed directly to reconstruct a `CustomUserPrincipal` with in-memory authorities.
