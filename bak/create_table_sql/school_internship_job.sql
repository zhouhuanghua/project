CREATE TABLE `shr_school_internship_job` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `company_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '公司id',
  `name` varchar(128) NOT NULL DEFAULT '' COMMENT '名称',
  `unique_key` varchar(32) NOT NULL DEFAULT '' COMMENT '唯一标识',
  `url` varchar(128) NOT NULL DEFAULT '' COMMENT '链接',
  `work_place` varchar(128) NOT NULL DEFAULT '' COMMENT '工作地点',
  `deadline` varchar(64) NOT NULL DEFAULT '' COMMENT '截至日期',
  `type` varchar(32) NOT NULL DEFAULT '' COMMENT '类别',
  `introduce` varchar(3000) NOT NULL DEFAULT '' COMMENT '介绍',
  `description` varchar(3000) NOT NULL DEFAULT '' COMMENT '描述',
  `creator` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_user` varchar(64) DEFAULT '' COMMENT '最后更新人',
  `last_update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已删除：1-是，0否',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_unique_key` (`unique_key`,`is_deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=878 DEFAULT CHARSET=utf8mb4 COMMENT='校招-实习-岗位表';