package cn.zhh.admin.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 校招实习岗位列表请求对象
 *
 * @author Zhou Huanghua
 */
@Data
@ApiModel
public class SchoolInternshipJobListReq implements ValidateReq {
    @ApiModelProperty(name = "岗位类别", example = "技术")
    @NotBlank(message = "岗位类别不能为空！")
    private String jobType;

    @ApiModelProperty(name = "公司id", example = "1", required = true)
    @NotNull(message = "公司id不能为空！")
    private Long companyId;

}
