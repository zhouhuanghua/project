package cn.zhh.admin.service.commonservice;

import cn.zhh.admin.entity.ProxyAddress;
import cn.zhh.admin.service.domainservice.ProxyAddressService;
import cn.zhh.common.constant.MqConsts;
import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.dto.mq.ProxyAddressMsg;
import cn.zhh.common.enums.PositionSourceEnum;
import cn.zhh.common.util.BeanUtils;
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
    private PositionInfoService positionInfoService;

    @Autowired
    private ProxyAddressService proxyAddressService;

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
        log.info("开始消费职位信息，来源：{}，名称：{}", source, name);
        try {
            positionInfoService.process(positionInfoMsg);
            log.info("职位(来源：{}，名称：{}) 消费成功！", source, name);
        } catch (Exception e) {
            log.error(String.format("职位(来源：%s，名称：%s) 消费失败！", source, name), e);
        }

        // 手动签收消息
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag, false);
    }

    /**
     * 消费公司评论
     *
     * @param msg
     * @param headers
     * @param channel
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConsts.COMPANY_COMMENT_QUEUE_NAME, durable = "true"),
            exchange = @Exchange(name = MqConsts.DIRECT_EXCHANGE_NAME),
            key = MqConsts.COMPANY_COMMENT_ROUTING_KEY
    ))
    @RabbitHandler
    public void consumeCompanyCommentMsg(@Payload Object msg, @Headers Map<String, Object> headers, Channel channel) throws Exception {
        // 处理消息

        // 手动签收消息
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag, false);
    }

    /**
     * 消费代理地址
     *
     * @param proxyAddressMsg
     * @param headers
     * @param channel
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConsts.PROXY_ADDRESS_QUEUE_NAMW, durable = "true"),
            exchange = @Exchange(name = MqConsts.DIRECT_EXCHANGE_NAME),
            key = MqConsts.PROXY_ADDRESS_ROUTING_KEY
    ))
    @RabbitHandler
    public void consumeProxyAddressMsg(@Payload ProxyAddressMsg proxyAddressMsg, @Headers Map<String, Object> headers, Channel channel) throws Exception {
        // 0、转换消息
        ProxyAddress proxyAddress = new ProxyAddress();
        BeanUtils.copyProperties(proxyAddressMsg, proxyAddress);

        // 1、保存消息
        proxyAddressService.insert(proxyAddress);

        // 2、手动签收
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag, false);
    }
}
