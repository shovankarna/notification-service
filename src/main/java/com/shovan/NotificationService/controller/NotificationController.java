package com.shovan.NotificationService.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shovan.NotificationService.config.RabbitMQConfig;
import com.shovan.NotificationService.dto.NotificationRequestDTO;
import com.shovan.NotificationService.dto.NotificationResponseDTO;
import com.shovan.NotificationService.entity.Notification;
import com.shovan.NotificationService.enums.Channel;
import com.shovan.NotificationService.enums.NotificationStatus;
import com.shovan.NotificationService.repository.NotificationRepository;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * REST controller that handles notification requests.
 * 
 * Exposes a POST endpoint to accept notification details,
 * persist them as PENDING, enqueue for async processing,
 * and return the created record IDs.
 */
@RestController
@RequestMapping("/api/notifications")
@Validated
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Constructor for dependency injection.
     *
     * @param notificationRepository repository to persist Notification entities
     * @param rabbitTemplate         template for publishing to RabbitMQ
     * @param objectMapper           Jackson mapper for serializing parameters
     */
    public NotificationController(NotificationRepository notificationRepository,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Accepts a NotificationRequest, creates one Notification entity per channel,
     * saves each with status=PENDING, publishes to RabbitMQ for async handling,
     * and returns the list of created IDs.
     *
     * @param request the incoming notification details (validated via JSR-380)
     * @return HTTP 202 Accepted with a NotificationResponse listing new IDs
     */
    @PostMapping
    public ResponseEntity<NotificationResponseDTO> sendNotifications(
            @Valid @RequestBody NotificationRequestDTO request) {

        // Serialize the parameters map to a JSON string for storage
        final String paramsJson;
        try {
            paramsJson = objectMapper.writeValueAsString(request.getParameters());
        } catch (JsonProcessingException e) {
            // Wrap and rethrow so GlobalExceptionHandler can map to a 400/500
            throw new RuntimeException("Failed to serialize notification parameters", e);
        }

        List<Long> notificationIds = new ArrayList<>();
        // For each requested channel, persist and enqueue a Notification
        for (Channel channel : request.getChannels()) {
            Notification notification = Notification.builder() // ← Builder pattern
                    .channel(channel)
                    .templateName(request.getTemplateName())
                    .parameters(paramsJson)
                    .status(NotificationStatus.PENDING)
                    .attempts(0)
                    .build();

            // Persist with initial status=PENDING
            Notification saved = notificationRepository.save(notification);
            notificationIds.add(saved.getId());

            // Publish the saved entity to RabbitMQ for async processing
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY,
                    saved);
        }

        // Build and return the response containing all created IDs
        NotificationResponseDTO response = NotificationResponseDTO.builder()
                .notificationIds(notificationIds)
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}