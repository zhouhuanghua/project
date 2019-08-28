CREATE TABLE `shr_school_internship_company` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(128) NOT NULL DEFAULT '' COMMENT '名称',
  `unique_key` varchar(16) NOT NULL DEFAULT '' COMMENT '唯一标识',
  `url` varchar(64) NOT NULL DEFAULT '' COMMENT '链接',
  `logo_url` varchar(128) NOT NULL DEFAULT '' COMMENT 'logo链接',
  `work_place` varchar(128) NOT NULL DEFAULT '' COMMENT '工作地点',
  `industry` varchar(128) NOT NULL DEFAULT '' COMMENT '职位行业',
  `job_num` smallint(4) NOT NULL DEFAULT '0' COMMENT '岗位数量',
  `expiry_date` varchar(32) NOT NULL DEFAULT '' COMMENT '有效期限',
  `introduce` varchar(8000) NOT NULL DEFAULT '' COMMENT '介绍',
  `label` varchar(256) NOT NULL DEFAULT '' COMMENT '标签',
  `hire_type` varchar(32) NOT NULL DEFAULT '' COMMENT '招聘类型',
  `job_types` varchar(256) NOT NULL DEFAULT '[]' COMMENT '岗位类别',
  `creator` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_user` varchar(64) DEFAULT '' COMMENT '最后更新人',
  `last_update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已删除：1-是，0否',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_unique_key` (`unique_key`,`is_deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COMMENT='校招-实习-公司表';