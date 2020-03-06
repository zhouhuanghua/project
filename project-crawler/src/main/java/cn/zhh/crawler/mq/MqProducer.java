package cn.zhh.crawler.mq;

import cn.zhh.common.dto.PositionInfo;
import cn.zhh.common.util.JsonUtils;
import cn.zhh.crawler.constant.CrawlerConsts;
import cn.zhh.crawler.dto.UrlDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * MQ生产者
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Component
public class MqProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendUrl(UrlDTO urlDTO) {
        log.info("发送职位链接至MQ，url=[{}]", urlDTO.getUrl());
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(CrawlerConsts.MQ_EXCHANGE_NAME, CrawlerConsts.URL_ROUTING_KEY, JsonUtils.toJson(urlDTO), correlationData);
    }

    public void sendDetail(PositionInfo positionInfo) {
        log.info("发送职位详情至MQ，positionDetail=[{}]", positionInfo.getCompanyName() + "-" + positionInfo.getName());
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(positionInfo.getUniqueKey());
        rabbitTemplate.convertAndSend(CrawlerConsts.MQ_EXCHANGE_NAME, CrawlerConsts.DETAIL_ROUTING_KEY, JsonUtils.toJson(positionInfo), correlationData);
    }
}