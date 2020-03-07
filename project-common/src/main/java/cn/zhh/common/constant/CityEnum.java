package cn.zhh.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

/**
 * 城市枚举
 *
 * @author Zhou Huanghua
 */
@AllArgsConstructor
public enum CityEnum {

    ALL(0, "全国"),
    BEIJING(1, "北京"),
    SHANGHAI(2, "上海"),
    GUANGZHOU(3, "广州"),
    SHENZHEN(4, "深圳"),
    HANGZHOU(5, "杭州"),
    CHENGDU(6, "成都");


    @Getter
    private int code;

    @Getter
    private String desc;

    public static Optional<String> code2Desc(Integer code) {
        if (Objects.nonNull(code)) {
            for (CityEnum cityEnum : CityEnum.values()) {
                if (Objects.equals(cityEnum.getCode(), code)) {
                    return Optional.of(cityEnum.getDesc());
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<CityEnum> getByDesc(String desc) {
        if (Objects.nonNull(desc)) {
            for (CityEnum cityEnum : CityEnum.values()) {
                if (Objects.equals(cityEnum.getDesc(), desc)) {
                    return Optional.of(cityEnum);
                }
            }
        }
        return Optional.empty();
    }
}
