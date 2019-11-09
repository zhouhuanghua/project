package cn.zhh.admin.service.mq;

import cn.zhh.common.constant.MqConsts;
import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.dto.mq.SchoolInternshipMsg;
import cn.zhh.common.enums.PositionSourceEnum;
import cn.zhh.common.util.JsonUtils;
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
    private PositionInfoMsgService positionInfoMsgService;

    @Autowired
    private SchoolInternshipMsgService schoolInternshipMsgService;

    /**
     * 消费职位信息
     *
     * @param positionInfoMsg
     * @param headers
     * @param channel
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConsts.POSITION_INFO_QUEUE_NAME, durable = "true"),
            exchange = @Exchange(name = MqConsts.DIRECT_EXCHANGE_NAME),
            key = MqConsts.POSITION_INFO_ROUTING_KEY
    ))
    @RabbitHandler
    public void consumePositionInfoMsg(@Payload PositionInfoMsg positionInfoMsg, @Headers Map<String, Object> headers, Channel channel) throws Exception {
        // 处理消息
        String source = PositionSourceEnum.code2desc(positionInfoMsg.getSource());
        String name = positionInfoMsg.getName();
        try {
            positionInfoMsgService.process(positionInfoMsg);
            log.info("职位【来源：{}，名称：{}】消费成功！", source, name);
        } catch (Exception e) {
            log.error(String.format("职位【来源：%s，名称：%s】消费失败！", source, name), e);
        }

        // 手动签收消息
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag, false);
    }

    /**
     * 消费校招-实习-数据
     *
     * @param msgBytes
     * @param headers
     * @param channel
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConsts.SCHOOL_INTERNSHIP_QUEUE_NAME, durable = "true"),
            exchange = @Exchange(name = MqConsts.DIRECT_EXCHANGE_NAME),
            key = MqConsts.SCHOOL_INTERNSHIP_ROUTING_KEY
    ))
    @RabbitHandler
    public void consumeSchoolInternshipMsg(byte[] msgBytes, @Headers Map<String, Object> headers, Channel channel) throws Exception {
        // 处理消息
        SchoolInternshipMsg schoolInternshipMsg = JsonUtils.bytes2Pojo(msgBytes, SchoolInternshipMsg.class);
        String name = schoolInternshipMsg.getName();
        log.info("开始消费校招-实习消息：{}", name);
        try {
            schoolInternshipMsgService.process(schoolInternshipMsg);
            log.info("校招-实习消息({})消费成功！", name);
        } catch (Exception e) {
            log.error(String.format("校招-实习消息({})消费失败！", name), e);
        }

        // 手动签收消息
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag, false);
    }
}
