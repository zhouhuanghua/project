package cn.zhh.common.dto.mq;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 搜索职位信息消息
 *
 * @author Zhou Huanghua
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchPositionInfoMsg extends BaseMqMessage {

    /** 内容 */
    private String content;

    /** 城市 */
    private String city;

    /** 工作经验 */
    private String workExp;

    /** 学历 */
    private String education;

    /** 公司规模 */
    private String companyScale;

    @Override
    public String messageIdPrefix() {
        return "SPIM";
    }
}
