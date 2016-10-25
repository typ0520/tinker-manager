CREATE TABLE `t_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `parent_id` int(11) DEFAULT NULL COMMENT '父账号id',
  `username` varchar(100) NOT NULL COMMENT '用户名',
  `mobile` varchar(100) DEFAULT NULL COMMENT '手机号',
  `email` varchar(200) DEFAULT NULL COMMENT '邮箱',
  `password` varchar(500) NOT NULL COMMENT '登录密码',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `last_login_ip` varchar(100) DEFAULT NULL COMMENT '最后一次登录ip',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后一次登录时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注字段',
  PRIMARY KEY (`id`),
  UNIQUE KEY `MOBILE` (`mobile`)
  UNIQUE KEY `MOBILE` (`email`)
  UNIQUE KEY `MOBILE` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
