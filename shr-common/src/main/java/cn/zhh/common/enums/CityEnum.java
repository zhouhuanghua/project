package cn.zhh.common.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 职位城市枚举
 *
 * @author Zhou Huanghua
 */
public enum CityEnum {

    ALL((byte)0, "全国"),
    BEIJING((byte)1, "北京"),
    SHANGHAI((byte)2, "上海"),
    GUANGZHOU((byte)3, "广州"),
    SHENZHEN((byte)4, "深圳"),
    HANGZHOU((byte)5, "杭州"),
    CHENGDU((byte)6, "成都");


    @Getter
    private Byte code;

    @Getter
    private String description;

    private CityEnum(Byte code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String code2desc(Byte code) {
        for (CityEnum cityEnum : CityEnum.values()) {
            if (Objects.equals(cityEnum.getCode(), code)) {
                return cityEnum.getDescription();
            }
        }
        return null;
    }
}
