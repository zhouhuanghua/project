package cn.zhh.common.dto;

import lombok.Data;

import java.util.Date;

/**
 * 职位数据
 *
 * @author Zhou Huanghua
 */
@Data
public class PositionInfo {

    private String uniqueKey;

    private String name;

    private String salary;

    private String city;

    private String workExp;

    private String education;

    private String welfare;

    private String description;

    private String label;

    private String workAddress;

    private Date publishTime;

    private String url;

    private String companyName;

    private String companyLogo;

    private String companyDevelopmentalStage;

    private String companyScale;

    private String companyDomain;

    private String companyUrl;

    private String companyIntroduction;

    public String toSimpleString() {
        return uniqueKey + "【" + name + "#" + companyName + "】";
    }
}