package cn.zhh.admin.dto.rsp;

import lombok.Data;

/**
 * 校招实习岗位列表响应对象
 *
 * @author Zhou Huanghua
 */
@Data
public class SchoolInternshipJobListRsp {
    private Long id;
    private String name;
    private String workPlace;
}