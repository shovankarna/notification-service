package com.shovan.NotificationService.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  // ← Spring manages this as a singleton bean
public class RabbitMQConfig {

    /** Name of the RabbitMQ exchange for notifications. */
    public static final String EXCHANGE = "notification.exchange";

    /** Routing key for delivering messages to the notification queue. */
    public static final String ROUTING_KEY = "notification.routingkey";

    /** Name of the queue that will receive notification messages. */
    public static final String QUEUE = "notification.queue";

    /**
     * Declare a durable DirectExchange.
     * 
     * @return the DirectExchange instance
     */
    @Bean
    public DirectExchange notificationExchange() {
        // durable=true, autoDelete=false
        return new DirectExchange(EXCHANGE, true, false);
    }

    /**
     * Declare a durable Queue.
     * 
     * @return the Queue instance
     */
    @Bean
    public Queue notificationQueue() {
        // durable=true, exclusive=false, autoDelete=false
        return new Queue(QUEUE, true, false, false);
    }

    /**
     * Bind the queue to the exchange under the given routing key.
     * Messages sent to the exchange with ROUTING_KEY will end up in this queue.
     * 
     * @param queue    the notification queue bean
     * @param exchange the notification exchange bean
     * @return the Binding between queue and exchange
     */
    @Bean
    public Binding notificationBinding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    /**
     * Configure Jackson-based JSON message converter.
     * This allows us to publish/consume POJOs as JSON.
     * 
     * @return the Jackson2JsonMessageConverter
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configure RabbitTemplate to use our JSON converter.
     * Use this template for publishing messages.
     * 
     * @param connectionFactory the RabbitMQ connection factory
     * @return the configured RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }

}