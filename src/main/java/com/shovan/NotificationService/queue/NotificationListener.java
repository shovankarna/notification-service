package com.shovan.NotificationService.queue;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import com.shovan.NotificationService.entity.Notification;
import com.shovan.NotificationService.factory.NotifierFactory;
import com.shovan.NotificationService.notifier.Notifier;
import com.shovan.NotificationService.repository.NotificationRepository;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Listens for incoming Notification messages on the RabbitMQ queue,
 * then orchestrates template rendering, notifier selection, decoration,
 * sending, persistence, and event publication.
 *
 * <p>
 * Patterns used here:
 * <ul>
 * <li><b>Adapter</b> – we call send() on a Notifier, hiding the 3rd-party API
 * details.</li>
 * <li><b>Factory</b> – NotifierFactory decides which Notifier to return for
 * each channel.</li>
 * <li><b>Decorator</b> – we wrap the base Notifier in CircuitBreakerDecorator
 * and RetryDecorator.</li>
 * <li><b>Template Method</b> – AbstractNotifier defines the send(...) workflow;
 * subclasses implement doSend().</li>
 * <li><b>Observer</b> – we publish a NotificationSentEvent for any downstream
 * listeners.</li>
 * </ul>
 * </p>
 */
@Component
@Slf4j
public class NotificationListener {

    private final NotificationRepository notificationRepository;
    private final TemplateService templateService;
    private final NotifierFactory notifierFactory;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationListener(NotificationRepository notificationRepository,
            TemplateService templateService,
            NotifierFactory notifierFactory,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            ApplicationEventPublisher eventPublisher) {
        this.notificationRepository = notificationRepository;
        this.templateService = templateService;
        this.notifierFactory = notifierFactory;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Consume a Notification entity from RabbitMQ and process it end-to-end.
     *
     * @param notification the Notification payload deserialized by
     *                     Jackson2JsonMessageConverter
     */
    @RabbitListener(queues = RabbitConfig.QUEUE)
    @Transactional
    public void handleNotification(Notification notification) {
        log.info("Received Notification id={} channel={}", notification.getId(), notification.getChannel());

        // 1) Render the template into a content string (HTML or text)
        // TemplateService may cache templates in Redis.
        String renderedContent = templateService.render(
                notification.getTemplateName(),
                notification.getParameters());

        // 2) Factory: pick the correct Notifier implementation (Adapter pattern)
        Notifier baseNotifier = notifierFactory.getNotifier(notification.getChannel());

        // 3) Decorator: wrap with circuit breaker
        String cbName = notification.getChannel().name().toLowerCase() + "Notifier";
        Notifier cbProtected = new CircuitBreakerDecorator(baseNotifier, circuitBreakerRegistry, cbName);

        // 4) Decorator: then wrap with retry logic
        Notifier withRetry = new RetryDecorator(cbProtected, retryRegistry, cbName);

        // 5) Template Method: calling send() runs prepare → doSend → postProcess
        try {
            withRetry.send(notification, renderedContent);
        } catch (Exception ex) {
            log.warn("Notification id={} failed after decorators: {}", notification.getId(), ex.getMessage());
            // status and attempts already updated in AbstractNotifier
        }

        // 6) Persist updated status & attempt count back to database
        notificationRepository.save(notification);
        log.info("Notification id={} status={} attempts={}",
                notification.getId(), notification.getStatus(), notification.getAttempts());

        // 7) Observer: publish an event so MetricsListener (or others) can react
        eventPublisher.publishEvent(new NotificationSentEvent(this, notification));
    }
}
