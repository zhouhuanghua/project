package cn.zhh.portal.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求
 *
 * @author Zhou Huanghua
 */
@Data
public class PageReq implements Serializable {

    private Integer pageNo;

    private Integer pageSize;
}
