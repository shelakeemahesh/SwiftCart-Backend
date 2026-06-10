package com.swiftcart.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    
    private final JavaMailSender mailSender;

    @Value("${twilio.account-sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token}")
    private String twilioAuthToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void initTwilio() {
        try {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            log.info("Twilio initialized successfully with SID: {}", twilioAccountSid);
        } catch (Exception e) {
            log.error("Failed to initialize Twilio: {}", e.getMessage());
        }
    }

    @Async
    public void sendEmail(String to, String subject, String text) {
        log.info("Sending Email to: {}, Subject: {}, Text: {}", to, subject, text);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendOtpSms(String phone, String otp) {
        log.info("--- SMS NOTIFICATION ---");
        log.info("To: {}", phone);
        log.info("Message: Your SwiftCart verification OTP is: {}. Valid for 10 minutes.", otp);
        log.info("-------------------------");
        
        try {
            String formattedPhone = phone.trim();
            if (!formattedPhone.startsWith("+")) {
                if (formattedPhone.length() == 10) {
                    formattedPhone = "+91" + formattedPhone;
                } else {
                    formattedPhone = "+" + formattedPhone;
                }
            }
            
            Message.creator(
                new PhoneNumber(formattedPhone),
                new PhoneNumber(twilioPhoneNumber),
                "Your SwiftCart verification OTP is: " + otp + ". Valid for 10 minutes."
            ).create();
            log.info("Twilio SMS sent successfully to: {}", formattedPhone);
        } catch (Exception e) {
            log.error("Failed to send Twilio SMS to {}: {}", phone, e.getMessage());
        }
    }

    @Async
    public void sendOrderConfirmation(String email, String orderUuid) {
        String subject = "SwiftCart - Order Placed Successfully!";
        String body = "Dear customer,\n\nYour order " + orderUuid + " has been successfully placed. We are processing it now.\n\nThank you for shopping with SwiftCart! ⚡";
        sendEmail(email, subject, body);
    }

    @Async
    public void sendOrderStatusUpdate(String email, String orderUuid, String status) {
        String subject = "SwiftCart - Order Status Updated";
        String body = "Dear customer,\n\nYour order " + orderUuid + " status has been updated to: " + status + ".\n\nThank you for shopping with SwiftCart! ⚡";
        sendEmail(email, subject, body);
    }

    @Async
    public void sendPushNotification(String userId, String title, String body) {
        log.info("Sending FCM Push Notification to user {}: {} - {}", userId, title, body);
        // In production: Integrate Firebase Cloud Messaging SDK
    }
}
