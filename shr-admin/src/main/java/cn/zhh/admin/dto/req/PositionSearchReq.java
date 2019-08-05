package cn.zhh.admin.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 职位搜索请求对象
 *
 * @author Zhou Huanghua
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel
public class PositionSearchReq extends PageReq {
    @ApiModelProperty(name = "内容", example = "Java", required = true)
    private String content;

    @ApiModelProperty(name = "城市", example = "广州")
    private String city;

    @ApiModelProperty(name = "工作经验", example = "3-5年")
    private String workExp;

    @ApiModelProperty(name = "学历", example = "本科")
    private String education;

    @ApiModelProperty(name = "公司规模", example = "20-99人")
    private String companyScale;
}
