package com.archnova.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单延迟取消：消息先进入延迟队列，到期后经死信交换机转到取消队列。
 */
@Configuration
public class OrderMqConfig {

    public static final String DELAY_EXCHANGE = "archnova.order.delay.exchange";
    public static final String DELAY_QUEUE = "archnova.order.delay.queue";
    public static final String DELAY_KEY = "order.delay";
    public static final String CANCEL_EXCHANGE = "archnova.order.cancel.exchange";
    public static final String CANCEL_QUEUE = "archnova.order.cancel.queue";
    public static final String CANCEL_KEY = "order.cancel";

    @Bean
    public DirectExchange orderDelayExchange() {
        return new DirectExchange(DELAY_EXCHANGE);
    }

    @Bean
    public DirectExchange orderCancelExchange() {
        return new DirectExchange(CANCEL_EXCHANGE);
    }

    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable(DELAY_QUEUE)
                .deadLetterExchange(CANCEL_EXCHANGE)
                .deadLetterRoutingKey(CANCEL_KEY)
                .build();
    }

    @Bean
    public Queue orderCancelQueue() {
        return QueueBuilder.durable(CANCEL_QUEUE).build();
    }

    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue()).to(orderDelayExchange()).with(DELAY_KEY);
    }

    @Bean
    public Binding orderCancelBinding() {
        return BindingBuilder.bind(orderCancelQueue()).to(orderCancelExchange()).with(CANCEL_KEY);
    }
}
