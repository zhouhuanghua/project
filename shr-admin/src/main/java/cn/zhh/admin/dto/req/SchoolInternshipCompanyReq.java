package cn.zhh.admin.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 校招实习公司请求对象
 *
 * @author Zhou Huanghua
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel
public class SchoolInternshipCompanyReq extends PageReq {
    @ApiModelProperty(name = "招聘类型", example = "校招")
    private String hireType;
}
