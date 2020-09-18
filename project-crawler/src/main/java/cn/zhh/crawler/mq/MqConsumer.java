package cn.zhh.crawler.mq;

import cn.zhh.common.dto.PositionInfo;
import cn.zhh.common.util.JsonUtils;
import cn.zhh.common.util.ThrowableUtils;
import cn.zhh.crawler.constant.CrawlerConsts;
import cn.zhh.crawler.dto.UrlDTO;
import cn.zhh.crawler.runner.Dao;
import cn.zhh.crawler.runner.ParseDetailRunner;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * MQ消费者
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Component
public class MqConsumer {

    @Autowired
    private ParseDetailRunner parseDetailRunner;

    @Autowired
    private Dao dao;

    private Function<String, String> urlToKeyFunc = url -> url.substring(url.lastIndexOf("jobs/") + 5, url.lastIndexOf(".html"));

    @RabbitListener(queues = {CrawlerConsts.URL_QUEUE_NAME})
    @RabbitHandler
    public void consumePositionUrl(@Payload String message, @Headers Map<String, Object> headers, Channel channel) throws Exception {
        UrlDTO urlDTO = JsonUtils.fromJson(message, UrlDTO.class);
        log.info("消费职位链接{}", urlDTO.toSimpleString());
        if (dao.isExists(urlToKeyFunc.apply(urlDTO.getUrl()))) {
            log.info("职位{}已存在。", urlDTO.getUrl());
        } else {
            // 处理消息
            parseDetailRunner.parseDetail(urlDTO);
        }
        // 手动签收消息
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag, false);
    }

    @RabbitListener(queues = {CrawlerConsts.DETAIL_QUEUE_NAME})
    @RabbitHandler
    public void consumePositionDetail(@Payload String message, @Headers Map<String, Object> headers, Channel channel) throws Exception {
        PositionInfo positionInfo = JsonUtils.fromJson(message, PositionInfo.class);
        log.info("消费职位详情{}", positionInfo.toSimpleString());
        // 处理消息
        try {
            dao.insertDb(positionInfo);
        } catch (Throwable t) {
            log.error("消费职位详情异常，detail={}，t={}", positionInfo, ThrowableUtils.getStackTrace(t));
        }
        // 手动签收消息
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag, false);
    }
}