package cn.zhh.common.constant;

import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

/**
 * 城市枚举
 *
 * @author Zhou Huanghua
 */
public enum CityEnum {

    ALL((byte) 0, "全国"),
    BEIJING((byte) 1, "北京"),
    SHANGHAI((byte) 2, "上海"),
    GUANGZHOU((byte) 3, "广州"),
    SHENZHEN((byte) 4, "深圳"),
    HANGZHOU((byte) 5, "杭州"),
    CHENGDU((byte) 6, "成都");


    @Getter
    private Byte code;

    @Getter
    private String desc;

    CityEnum(Byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Optional<String> code2Desc(Byte code) {
        for (CityEnum cityEnum : CityEnum.values()) {
            if (Objects.equals(cityEnum.getCode(), code)) {
                return Optional.of(cityEnum.getDesc());
            }
        }
        return Optional.empty();
    }

    public static Optional<CityEnum> getByDesc(String desc) {
        for (CityEnum cityEnum : CityEnum.values()) {
            if (Objects.equals(cityEnum.getDesc(), desc)) {
                return Optional.of(cityEnum);
            }
        }
        return Optional.empty();
    }
}
