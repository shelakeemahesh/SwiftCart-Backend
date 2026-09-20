# SwiftCart Backend — REST API Reference

All endpoints are versioned under `/api/v1/` unless specified otherwise.

---

## 1. Authentication & Identity (`/api/v1/auth`)

| Method | Endpoint | Access | Description |
| ------ | -------- | ------ | ----------- |
| `POST` | `/api/v1/auth/register` | Public | Register customer account |
| `POST` | `/api/v1/auth/seller/register` | Public | Register seller account with GSTIN/PAN |
| `POST` | `/api/v1/auth/login` | Public | Authenticate with email/password; returns JWT |
| `POST` | `/api/v1/auth/send-otp` | Public | Generate and send SMS OTP |
| `POST` | `/api/v1/auth/verify-otp` | Public | Verify SMS OTP |
| `POST` | `/api/v1/auth/reset-password` | Public | Reset password using verified OTP |
| `GET`  | `/oauth2/authorization/{provider}` | Public | Initiate Google or GitHub OAuth2 flow |
| `GET`  | `/login/oauth2/code/{provider}` | Public | OAuth2 redirect authorization callback |

---

## 2. Product Catalog & Search (`/api/v1/products`)

| Method | Endpoint | Access | Description |
| ------ | -------- | ------ | ----------- |
| `GET`  | `/api/v1/products` | Public | Browse paginated products with category/price filters |
| `GET`  | `/api/v1/products/{id}` | Public | Get product details by ID |
| `GET`  | `/api/v1/products/slug/{slug}` | Public | Get product details by SEO slug |
| `GET`  | `/api/v1/products/featured` | Public | Get featured carousel products |
| `GET`  | `/api/v1/products/search` | Public | High-performance search with Elasticsearch/MySQL fallback |
| `GET`  | `/api/v1/products/search/suggestions`| Public | Autocomplete suggestions |
| `POST` | `/api/v1/products` | Seller / Admin | Create new catalog product |
| `PUT`  | `/api/v1/products/{id}` | Seller / Admin | Update product information |
| `DELETE`| `/api/v1/products/{id}`| Seller / Admin | Delete catalog product |

---

## 3. Shopping Cart (`/api/v1/cart`)

| Method | Endpoint | Access | Description |
| ------ | -------- | ------ | ----------- |
| `GET`  | `/api/v1/cart` | Authenticated | Retrieve authenticated user's cart |
| `POST` | `/api/v1/cart/items` | Authenticated | Add item or update quantity in cart |
| `DELETE`| `/api/v1/cart/items/{itemId}` | Authenticated | Remove item from cart |
| `DELETE`| `/api/v1/cart/clear` | Authenticated | Clear all items from cart |

---

## 4. Checkout & Orders (`/api/v1/orders`)

| Method | Endpoint | Access | Description |
| ------ | -------- | ------ | ----------- |
| `POST` | `/api/v1/orders` | Customer | Place order with cart items and address |
| `GET`  | `/api/v1/orders/{orderId}` | Owner / Admin | Get order details |
| `GET`  | `/api/v1/orders/user/{userId}` | Owner / Admin | List order history for user |
| `GET`  | `/api/v1/orders/{orderId}/track`| Owner / Admin | Real-time live status tracking |
| `POST` | `/api/v1/orders/{orderId}/cancel`| Owner / Admin | Cancel order and trigger automated refund |

---

## 5. Payments & Webhooks (`/api/v1/payments`)

| Method | Endpoint | Access | Description |
| ------ | -------- | ------ | ----------- |
| `POST` | `/api/v1/payments/create-order/{orderId}` | Customer | Generate Razorpay order token |
| `POST` | `/api/v1/payments/verify` | Customer | Verify HmacSHA256 Razorpay signature |
| `POST` | `/api/v1/payments/webhook` | Public (Signed) | Process idempotent asynchronous webhook |
| `POST` | `/api/v1/payments/refund/{orderId}` | Admin / System | Execute order refund through Razorpay |

---

## 6. AI Chatbot & Assistant (`/api/v1/chatbot`)

| Method | Endpoint | Access | Description |
| ------ | -------- | ------ | ----------- |
| `POST` | `/api/v1/chatbot/message` | Public / Auth | Conversational assistance, FAQ, and RAG |
| `POST` | `/api/v1/chatbot/orders/{orderId}/refund` | Owner / Admin | Automated chatbot order refund request |

---

## 7. Operational & Health

| Method | Endpoint | Access | Description |
| ------ | -------- | ------ | ----------- |
| `GET`  | `/actuator/health` | Public | Spring Boot Actuator readiness and liveness |
| `GET`  | `/actuator/info` | Public | Application version and metadata |
| `GET`  | `/v3/api-docs` | Public | OpenAPI 3.0 JSON specification |
| `GET`  | `/swagger-ui.html` | Public | Interactive Swagger UI API documentation |
