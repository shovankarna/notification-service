package com.shovan.NotificationService.notifier;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends notifications via Twilio SMS.
 * <p>
 * <b>Pattern:</b> Adapter – wraps Twilio’s API to match our Notifier interface.
 * </p>
 */

@Component
@AllArgsConstructor
@Slf4j
public class SmsNotifier extends AbstractNotifier {
    
    private final ObjectMapper objectMapper;
    private final String twilioSid;
    private final String twilioToken;
    private final String fromNumber;


    /** Initialize the Twilio SDK on startup. */
    @PostConstruct
    public void init() {
        Twilio.init(twilioSid, twilioToken);
    }

    /**
     * Perform the actual SMS send.
     * Expects parameters JSON to contain "phoneNumber".
     */
    @Override
    protected void doSend() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> params = objectMapper.readValue(
                    notification.getParameters(), Map.class);

            String to = (String) params.get("phoneNumber");
            Message msg = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromNumber),
                    content).create();

            log.debug("SMS sent [id={}, to={}, sid={}]",
                    notification.getId(), to, msg.getSid());
        } catch (Exception ex) {
            throw new RuntimeException(
                    "SmsNotifier failed for notification id=" + notification.getId(), ex);
        }
    }
}
