package com.shovan.NotificationService.decorator;

import com.shovan.NotificationService.entity.Notification;
import com.shovan.NotificationService.notifier.Notifier;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Decorator that adds retry behavior around any Notifier.
 *
 * <p>
 * <b>Pattern:</b> <i>Decorator</i> – wraps a Notifier and
 * retries the send(...) call on transient failures.
 * </p>
 */
@Slf4j
public class RetryDecorator implements Notifier {

    /** Underlying notifier (possibly already wrapped by other decorators). */
    private final Notifier delegate;

    /** Resilience4j Retry instance for this channel. */
    private final Retry retry;

    /**
     * @param delegate  the Notifier to wrap
     * @param registry  Resilience4j registry to obtain retry policies
     * @param retryName the name/key of the retry config (matches config)
     */
    public RetryDecorator(Notifier delegate,
            RetryRegistry registry,
            String retryName) {
        this.delegate = delegate;
        this.retry = registry.retry(retryName);
    }

    /**
     * Wraps the delegate.send(...) call in retry logic.
     *
     * @param notification    the notification entity to send
     * @param renderedContent the already-rendered content (HTML/text)
     */
    @Override
    public void send(Notification notification, String renderedContent) {
        // Decorate the send() call with retry logic
        Runnable decorated = Retry
                .decorateRunnable(retry, () -> delegate.send(notification, renderedContent));

        try {
            decorated.run();
        } catch (Exception ex) {
            log.warn("RetryDecorator exhausted retries for notification id={}: {}",
                    notification.getId(), ex.getMessage());
            throw ex;
        }
    }
}
