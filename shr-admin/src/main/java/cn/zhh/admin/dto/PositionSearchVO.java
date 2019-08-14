package cn.zhh.admin.dto;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Id;
import java.util.Date;

/**
 * 职位搜索VO
 *
 * @author Zhou Huanghua
 */
@Data
@Document(indexName="shr", type="PositionSearchVO")
public class PositionSearchVO {

    @Id
    private Long id;

    private String uniqueKey;

    @Field(type = FieldType.Text)
    private String name;

    private Byte source;

    private String salary;

    private String city;

    private String workExp;

    private String education;

    private String welfare;

    private String label;

    private Date publishTime;

    @Field(type = FieldType.Text)
    private String companyName;

    private String companyLogo;

    private String companyDevelopmentStage;
}
