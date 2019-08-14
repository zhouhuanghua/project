package cn.zhh.admin.dto.rsp;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 职位详情响应对象
 *
 * @author Zhou Huanghua
 */
@Data
public class PositionDetailRsp {

    private PositionVO position;

    private CompanyVO company;

    @Data
    public static class PositionVO {
        private String name;
        private Byte source;
        private String salary;
        private String city;
        private String workExp;
        private String education;
        private String welfare;
        private String description;
        private String label;
        private String workAddress;
        private Date publishTime;
        private String url;
    }

    @Data
    public static class CompanyVO {
        private String name;
        private String logo;
        private String developmentalStage;
        private String scale;
        private String domain;
        private String url;
        private String introduction;
        private List<CompanyComment> commentList;

        @Data
        public static class CompanyComment {
            private String source;
            private String content;
        }
    }
}
