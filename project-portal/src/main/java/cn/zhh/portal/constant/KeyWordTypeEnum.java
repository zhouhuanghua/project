package cn.zhh.portal.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

/**
 * 关键字类型
 *
 * @author Zhou Huanghua
 */
@AllArgsConstructor
public enum KeyWordTypeEnum {

    POSITION(1),

    COMPANY(2),

    DESCRIPTION(3);

    @Getter
    private int code;

    public static Optional<KeyWordTypeEnum> getByCode(Integer code) {
        if (Objects.nonNull(code)) {
            for (KeyWordTypeEnum typeEnum : KeyWordTypeEnum.values()) {
                if (Objects.equals(code, typeEnum.code)) {
                    return Optional.of(typeEnum);
                }
            }
        }
        return Optional.empty();
    }
}