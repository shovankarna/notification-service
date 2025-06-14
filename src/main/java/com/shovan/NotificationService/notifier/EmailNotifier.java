package com.shovan.NotificationService.notifier;

import java.util.Map;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends notifications via SMTP/JavaMail.
 * <p>
 * <b>Pattern:</b> Adapter – wraps Spring’s JavaMailSender so it fits our
 * Notifier interface.
 * </p>
 */
@Component
@AllArgsConstructor
@Slf4j
public class EmailNotifier extends AbstractNotifier {

    private final JavaMailSender javaMailSender;
    private final ObjectMapper objectMapper;

    /**
     * Perform the actual email send.
     * Expects parameters JSON to contain:
     * - "email": recipient address
     * - optional "subject"
     */
    @Override
    protected void doSend() {

        try {

            @SuppressWarnings("unchecked")
            Map<String, Object> params = objectMapper.readValue(notification.getParameters(), Map.class);

            String to = (String) params.get("email");
            String subject = (String) params.getOrDefault("subject", "NOtification from our service");

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, "UTF-8");

            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(content, true);

            javaMailSender.send(message);
            log.debug("Email sent [id={}, to={}]", notification.getId(), to);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}
