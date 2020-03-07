package cn.zhh.portal.dto;

import lombok.Data;

/**
 * 职位搜索请求参数对象
 *
 * @author Zhou Huanghua
 */
@Data
public class PositionSearchReq extends PageReq {

    private Integer kwType;

    private String content;

    private Integer cityCode;
}
