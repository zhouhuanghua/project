package cn.zhh.common.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 公司发展阶段枚举
 *
 * @author Zhou Huanghua
 */
public enum DevelopmentStageEnum {

    ALL((byte)0, "不限"),
    NOT_NEED((byte)1, "不需要融资"),
    NOT((byte)2, "未融资"),
    ANGEL((byte)3, "天使轮"),
    A((byte)4, "A轮"),
    B((byte)5, "B轮"),
    C((byte)6, "C轮"),
    D((byte)7, "D轮及以上"),
    LISTED((byte)8, "已上市");

    @Getter
    private Byte code;

    @Getter
    private String description;

    private DevelopmentStageEnum(Byte code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String code2desc(Byte code) {
        for (DevelopmentStageEnum developmentStageEnum : DevelopmentStageEnum.values()) {
            if (Objects.equals(developmentStageEnum.getCode(), code)) {
                return developmentStageEnum.getDescription();
            }
        }
        return null;
    }

    public static Byte desc2code(String desc) {
        for (DevelopmentStageEnum developmentStageEnum : DevelopmentStageEnum.values()) {
            if (Objects.equals(developmentStageEnum.getDescription(), desc)) {
                return developmentStageEnum.getCode();
            }
        }
        return null;
    }
}
