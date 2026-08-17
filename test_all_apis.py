import urllib.request
import urllib.parse
import json
import time
import sys

BASE_URL = "http://localhost:8080"

passed_tests = []
failed_tests = []

def request(method, path, body=None, token=None, expected_status=200):
    url = f"{BASE_URL}{path}"
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"

    data_bytes = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url, data=data_bytes, headers=headers, method=method)

    try:
        with urllib.request.urlopen(req, timeout=20) as response:
            status = response.status
            content_type = response.headers.get("Content-Type", "")
            if "application/json" in content_type:
                resp_body = json.loads(response.read().decode("utf-8"))
            else:
                resp_body = response.read()
            return status, resp_body
    except urllib.error.HTTPError as e:
        status = e.code
        try:
            resp_body = json.loads(e.read().decode("utf-8"))
        except Exception:
            resp_body = str(e)
        return status, resp_body
    except Exception as e:
        return 0, str(e)

def test_api(name, method, path, body=None, token=None, expected_status=200, check_fn=None):
    print(f"\n[RUNNING] {name} ({method} {path})...")
    status, res = request(method, path, body, token, expected_status)
    if status == expected_status:
        if check_fn:
            try:
                assert check_fn(res), f"Custom assertion failed for {name}"
            except AssertionError as err:
                print(f"❌ [FAILED] {name}: {err}")
                failed_tests.append((name, str(err)))
                return None
        print(f"✅ [PASSED] {name} (Status: {status})")
        passed_tests.append(name)
        return res
    else:
        print(f"❌ [FAILED] {name} - Expected {expected_status}, Got {status}")
        print(f"   Response: {res}")
        failed_tests.append((name, f"Expected {expected_status}, Got {status}: {res}"))
        return None

def main():
    print("=" * 70)
    print("🚀 SWIFTCART COMPREHENSIVE END-TO-END API TEST SUITE")
    print("=" * 70)

    # 1. Health check
    test_api("Actuator Health Check", "GET", "/actuator/health", expected_status=200)

    # 2. Public Category & Product Endpoints
    cats = test_api("Get All Categories", "GET", "/api/v1/categories", expected_status=200,
                    check_fn=lambda r: len(r.get("data", [])) >= 8)
    
    first_cat_id = cats["data"][0]["id"] if cats and cats.get("data") else 1
    test_api("Get Category by ID", "GET", f"/api/v1/categories/{first_cat_id}", expected_status=200)

    # Products listing with filters
    prods = test_api("List Products (All)", "GET", "/api/v1/products?size=10", expected_status=200,
                     check_fn=lambda r: r.get("data", {}).get("totalElements", 0) >= 120)
    
    test_api("List Products with Category Filter", "GET", f"/api/v1/products?categoryId={first_cat_id}&size=5", expected_status=200)
    test_api("List Products with Price Filter", "GET", "/api/v1/products?minPrice=1000&maxPrice=50000", expected_status=200)
    test_api("List Trending Products", "GET", "/api/v1/products/trending", expected_status=200)
    test_api("List New Arrivals", "GET", "/api/v1/products/new-arrivals", expected_status=200)
    test_api("List Flash Deals", "GET", "/api/v1/products/deals", expected_status=200)

    first_product = prods["data"]["content"][0] if prods and prods.get("data") and prods["data"].get("content") else None
    first_product_id = first_product["id"] if first_product else 30268
    first_product_slug = first_product["slug"] if first_product else "iphone"

    test_api("Get Product By ID", "GET", f"/api/v1/products/{first_product_id}", expected_status=200)
    test_api("Get Product By Slug", "GET", f"/api/v1/products/{first_product_slug}", expected_status=200)
    test_api("Get Related Products", "GET", f"/api/v1/products/{first_product_id}/related", expected_status=200)
    test_api("Get Frequently Bought Together", "GET", f"/api/v1/products/{first_product_id}/frequently-bought", expected_status=200)
    test_api("Get Product Price History", "GET", f"/api/v1/products/{first_product_slug}/price-history", expected_status=200)

    # Search endpoints
    test_api("Search Products Query", "GET", "/api/v1/search?q=Apple", expected_status=200)
    test_api("Search Auto-Suggest", "GET", "/api/v1/search/suggest?q=App", expected_status=200)

    # Reviews & Sentiment Public Endpoints
    test_api("Get Product Reviews", "GET", f"/api/v1/reviews/products/{first_product_id}", expected_status=200)
    test_api("Get Product Sentiment Analytics", "GET", f"/api/v1/reviews/products/{first_product_id}/sentiment", expected_status=200)

    # AI Chatbot Public Endpoints
    test_api("Get Chatbot Return Policy", "GET", "/api/v1/chat/return-policy", expected_status=200)
    test_api("RAG AI Chatbot Message", "POST", "/api/v1/chat/message", 
             body={"message": "Can you recommend running shoes in sports?"}, 
             expected_status=200,
             check_fn=lambda r: len(r.get("data", {}).get("messageText", "")) > 10)

    # 3. Authentication & Logins
    # Admin Login
    admin_login = test_api("Login Admin", "POST", "/api/v1/auth/login",
                           body={"identifier": "admin@swiftcart.com", "password": "admin123"},
                           expected_status=200)
    admin_token = admin_login["data"]["accessToken"] if admin_login and admin_login.get("data") else None

    # Seller Login
    seller_login = test_api("Login Seller", "POST", "/api/v1/auth/login",
                            body={"identifier": "seller_seed@swiftcart.com", "password": "seed123"},
                            expected_status=200)
    seller_token = seller_login["data"]["accessToken"] if seller_login and seller_login.get("data") else None

    # Customer OTP Flow & Login with a dynamic phone
    dynamic_phone = f"9503{int(time.time()) % 1000000:06d}"
    test_api("Send OTP for Dynamic Phone", "POST", f"/api/v1/auth/send-otp?phone={dynamic_phone}", expected_status=200)
    otp_verify_res = test_api("Verify OTP for Sandbox Customer", "POST", "/api/v1/auth/verify-otp",
                              body={"phone": "9503072201", "otp": "123456"},
                              expected_status=200)
    
    customer_token = otp_verify_res["data"]["accessToken"] if otp_verify_res and otp_verify_res.get("data") else admin_token

    # 4. User Profile & Address Operations
    test_api("Get Current User Profile (/api/v1/auth/me)", "GET", "/api/v1/auth/me", token=customer_token, expected_status=200)
    test_api("Get User Profile (/api/v1/users/me)", "GET", "/api/v1/users/me", token=customer_token, expected_status=200)
    test_api("Update User Profile", "PUT", "/api/v1/users/me", body={"name": "SwiftCart Customer Verified"}, token=customer_token, expected_status=200)

    addr_res = test_api("Add User Address (/api/v1/users/me/addresses)", "POST", "/api/v1/users/me/addresses",
                        body={
                            "label": "HOME",
                            "recipientName": "SwiftCart Customer",
                            "phone": "9503072201",
                            "pincode": "560001",
                            "flatHouse": "Flat 402, Sunshine Apts",
                            "area": "MG Road",
                            "city": "Bengaluru",
                            "state": "Karnataka",
                            "isDefault": True
                        },
                        token=customer_token, expected_status=200)
    
    address_id = addr_res["data"]["id"] if addr_res and addr_res.get("data") else None

    test_api("List User Addresses (/api/v1/users/me/addresses)", "GET", "/api/v1/users/me/addresses", token=customer_token, expected_status=200)
    test_api("List User Addresses (/api/v1/users/addresses)", "GET", "/api/v1/users/addresses", token=customer_token, expected_status=200)
    if address_id:
        test_api("Set Address Default", "PUT", f"/api/v1/users/me/addresses/{address_id}/default", token=customer_token, expected_status=200)

    # 5. Price Drop Alert
    test_api("Create Price Drop Alert", "POST", f"/api/v1/products/{first_product_slug}/alerts",
             body={"targetPrice": 500.0, "email": "customer@swiftcart.com"},
             token=customer_token, expected_status=200)

    # 6. Wishlist Operations
    test_api("Add Product to Wishlist", "POST", f"/api/v1/wishlist/{first_product_id}", token=customer_token, expected_status=200)
    test_api("Get Wishlist", "GET", "/api/v1/wishlist", token=customer_token, expected_status=200)
    test_api("Check If in Wishlist", "GET", f"/api/v1/wishlist/check/{first_product_id}", token=customer_token, expected_status=200)
    test_api("Remove from Wishlist", "DELETE", f"/api/v1/wishlist/{first_product_id}", token=customer_token, expected_status=200)

    # 7. Cart Operations
    test_api("Add Item to Cart", "POST", "/api/v1/cart/items",
             body={"productId": first_product_id, "quantity": 2},
             token=customer_token, expected_status=200)
    
    cart_res = test_api("Get Cart", "GET", "/api/v1/cart", token=customer_token, expected_status=200)
    cart_item_id = cart_res["data"][0]["id"] if cart_res and cart_res.get("data") and len(cart_res["data"]) > 0 else None

    if cart_item_id:
        test_api("Update Cart Item Quantity", "PUT", f"/api/v1/cart/items/{cart_item_id}?quantity=1", token=customer_token, expected_status=200)

    test_api("Validate Cart Stock", "POST", "/api/v1/cart/validate", token=customer_token, expected_status=200)

    # 8. Coupons
    test_api("Get My Available Coupons", "GET", "/api/v1/coupons/my-coupons", token=customer_token, expected_status=200)

    # 9. Place Order & Order Operations
    if address_id:
        order_res = test_api("Place Order (COD)", "POST", "/api/v1/orders",
                             body={
                                 "addressId": address_id,
                                 "paymentMethod": "COD",
                                 "notes": "Deliver during daytime"
                             },
                             token=customer_token, expected_status=200)
        
        order_uuid = order_res["data"]["orderUuid"] if order_res and order_res.get("data") else None
        if order_uuid:
            test_api("List User Orders", "GET", "/api/v1/orders", token=customer_token, expected_status=200)
            test_api("Get Order Details by UUID", "GET", f"/api/v1/orders/{order_uuid}", token=customer_token, expected_status=200)
            test_api("Download Order Invoice PDF", "GET", f"/api/v1/orders/{order_uuid}/invoice", token=customer_token, expected_status=200)
            test_api("Cancel Order", "POST", f"/api/v1/orders/{order_uuid}/cancel", token=customer_token, expected_status=200)

    # 10. Review Helpful count
    reviews_list = test_api("Get Product Reviews List", "GET", f"/api/v1/reviews/products/{first_product_id}", expected_status=200)
    if reviews_list and reviews_list.get("data") and reviews_list["data"].get("content") and len(reviews_list["data"]["content"]) > 0:
        first_review_id = reviews_list["data"]["content"][0]["id"]
        test_api("Mark Review Helpful", "POST", f"/api/v1/reviews/{first_review_id}/helpful", token=customer_token, expected_status=200)

    # 11. Seller Operations
    if seller_token:
        test_api("Seller Dashboard Stats", "GET", "/api/v1/seller/dashboard", token=seller_token, expected_status=200)
        test_api("Seller Sentiment Insights", "GET", "/api/v1/seller/sentiment-insights", token=seller_token, expected_status=200)
        test_api("Seller My Products", "GET", "/api/v1/seller/products", token=seller_token, expected_status=200)
        test_api("Seller Recent Orders", "GET", "/api/v1/seller/orders", token=seller_token, expected_status=200)

    # 12. Admin Operations
    if admin_token:
        test_api("Admin Sales Analytics", "GET", "/api/v1/admin/analytics/sales", token=admin_token, expected_status=200)
        test_api("Admin Top Products Analytics", "GET", "/api/v1/admin/analytics/products", token=admin_token, expected_status=200)
        test_api("Admin List Users", "GET", "/api/v1/admin/users", token=admin_token, expected_status=200)
        test_api("Admin List Products", "GET", "/api/v1/admin/products", token=admin_token, expected_status=200)
        test_api("Admin List Orders", "GET", "/api/v1/admin/orders", token=admin_token, expected_status=200)
        test_api("Admin List Coupons", "GET", "/api/v1/admin/coupons", token=admin_token, expected_status=200)
        test_api("Admin List Flash Sales", "GET", "/api/v1/admin/flash-sales", token=admin_token, expected_status=200)
        test_api("Admin Sentiment Overview", "GET", "/api/v1/admin/sentiment/overview", token=admin_token, expected_status=200)
        test_api("Admin Batch Sentiment Analysis", "POST", "/api/v1/admin/reviews/batch-sentiment-analysis", token=admin_token, expected_status=200)

    print("\n" + "=" * 70)
    print(f"📊 TEST SUITE SUMMARY: {len(passed_tests)} PASSED, {len(failed_tests)} FAILED")
    print("=" * 70)
    if failed_tests:
        print("\n❌ FAILED TESTS:")
        for name, err in failed_tests:
            print(f"  - {name}: {err}")
        sys.exit(1)
    else:
        print("\n🎉 ALL API ENDPOINTS PASSED WITH 100% SUCCESS!")

if __name__ == "__main__":
    main()
