package cn.zhh.crawler.service;

import cn.zhh.common.constant.MqConsts;
import cn.zhh.common.dto.mq.BaseMqMessage;
import cn.zhh.common.dto.mq.PositionInfoMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * MQ生产者
 *
 * @author Zhou Huanghua
 */
@Component
@Slf4j
public class MqProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送职位信息消息
     *
     * @param msg
     */
    public void sendPositionInfoMsg(PositionInfoMsg msg) {
        send(MqConsts.DIRECT_EXCHANGE_NAME, MqConsts.POSITION_INFO_ROUTING_KEY, msg);
    }

    private void send(String exchange, String routingKey, BaseMqMessage message) {
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(message.getMessageId());
        rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
    }
}
