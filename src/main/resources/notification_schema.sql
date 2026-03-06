-- 消息通知模块建表脚本
-- 执行前请根据需要调整库名、字符集等配置

-- =========================
-- 1. 消息通知表 notification
-- =========================
CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `from_user_id` BIGINT NOT NULL COMMENT '触发行为的用户ID',
  `type` TINYINT NOT NULL COMMENT '通知类型：1-评论 2-点赞 3-收藏',
  `biz_type` TINYINT NOT NULL COMMENT '业务类型：1-帖子 2-资源 3-评论',
  `biz_id` BIGINT NOT NULL COMMENT '业务ID，如帖子ID/资源ID/评论ID',
  `title` VARCHAR(255) NOT NULL COMMENT '通知标题',
  `content` VARCHAR(1000) DEFAULT NULL COMMENT '通知内容摘要',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_notification_user_time` (`user_id`, `create_time`),
  KEY `idx_notification_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户消息通知表';

