/*用户表*/
CREATE TABLE `t_user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `parent_id` int DEFAULT NULL COMMENT '父账号id',
  `username` varchar(32) NOT NULL COMMENT '用户名',
  `mobile` varchar(16) DEFAULT NULL COMMENT '手机号',
  `email` varchar(32) DEFAULT NULL COMMENT '邮箱',
  `password` varchar(32) NOT NULL COMMENT '登录密码',
  `avatar` varchar(256) DEFAULT NULL COMMENT '头像',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `MOBILE` (`mobile`),
  UNIQUE KEY `EMAIL` (`email`),
  UNIQUE KEY `USERNAME` (`username`),
  FOREIGN KEY(parent_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*应用信息表*/
CREATE TABLE `t_app_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int COMMENT '用户id',
  `appname` varchar(32) NOT NULL COMMENT '应用名字',
  `platform` varchar(32) NOT NULL COMMENT '平台',
  `uid` varchar(64) DEFAULT NULL COMMENT '应用的唯一标示',
  `description` varchar(256) NOT NULL COMMENT '应用描述',
  `secret` varchar(256) NOT NULL COMMENT '应用秘钥',
  `public_key` varchar(256) DEFAULT NULL COMMENT '公钥',
  `private_key` varchar(256) DEFAULT NULL COMMENT '私钥',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UID` (`uid`),
  FOREIGN KEY(user_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*版本信息表*/
CREATE TABLE `t_version_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int COMMENT '用户id',
  `app_uid` varchar(64) NOT NULL COMMENT '应用id',
  `version_name` varchar(32) NOT NULL COMMENT '版本名字',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  FOREIGN KEY(user_id) REFERENCES t_user(id),
  FOREIGN KEY(app_uid) REFERENCES t_app_info(uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*版本信息表*/
CREATE TABLE `t_patch_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int COMMENT '用户id',
  `app_uid` varchar(64) NOT NULL COMMENT '应用id',
  `version_name` varchar(32) NOT NULL COMMENT '版本名字',
  `patch_version` int COMMENT '补丁版本',
  `publish_version` int COMMENT '发布版本',
  `status` int COMMENT '0 未发布 1 已发布',
  `publish_type` int COMMENT '0 灰度发布 1 正常发布',
  `patch_size` long COMMENT '补丁大小',
  `file_hash` varchar(64) NOT NULL COMMENT '文件的hash值',
  `description` varchar(256) NOT NULL COMMENT '补丁描述',
  `tags` varchar(256) DEFAULT NULL COMMENT '灰度发布的tag用，分割',
  `storage_path` varchar(256) NOT NULL COMMENT '存储路径',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  FOREIGN KEY(user_id) REFERENCES t_user(id),
  FOREIGN KEY(app_uid) REFERENCES t_app_info(uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
