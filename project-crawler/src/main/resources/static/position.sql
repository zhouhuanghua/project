CREATE TABLE `inf_position` (
  `id`                          bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `unique_key`                  varchar(32)         NOT NULL DEFAULT '' COMMENT '网站职位唯一标识',
  `name`                        varchar(64)         NOT NULL DEFAULT '' COMMENT '名称',
  `salary`                      varchar(16)         NOT NULL DEFAULT '' COMMENT '薪水',
  `city`                        varchar(32)         NOT NULL DEFAULT '' COMMENT '城市',
  `work_exp`                    varchar(16)         NOT NULL DEFAULT '' COMMENT '工作经验',
  `education`                   varchar(16)         NOT NULL DEFAULT '' COMMENT '学历',
  `welfare`                     varchar(128)        NOT NULL DEFAULT '' COMMENT '福利',
  `description`                 varchar(5000)       NOT NULL DEFAULT '' COMMENT '描述',
  `label`                       varchar(128)        NOT NULL DEFAULT '' COMMENT '标签',
  `work_address`                varchar(256)        NOT NULL DEFAULT '' COMMENT '工作地址',
  `publish_time`                datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `url`                         varchar(256)        NOT NULL DEFAULT '' COMMENT '详情链接',
  `company_name`                VARCHAR(128)        NOT NULL DEFAULT ''
  COMMENT '公司名称',
  `company_logo`                VARCHAR(256)        NOT NULL DEFAULT ''
  COMMENT '公司图案',
  `company_developmental_stage` VARCHAR(256)        NOT NULL DEFAULT ''
  COMMENT '公司发展阶段',
  `company_scale`               VARCHAR(512)        NOT NULL DEFAULT ''
  COMMENT '公司规模',
  `company_domain`              VARCHAR(256)        NOT NULL DEFAULT ''
  COMMENT '公司主营',
  `company_url`                 VARCHAR(256)        NOT NULL DEFAULT ''
  COMMENT '公司链接',
  `company_introduction`        VARCHAR(5000)       NOT NULL DEFAULT ''
  COMMENT '公司介绍',
  `creator`                     varchar(64)         NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time`                 datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_user`            varchar(64)                  DEFAULT '' COMMENT '最后更新人',
  `last_update_time`            DATETIME                     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
  COMMENT '最后更新时间',
  `is_deleted`                  tinyint(1)          NOT NULL DEFAULT '0' COMMENT '是否已删除：1-是，0否',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_unique_key` (`unique_key`, `is_deleted`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_company_neme` (`company_name`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `idx_last_update_time` (`last_update_time`) USING BTREE
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COMMENT ='职位信息表';