package cn.zhh.admin.dto.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 分页请求
 *
 * @author Zhou Huanghua
 */
@Data
public class PageReq implements ValidateReq, Serializable {

    @ApiModelProperty(name = "页码", example = "1", required = true)
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页面不能小于1")
    private Integer pageNum;

    @ApiModelProperty(name = "每页数量", example = "10", required = true)
    @NotNull(message = "每页数量不能为空")
    @Min(value = 1, message = "每页数量不能小于1")
    private Integer pageSize;
}
