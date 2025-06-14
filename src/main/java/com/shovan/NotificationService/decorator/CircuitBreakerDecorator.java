package com.shovan.NotificationService.decorator;

import com.shovan.NotificationService.entity.Notification;
import com.shovan.NotificationService.notifier.Notifier;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Decorator that adds a Circuit Breaker around any Notifier.
 *
 * <p>
 * <b>Pattern:</b> <i>Decorator</i> – wraps a Notifier and
 * adds circuit‐breaker behavior without modifying its code.
 * </p>
 */

@Slf4j
public class CircuitBreakerDecorator implements Notifier {

    /** Underlying notifier that actually sends the notification. */
    private final Notifier delegate;

    /** Resilience4j CircuitBreaker instance for this channel. */
    private final CircuitBreaker circuitBreaker;

    /**
     * @param delegate           the Notifier to wrap
     * @param registry           Resilience4j registry to obtain breakers
     * @param circuitBreakerName the name/key of the circuit breaker (matches
     *                           config)
     */
    public CircuitBreakerDecorator(Notifier delegate,
            CircuitBreakerRegistry registry,
            String circuitBreakerName) {
        this.delegate = delegate;
        this.circuitBreaker = registry.circuitBreaker(circuitBreakerName);
    }

    /**
     * Wraps the delegate.send(...) call in a circuit breaker.
     *
     * @param notification    the notification entity to send
     * @param renderedContent the already-rendered content (HTML/text)
     */
    @Override
    public void send(Notification notification, String renderedContent) {
        // Decorate the send() call with circuit-breaker logic
        Runnable decorated = CircuitBreaker
                .decorateRunnable(circuitBreaker, () -> delegate.send(notification, renderedContent));

        try {
            decorated.run();
        } catch (Exception ex) {
            log.warn("CircuitBreaker intercepted failure for notification id={}: {}",
                    notification.getId(), ex.getMessage());
            throw ex;
        }
    }
}
