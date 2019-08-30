package cn.zhh.common.dto.mq;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 技巧
 *
 * @author Zhou Huanghua
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SkillMsg extends BaseMqMessage implements Serializable {
    /**
     * 标题
     */
    private String title;
    /**
     * 类型
     */
    private String type;
    /**
     * 摘要
     */
    private String summary;
    /**
     * 正文
     */
    private String text;

    @Override
    public String messageIdPrefix() {
        return "SKILL";
    }
}
