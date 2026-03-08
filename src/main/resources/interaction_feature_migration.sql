-- 互动功能补齐迁移脚本（MySQL 8+）
-- 目标：补齐评论点赞、资源点赞/收藏相关表与字段

-- 1) post_comment_like
CREATE TABLE IF NOT EXISTS `post_comment_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `comment_id` BIGINT NOT NULL COMMENT '评论ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_comment_like_user` (`comment_id`, `user_id`),
  KEY `idx_post_comment_like_user` (`user_id`, `comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子评论点赞记录表';

-- 2) resource_like
CREATE TABLE IF NOT EXISTS `resource_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `resource_id` BIGINT NOT NULL COMMENT '资源ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_like_user` (`resource_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源点赞记录表';

-- 3) resource_favorite
CREATE TABLE IF NOT EXISTS `resource_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `resource_id` BIGINT NOT NULL COMMENT '资源ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_fav_user` (`resource_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源收藏记录表';

-- 4) resource_comment_like
CREATE TABLE IF NOT EXISTS `resource_comment_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `comment_id` BIGINT NOT NULL COMMENT '评论ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_comment_like_user` (`comment_id`, `user_id`),
  KEY `idx_resource_comment_like_user` (`user_id`, `comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源评论点赞记录表';

-- 5) 补齐 resource 表计数字段（若已有则跳过）
ALTER TABLE `resource`
  ADD COLUMN IF NOT EXISTS `like_count` INT DEFAULT 0 COMMENT '点赞数量',
  ADD COLUMN IF NOT EXISTS `favorite_count` INT DEFAULT 0 COMMENT '收藏数量';

