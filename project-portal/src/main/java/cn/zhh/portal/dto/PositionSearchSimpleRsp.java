package cn.zhh.portal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 职位搜索响应简单参数对象
 *
 * @author Zhou Huanghua
 */
@Data
public class PositionSearchSimpleRsp {

    private Long id;

    private String name;

    private String salary;

    private String city;

    private String workExp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone= "GMT+8")
    private Date publishTime;

    private String companyName;

    private String companyDevelopmentalStage;
}
