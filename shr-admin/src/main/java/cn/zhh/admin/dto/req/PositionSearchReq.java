package cn.zhh.admin.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 职位搜索请求对象
 *
 * @author Zhou Huanghua
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel
public class PositionSearchReq extends PageReq {
    @ApiModelProperty(name = "内容", example = "Java")
    private String content;

    @ApiModelProperty(name = "城市", example = "广州")
    private String city;

    @ApiModelProperty(name = "工作经验", example = "[1,2]")
    private List<Byte> workExpList;

    @ApiModelProperty(name = "学历", example = "[2,3]")
    private List<Byte> educationList;

    @ApiModelProperty(name = "公司发展阶段", example = "[3,4]")
    private List<Byte> companyDevelopmentStageList;
}
