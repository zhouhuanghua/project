package cn.zhh.admin.dto.rsp;

import lombok.Data;

/**
 * 校招实习岗位列表响应对象
 *
 * @author Zhou Huanghua
 */
@Data
public class SchoolInternshipJobDetailRsp {
    private Long id;
    private String name;
    private String url;
    private String type;
    private String workPlace;
    private String deadline;
    private String introduce;
    private String description;
}