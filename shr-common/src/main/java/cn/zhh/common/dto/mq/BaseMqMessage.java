package cn.zhh.common.dto.mq;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MQ消息基类
 *
 * @author Zhou Huanghua
 */
public abstract class BaseMqMessage {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    protected BaseMqMessage() {
        this.setMessageId();
    }

    private String messageId;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId() {
        this.messageId = messageIdPrefix() + LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    /**
     * 消息ID前缀
     *
     * @return messageIdPrefix
     */
    public abstract String messageIdPrefix();
}
