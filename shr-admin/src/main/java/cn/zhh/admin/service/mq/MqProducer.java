package cn.zhh.admin.service.mq;

import cn.zhh.common.constant.MqConsts;
import cn.zhh.common.dto.mq.BaseMqMessage;
import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
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
     * 发送职位搜索消息
     *
     * @param msg
     */
    public void sendSearchPositionInfoMsg(SearchPositionInfoMsg msg) {
        send(MqConsts.SEARCH_POSITION_INFO_TOPIC_EXCHANGE_NAME, "", msg);
    }

    private void send(String exchange, String routingKey, BaseMqMessage message) {
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(message.getMessageId());
        rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
    }
}
