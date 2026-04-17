-- 私信模块数据库结构

CREATE TABLE IF NOT EXISTS `private_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `from_user_id` BIGINT NOT NULL COMMENT '发送方用户ID',
  `to_user_id` BIGINT NOT NULL COMMENT '接收方用户ID',
  `content` VARCHAR(1000) NOT NULL COMMENT '私信内容',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `is_recalled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否撤回：0-否 1-是',
  `from_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '发送方是否删除：0-否 1-是',
  `to_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '接收方是否删除：0-否 1-是',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_pm_from_to_time` (`from_user_id`, `to_user_id`, `create_time`),
  KEY `idx_pm_to_read_time` (`to_user_id`, `is_read`, `create_time`),
  KEY `idx_pm_to_from_time` (`to_user_id`, `from_user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户私信表';

ALTER TABLE `private_message`
  ADD COLUMN IF NOT EXISTS `is_recalled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否撤回：0-否 1-是',
  ADD COLUMN IF NOT EXISTS `from_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '发送方是否删除：0-否 1-是',
  ADD COLUMN IF NOT EXISTS `to_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '接收方是否删除：0-否 1-是';

CREATE TABLE IF NOT EXISTS `private_message_session_setting` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '当前用户ID',
  `peer_user_id` BIGINT NOT NULL COMMENT '会话对端用户ID',
  `is_pinned` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
  `is_muted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否免打扰：0-否 1-是',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pm_setting_user_peer` (`user_id`, `peer_user_id`),
  KEY `idx_pm_setting_user_pin` (`user_id`, `is_pinned`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信会话设置表';



