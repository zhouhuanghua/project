package cn.zhh.common.dto.mq;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 校招-实习消息
 *
 * @author Zhou Huanghua
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SchoolInternshipMsg extends BaseMqMessage implements Serializable {

    /**
     * 公司名称
     */
    private String name;

    /** 唯一标识 */
    private String uniqueKey;

    /** 链接 */
    private String url;

    /** logo链接 */
    private String logoUrl;

    /** 工作地点 */
    private String workPlace;

    /** 岗位行业 */
    private String industry;

    /** 岗位数量 */
    private Integer jobNum;

    /** 有效期限 */
    private String expiryDate;

    /** 介绍 */
    private String introduce;

    /** 标签 */
    private String label;

    /** 招聘类型 */
    private String hireType;

    /** 岗位信息 */
    private List<JobMsg> jobInfo;

    @Override
    public String messageIdPrefix() {
        return "SIM";
    }

    @Data
    public static class JobMsg {
        /** 岗位链接 */
        private String jobLink;
        /** 岗位名称 */
        private String jobName;
        /** 岗位唯一标识*/
        private String jobUniqueKey;
        /** 岗位工作地点 */
        private String jobWorkPlace;
        /**
         * 岗位类别
         */
        private String jobType;
        /** 岗位有效期限 */
        private String jobDeadline;
        /** 岗位介绍 */
        private String jobIntroduce;
        /** 岗位描述 */
        private String jobDescription;
    }
}
