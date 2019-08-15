package cn.zhh.common.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 工作经验枚举
 *
 * @author Zhou Huanghua
 */
public enum WorkExpEnum {

    ALL((byte) 0, "不限"),
    NONE((byte) 1, "无经验"),
    LESS1((byte) 2, "1年以下"),
    ONE2THREE((byte) 3, "1-3年"),
    THREE2FIVE((byte) 4, "3-5年"),
    FIVE2TEN((byte) 5, "5-10年"),
    MORE10((byte) 6, "10年以上"),
    NOT_REQUIRED((byte) 7, "不要求");


    @Getter
    private Byte code;

    @Getter
    private String description;

    private WorkExpEnum(Byte code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String code2desc(Byte code) {
        for (WorkExpEnum workExpEnum : WorkExpEnum.values()) {
            if (Objects.equals(workExpEnum.getCode(), code)) {
                return workExpEnum.getDescription();
            }
        }
        return null;
    }

    public static Byte desc2code(String desc) {
        for (WorkExpEnum workExpEnum : WorkExpEnum.values()) {
            if (Objects.equals(workExpEnum.getDescription(), desc)) {
                return workExpEnum.getCode();
            }
        }
        return null;
    }
}
