package cn.zhh.common.enums;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 是否删除枚举
 *
 * @author Zhou Huanghua
 */
@Slf4j
public enum IsDeletedEnum {

    YES((byte) 1, "已删除"),
    NO((byte) 0, "未删除");

    @Getter
    private Byte code;

    @Getter
    private String description;

    private IsDeletedEnum(Byte code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String code2desc(Byte code) {
        for (IsDeletedEnum isDeletedEnum : IsDeletedEnum.values()) {
            if (Objects.equals(isDeletedEnum.getCode(), code)) {
                return isDeletedEnum.getDescription();
            }
        }
        return null;
    }
}
