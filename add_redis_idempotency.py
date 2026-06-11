import os
import re

filepath = 'src/main/java/com/swiftcart/service/PaymentService.java'
with open(filepath, 'r') as f:
    content = f.read()

target = '''    public void processWebhook(String payload, String signatureHeader) {
        // Verify webhook authenticity using webhook secret
        boolean isValid = verifyWebhookSignature(payload, signatureHeader, razorpayWebhookSecret);
        if (!isValid) {
            log.warn("Invalid Razorpay webhook signature");
            throw new RuntimeException("Invalid webhook signature, HMAC verification failed");
        }

        JSONObject event = new JSONObject(payload);'''

replacement = '''    public void processWebhook(String payload, String signatureHeader) {
        // Verify webhook authenticity using webhook secret
        boolean isValid = verifyWebhookSignature(payload, signatureHeader, razorpayWebhookSecret);
        if (!isValid) {
            log.warn("Invalid Razorpay webhook signature");
            throw new RuntimeException("Invalid webhook signature, HMAC verification failed");
        }

        // Redis Idempotency Check using webhook signature as unique key
        String idempotencyKey = "webhook_processed:" + signatureHeader;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "true", Duration.ofHours(24));
        if (Boolean.FALSE.equals(isNew)) {
            log.info("Webhook already processed (Idempotency key: {})", signatureHeader);
            return;
        }

        JSONObject event = new JSONObject(payload);'''

content = content.replace(target, replacement)

with open(filepath, 'w') as f:
    f.write(content)
