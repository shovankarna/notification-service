package com.shovan.NotificationService.notifier;

import com.shovan.NotificationService.entity.Notification;
import com.shovan.NotificationService.enums.NotificationStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * Base class for all Notifier implementations.
 * <p>
 * <b>Pattern:</b> Template Method – defines the skeleton of the send workflow:
 * <ol>
 * <li>prepare()</li>
 * <li>doSend()</li>
 * <li>postProcess()</li>
 * </ol>
 * Subclasses override doSend() to plug in channel‐specific logic.
 * </p>
 */

@Slf4j
public abstract class AbstractNotifier implements Notifier {

    protected Notification notification;

    protected String content;

    /**
     * Final Template Method: orchestrates the send steps.
     */
    @Override
    public final void send(Notification notification, String renderedContent) {

        prepare(notification, renderedContent);

        try {
            doSend();
            this.notification.setStatus(NotificationStatus.SUCCESS);
            log.info("Notification id={} sent successfully via {}",
                    this.notification.getId(), this.notification.getChannel());

        } catch (Exception ex) {

            this.notification.setStatus(NotificationStatus.FAILED);
            log.error("Failed to send notification id={} via {}",
                    this.notification.getId(), this.notification.getChannel(), ex);
            throw ex instanceof RuntimeException
                    ? (RuntimeException) ex
                    : new RuntimeException(ex);
        } finally {
            postProcess(this.notification);
        }
    }

    /**
     * Hook: initialize fields before sending.
     */
    protected void prepare(Notification notification, String renderedContent) {
        this.notification = notification;
        this.content = renderedContent;
    }

    /**
     * Primitive operation: subclasses must implement this to perform the actual
     * send.
     */
    protected abstract void doSend();

    /**
     * Hook: update attempt count (persisted later by listener).
     */
    protected void postProcess(Notification notification) {
        notification.setAttempts(notification.getAttempts() + 1);
    }

}
