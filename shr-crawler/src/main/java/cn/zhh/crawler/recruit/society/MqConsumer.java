package cn.zhh.crawler.recruit.society;

import cn.zhh.common.constant.MqConsts;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MQ消费者
 *
 * @author Zhou Huanghua
 */
@Component
@Slf4j
public class MqConsumer {

    @Autowired
    private PositionDetailCrawlExecutor positionDetailCrawlExecutor;

    /**
     * 消费职位链接信息
     *
     * @param positionUrlMsg
     * @param headers
     * @param channel
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConsts.POSITION_URL_QUEUE_NAME, durable = "true"),
            exchange = @Exchange(name = MqConsts.DIRECT_EXCHANGE_NAME),
            key = MqConsts.POSITION_URL_ROUTING_KEY
    ))
    @RabbitHandler
    public void consumePositionUrlMsg(@Payload PositionUrlMsg positionUrlMsg, @Headers Map<String, Object> headers, Channel channel) throws Exception {
        // 处理消息
        positionDetailCrawlExecutor.consumeByJsoup(positionUrlMsg);
        // 手动签收消息
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag, false);
    }

    /**
     * 消费职位链接补偿信息
     *
     * @param positionUrlMsg
     * @param headers
     * @param channel
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConsts.POSITION_URL_COMPENSATE_QUEUE_NAME, durable = "true"),
            exchange = @Exchange(name = MqConsts.DIRECT_EXCHANGE_NAME),
            key = MqConsts.POSITION_URL_COMPENSATE_ROUTING_KEY
    ))
    @RabbitHandler
    public void consumePisitionUrlCompensateMsg(@Payload PositionUrlMsg positionUrlMsg, @Headers Map<String, Object> headers, Channel channel) throws Exception {
        // 处理消息
        positionDetailCrawlExecutor.consumeBySelenium(positionUrlMsg);
        // 手动签收消息
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag, false);
    }
}
