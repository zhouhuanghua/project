package cn.zhh.common.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 职位来源枚举
 *
 * @author Zhou Huanghua
 */
public enum EducationEnum {

    ALL((byte)0, "不限"),
    JUNIOR_COLLEGE((byte)1, "大专及以下"),
    UNDERGRADUATE((byte)2, "本科"),
    MASTER((byte)3, "硕士"),
    DOCTOR((byte)4, "博士"),
    NOT_REQUIRED((byte)5, "不要求");

    @Getter
    private Byte code;

    @Getter
    private String description;

    private EducationEnum(Byte code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String code2desc(Byte code) {
        for (EducationEnum educationEnum : EducationEnum.values()) {
            if (Objects.equals(educationEnum.getCode(), code)) {
                return educationEnum.getDescription();
            }
        }
        return null;
    }
}
