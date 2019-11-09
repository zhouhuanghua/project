package cn.zhh.crawler.recruit.society;

import cn.zhh.common.dto.mq.BaseMqMessage;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TODO
 *
 * @author Zhou Huanghua
 * @date 2019/11/9 16:20
 */
@Data
@AllArgsConstructor
public class PositionUrlMsg extends BaseMqMessage {

    private Byte website;

    private String detailUrl;

    @Override
    public String messageIdPrefix() {
        return "PUM";
    }
}
