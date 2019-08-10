package cn.zhh.common.dto.mq;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 爬虫系统收集的职位数据
 *
 * @author Zhou Huanghua
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PositionInfoMsg extends BaseMqMessage implements Serializable {

    private String uniqueKey;

    private String name;

    private Byte source;

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

    @Override
    public String messageIdPrefix() {
        return "PIM";
    }
}
