package cn.zhh.portal.dto;

import cn.zhh.common.dto.PositionInfo;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 职位搜索VO
 *
 * @author Zhou Huanghua
 */
@Data
@Document(indexName = "project", type = "PositionSearchVO")
public class PositionSearchVO extends PositionInfo {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String name;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String companyName;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String description;
}
