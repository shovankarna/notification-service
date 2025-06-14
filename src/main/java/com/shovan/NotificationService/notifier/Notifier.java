package com.shovan.NotificationService.notifier;

import com.shovan.NotificationService.entity.Notification;

/**
 * Core abstraction for sending notifications.
 * 
 * <p>
 * <b>Pattern:</b> Defines the Notifier interface for all channels.
 * </p>
 */
public interface Notifier {

    /**
     * Send the given notification with rendered content.
     * 
     * @param notification    the Notification entity containing metadata and
     *                        parameters
     * @param renderedContent the body/content produced after template rendering
     */
    void send(Notification notification, String renderedContent);
}

// Where the patterns are used:

// Adapter: Each concrete *Notifier wraps a third‐party client (JavaMailSender,
// Twilio SDK, FirebaseMessaging) and adapts it to our Notifier interface.

// Template Method: AbstractNotifier.send(...) provides the fixed algorithm
// (prepare → doSend → postProcess), while subclasses implement only the
// doSend() primitive.

// Singleton: All @Component beans (including these notifiers) are singletons by
// default in Spring.

// With this in place, our NotificationListener can simply:

// Render the template,

// Fetch the right Notifier via NotifierFactory, and

// Call notifier.send(notification, renderedContent).