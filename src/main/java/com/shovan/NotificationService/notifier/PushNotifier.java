package com.shovan.NotificationService.notifier;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends push notifications via Firebase Cloud Messaging.
 * <p>
 * <b>Pattern:</b> Adapter – wraps FirebaseMessaging so it fits our Notifier
 * interface.
 * </p>
 */
@Component
@Slf4j
@AllArgsConstructor
public class PushNotifier extends AbstractNotifier {

    private final FirebaseMessaging fcm;
    private final ObjectMapper objectMapper;

    /**
     * Perform the actual push send.
     * Expects parameters JSON to contain:
     * - "deviceToken"
     * - optional "title"
     */
    @Override
    protected void doSend() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> params = objectMapper.readValue(
                    notification.getParameters(), Map.class);

            String token = (String) params.get("deviceToken");
            String title = (String) params.getOrDefault("title", "Notification");

            Notification fcmNotif = Notification.builder()
                    .setTitle(title)
                    .setBody(content)
                    .build();

            Message fcmMsg = Message.builder()
                    .setToken(token)
                    .setNotification(fcmNotif)
                    .build();

            String response = fcm.send(fcmMsg);
            log.debug("Push sent [id={}, token={}, resp={}]",
                    notification.getId(), token, response);
        } catch (Exception ex) {
            throw new RuntimeException(
                    "PushNotifier failed for notification id=" + notification.getId(), ex);
        }
    }
}
