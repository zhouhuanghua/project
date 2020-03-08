package cn.zhh.portal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 职位搜索响应详细参数对象
 *
 * @author Zhou Huanghua
 */
@Data
public class PositionSearchDetailRsp extends PositionSearchVO {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone= "GMT+8")
    private Date publishTime;
}
