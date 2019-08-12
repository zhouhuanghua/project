package cn.zhh.common.dto.mq;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 搜索职位信息消息
 *
 * @author Zhou Huanghua
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchPositionInfoMsg extends BaseMqMessage implements Serializable {

    /** 内容 */
    private String content;

    /** 城市 */
    private Byte city;

    @Override
    public String messageIdPrefix() {
        return "SPIM";
    }
}
