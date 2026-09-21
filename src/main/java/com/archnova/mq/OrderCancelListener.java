package com.archnova.mq;

import com.archnova.config.OrderMqConfig;
import com.archnova.service.ShopOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCancelListener {

    private final ShopOrderService shopOrderService;

    @RabbitListener(queues = OrderMqConfig.CANCEL_QUEUE)
    public void onCancel(String orderNo) {
        shopOrderService.cancelIfPending(orderNo);
    }
}
