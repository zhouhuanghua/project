package cn.zhh.crawler.service;

import cn.zhh.common.constant.MqConsts;
import cn.zhh.common.dto.mq.QueryCompanyCommentMsg;
import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
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
    private ObjectMapper objectMapper;

    /**
     * 智联消费搜索职位信息消息
     *
     * @param messageBytes
     * @param headers
     * @param channel
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConsts.SEARCH_POSITION_INFO_ZHILIAN_QUEUE_NAME, durable = "true"),
            exchange = @Exchange(name = MqConsts.SEARCH_POSITION_INFO_TOPIC_EXCHANGE_NAME, type = "topic"),
            key = ""
    ))
    @RabbitHandler
    public void consumeSearchPositionInfoMsg(byte[] messageBytes, @Headers Map<String, Object> headers, Channel channel) throws Exception {
        // 转化
        SearchPositionInfoMsg searchPositionInfoMsg = JsonUtils.bytes2Pojo(messageBytes, SearchPositionInfoMsg.class);

        // todo

        // 手动签收消息
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag, false);
    }

    /**
     * 消费查询职位评论消息
     * @param messageBytes
     * @param headers
     * @param channel
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConsts.QUERY_COMPANY_COMMENT_QUEUE_NAME, durable = "true"),
            exchange = @Exchange(name = MqConsts.DIRECT_EXCHANGE_NAME),
            key = MqConsts.QUERY_COMPANY_COMMENT_ROUTING_KEY
    ))
    @RabbitHandler
    public void consumeQueryCompanyCommentMsg(byte[] messageBytes, @Headers Map<String, Object> headers, Channel channel) throws Exception {
        // 转化
        QueryCompanyCommentMsg queryCompanyCommentMsg = JsonUtils.bytes2Pojo(messageBytes, QueryCompanyCommentMsg.class);

        // todo

        // 手动签收消息
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag, false);
    }
}
