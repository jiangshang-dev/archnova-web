package com.archnova.mq;

import com.archnova.config.OrderMqConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDelayPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${archnova.order.cancel-delay-ms:86400000}")
    private long cancelDelayMs;

    public void publish(String orderNo) {
        rabbitTemplate.convertAndSend(OrderMqConfig.DELAY_EXCHANGE, OrderMqConfig.DELAY_KEY, orderNo, message -> {
            message.getMessageProperties().setExpiration(String.valueOf(cancelDelayMs));
            return message;
        });
    }
}
