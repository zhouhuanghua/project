package cn.zhh.common.dto.mq;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询公司评论消息
 *
 * @author Zhou Huanghua
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryCompanyCommentMsg extends BaseMqMessage {

    /** 公司名称 */
    private String name;

    @Override
    public String messageIdPrefix() {
        return "QCCM";
    }
}
