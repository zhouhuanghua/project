package cn.zhh.common.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 职位来源枚举
 *
 * @author Zhou Huanghua
 */
public enum PositionSourceEnum {

    ZHILIAN((byte)1, "智联"),
    LAGOU((byte)2, "拉勾"),
    BOSS((byte)3, "boss直聘"),
    UNKNOW((byte)0, "未知");

    @Getter
    private Byte code;

    @Getter
    private String description;

    private PositionSourceEnum(Byte code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String code2desc(Byte code) {
        for (PositionSourceEnum positionSourceEnum : PositionSourceEnum.values()) {
            if (Objects.equals(positionSourceEnum.getCode(), code)) {
                return positionSourceEnum.getDescription();
            }
        }
        return null;
    }
}
