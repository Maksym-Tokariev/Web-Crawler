package com.webcrawler.config;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.*;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
  Configures RabbitMQ for reactive processes.
 */

@Slf4j
@Configuration
public class RabbitConfig {

    @Value("${spring.rabbitmq.host}")
    private String RABBIT_MQ_HOST;

    @Value("${spring.rabbitmq.username}")
    private String RABBIT_MQ_USER;

    @Value("${spring.rabbitmq.password}")
    private String RABBIT_MQ_PASSWORD;

    @Value("${spring.rabbitmq.port}")
    private int RABBIT_MQ_PORT;

    public static final String EXCHANGE = "crawler.exchange";
    public static final String ROUTING_KEY = "url.routing.key";
    public static final String QUEUE_NAME = "crawler.queue";

    @Bean
    public ConnectionFactory connectionFactory() {
        log.info("Creating ConnectionFactory");
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(RABBIT_MQ_HOST);
        connectionFactory.setUsername(RABBIT_MQ_USER);
        connectionFactory.setPassword(RABBIT_MQ_PASSWORD);
        connectionFactory.setPort(RABBIT_MQ_PORT);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setAutomaticRecoveryEnabled(true);

        return connectionFactory;
    }

    @Bean
    public Queue urlQueue(RabbitAdmin rabbitAdmin) {
        Queue queue = new Queue(QUEUE_NAME, true);
        rabbitAdmin.declareQueue(queue);
        log.info("Queue declared: {}", QUEUE_NAME);
        return queue;
    }

    @Bean
    public TopicExchange topicExchange(RabbitAdmin rabbitAdmin) {
        TopicExchange exchange = new TopicExchange(EXCHANGE);
        rabbitAdmin.declareExchange(exchange);
        log.info("Exchange declared: {}", EXCHANGE);
        return exchange;
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange, RabbitAdmin rabbitAdmin) {
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
        rabbitAdmin.declareBinding(binding);
        log.info("Binding declared: {} -> {}", QUEUE_NAME, EXCHANGE);
        return binding;
    }

    @Bean
    public Mono<Connection> connectionMono(ConnectionFactory connectionFactory) {
        log.info("Creating connection");
        return Mono.fromCallable(() -> connectionFactory.newConnection("crawler"))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .cache();
    }

    @Bean
    public RabbitAdmin rabbitAdmin(
            org.springframework.amqp.rabbit.connection.ConnectionFactory springConnectionFactory
    ) {
        return new RabbitAdmin(springConnectionFactory);
    }


    @Bean
    public SenderOptions senderOptions(Mono<Connection> connectionMono) {
        return new SenderOptions().connectionMono(connectionMono)
                .resourceManagementScheduler(Schedulers.boundedElastic());
    }

    @Bean
    public Sender sender(SenderOptions senderOptions) {
        return RabbitFlux.createSender(senderOptions);
    }

    @Bean
    public ReceiverOptions receiverOptions(Mono<Connection> connectionMono) {
        return new ReceiverOptions().connectionMono(connectionMono);
    }

    @Bean
    public Receiver receiver(ReceiverOptions receiverOptions) {
        return RabbitFlux.createReceiver(receiverOptions);
    }
}
