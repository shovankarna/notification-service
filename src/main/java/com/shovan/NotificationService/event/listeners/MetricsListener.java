package com.shovan.NotificationService.event.listeners;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.instrument.Counter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener that tracks notification outcomes and reports metrics via Micrometer.
 *
 * <p><b>Pattern:</b> <i>Observer</i> – reacts to published
 * NotificationSentEvent instances without coupling to the publisher.</p>
 */
@Component
@Slf4j
public class MetricsListener {


    private final MeterRegistry meterRegistry;

    /**
     * Constructor injection of Micrometer’s MeterRegistry.
     *
     * @param meterRegistry the central registry for meters (counters, gauges, etc.)
     */
    public MetricsListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Handle NotificationSentEvent and increment corresponding counters.
     *
     * <p>We tag each counter by {@code status} and {@code channel}
     * so dashboards can break down success vs failure per channel.</p>
     *
     * @param event the event containing the processed Notification
     */
    @EventListener
    public void onNotificationSent(NotificationSentEvent event) {
        var notification = event.getNotification();
        String status = notification.getStatus().name().toLowerCase();
        String channel = notification.getChannel().name().toLowerCase();

        // Build metric name and tags
        String metricName = "notification.sent";
        Counter counter = Counter.builder(metricName)
                .description("Count of notifications sent")
                .tag("status", status)
                .tag("channel", channel)
                .register(meterRegistry);

        // Increment the counter for this outcome
        counter.increment();

        log.debug("MetricsListener incremented '{}' counter [status={}, channel={}]",
                  metricName, status, channel);
    }
}