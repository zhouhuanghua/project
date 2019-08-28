package cn.zhh.admin.dto.rsp;

import lombok.Data;

/**
 * 校招实习公司响应对象
 *
 * @author Zhou Huanghua
 */
@Data
public class SchoolInternshipCompanyRsp {
    private Long id;
    private String name;
    private String url;
    private String logoUrl;
    private String workPlace;
    private String industry;
    private Integer jobNum;
    private String expiryDate;
    private String introduce;
    private String label;
    private String hireType;
    private String jobTypes;
}