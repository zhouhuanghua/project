package cn.zhh.common.dto.mq;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TODO
 *
 * @author Zhou Huanghua
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProxyAddressMsg extends BaseMqMessage {

    private String ip;

    private String port;

    private String type;

    @Override
    public String messageIdPrefix() {
        return "PAM";
    }
}
