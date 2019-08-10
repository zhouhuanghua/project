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

    /** 工作经验 */
    private Byte workExp;

    /** 学历 */
    private Byte education;

    /** 公司发展阶段 */
    private Byte developmentStage;

    @Override
    public String messageIdPrefix() {
        return "SPIM";
    }
}
