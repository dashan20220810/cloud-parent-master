package com.qianyi.modulespringrabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Slf4j
@Configuration
public class RabbitMqConfig {

    @Autowired
    private ConnectionFactory rabbitConnectionFactory;

    @Autowired
    private RabbitProperties properties;

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(){
        SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
        containerFactory.setConnectionFactory(rabbitConnectionFactory);
        containerFactory.setConcurrentConsumers(1);
        containerFactory.setMaxConcurrentConsumers(1);
        containerFactory.setPrefetchCount(1);
        containerFactory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        containerFactory.setMessageConverter(new Jackson2JsonMessageConverter());
        containerFactory.setAdviceChain(
                RetryInterceptorBuilder
                        .stateless()
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .retryOperations(rabbitRetryTemplate())
                        .build()
        );
        return containerFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(rabbitConnectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setRetryTemplate(rabbitRetryTemplate());
        rabbitTemplate.setConfirmCallback(
                (correlationData, b, s) -> {
                    log.info("ComfirmCallback  相关数据: {}",correlationData);
                    log.info("ComfirmCallback  确认情况: {}",b);
                    log.info("ComfirmCallback  原因: {}",s);
                }
        );

        rabbitTemplate.setReturnsCallback(
                (returnedMessage -> {
                    log.info("returnCallback: 消息: {}",returnedMessage.getMessage());
                    log.info("returnCallback: 回应码: {}",returnedMessage.getReplyCode());
                    log.info("returnCallback: 回应消息: {}",returnedMessage.getReplyText());
                    log.info("returnCallback: 交换机: {}",returnedMessage.getExchange());
                    log.info("returnCallback: 路由键: {}",returnedMessage.getRoutingKey());
                })
        );
        return rabbitTemplate;
    }

    @Bean
    public RetryTemplate rabbitRetryTemplate(){
        RetryTemplate retryTemplate = new RetryTemplate();

        retryTemplate.registerListener(new RetryListener() {
            @Override
            public <T, E extends Throwable> boolean open(RetryContext retryContext, RetryCallback<T, E> retryCallback) {
                return true;
            }

            @Override
            public <T, E extends Throwable> void close(RetryContext retryContext, RetryCallback<T, E> retryCallback, Throwable throwable) {

                return;
            }

            @Override
            public <T, E extends Throwable> void onError(RetryContext retryContext, RetryCallback<T, E> retryCallback, Throwable throwable) {

            }
        });

        retryTemplate.setBackOffPolicy(backOffPolicyByProperties());
        retryTemplate.setRetryPolicy(retryPolicyByProperties());
        return retryTemplate;
    }

    @Bean
    public ExponentialBackOffPolicy backOffPolicyByProperties(){
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        long maxInterval = properties.getListener().getSimple().getRetry().getMaxInterval().getSeconds();
        long initialInterval = properties.getListener().getSimple().getRetry().getInitialInterval().getSeconds();
        double multiplier = properties.getListener().getSimple().getRetry().getMultiplier();

        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMaxInterval(maxInterval);
        backOffPolicy.setMultiplier(multiplier);

        return backOffPolicy;
    }

    @Bean
    public SimpleRetryPolicy retryPolicyByProperties(){
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        int maxAttempts = properties.getListener().getSimple().getRetry().getMaxAttempts();
        retryPolicy.setMaxAttempts(maxAttempts);
        return retryPolicy;
    }
}
