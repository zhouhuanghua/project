CREATE TABLE `shr_company` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(128) NOT NULL DEFAULT '' COMMENT '名称',
  `logo` varchar(256) NOT NULL DEFAULT '' COMMENT 'logo链接',
  `developmental_stage` varchar(128) NOT NULL DEFAULT '' COMMENT '发展阶段',
  `scale` varchar(32) NOT NULL DEFAULT '' COMMENT '规模',
  `domain` varchar(128) NOT NULL DEFAULT '' COMMENT '主营',
  `url` varchar(256) NOT NULL DEFAULT '' COMMENT '主页链接',
  `introduction` varchar(3000) NOT NULL DEFAULT '' COMMENT '简介',
  `creator` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_user` varchar(64) DEFAULT '' COMMENT '最后更新人',
  `last_update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已删除：1-是，0否',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司表';