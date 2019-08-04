CREATE TABLE `shr_proxy_address` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `ip` varchar(16) NOT NULL DEFAULT '' COMMENT 'IP',
  `port` int(6) unsigned NOT NULL DEFAULT '0' COMMENT '端口',
  `type` varchar(16) NOT NULL DEFAULT '' COMMENT '类型',
  `creator` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_user` varchar(64) DEFAULT '' COMMENT '最后更新人',
  `last_update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已删除：1-是，0否',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_ip_port` (`ip`,`port`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=180 DEFAULT CHARSET=utf8mb4 COMMENT='代理地址表';

