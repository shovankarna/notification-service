package com.shovan.NotificationService.factory;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.shovan.NotificationService.enums.Channel;
import com.shovan.NotificationService.notifier.EmailNotifier;
import com.shovan.NotificationService.notifier.Notifier;
import com.shovan.NotificationService.notifier.PushNotifier;
import com.shovan.NotificationService.notifier.SmsNotifier;

import lombok.AllArgsConstructor;

/**
 * Factory for creating Notifier instances based on the Channel.
 * <p>
 * This encapsulates the logic of “which concrete class to instantiate”
 * so clients (like our Facade or Listener) don’t need to know the details.
 * </p>
 */
@Component
public class NotifierFactory {

    /**
     * Internal map of Channel → Notifier implementation.
     * We use EnumMap for performance when keys are enum values.
     */
    private final Map<Channel, Notifier> notifiers = new EnumMap<>(Channel.class);

    /**
     * Constructor: wires in each concrete Notifier and registers
     * it under its Channel key.
     *
     * @param emailNotifier adapter for EMAIL channel
     * @param smsNotifier   adapter for SMS channel
     * @param pushNotifier  adapter for PUSH channel
     */
    public NotifierFactory(EmailNotifier emailNotifier,
            SmsNotifier smsNotifier,
            PushNotifier pushNotifier) {
        // Populate the map here—this is the core of the Factory pattern.
        notifiers.put(Channel.EMAIL, emailNotifier);
        notifiers.put(Channel.SMS, smsNotifier);
        notifiers.put(Channel.PUSH, pushNotifier);
    }

    /**
     * Factory method: return the correct Notifier for the requested Channel.
     * <ul>
     * <li>If the channel is EMAIL, returns EmailNotifier</li>
     * <li>If SMS, returns SmsNotifier</li>
     * <li>If PUSH, returns PushNotifier</li>
     * </ul>
     * 
     * @param channel the medium to send on
     * @return a Notifier that knows how to handle that channel
     * @throws IllegalArgumentException if no Notifier is registered for the channel
     */
    public Notifier getNotifier(Channel channel) {
        Notifier notifier = notifiers.get(channel);
        if (notifier == null) {
            throw new IllegalArgumentException("Unsupported channel: " + channel);
        }
        return notifier;
    }

}

// Why Factory?

// Centralizes object‐creation logic

// Makes it trivial to add new channels: register a new SomeNewNotifier bean and
// add to the map

// Keeps client code (e.g. our Facade or Listener) clean—just call
// factory.getNotifier(channel)