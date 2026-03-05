-- 论坛模块建表脚本
-- 执行前请根据需要调整库名、字符集等配置

-- =========================
-- 1. 帖子表 post
-- =========================
CREATE TABLE IF NOT EXISTS `post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(255) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容（富文本或Markdown）',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `author_id` BIGINT NOT NULL COMMENT '作者用户ID',
  `like_count` INT DEFAULT 0 COMMENT '点赞数量（冗余字段）',
  `view_count` INT DEFAULT 0 COMMENT '浏览数量',
  `comment_count` INT DEFAULT 0 COMMENT '评论数量',
  `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-已屏蔽',
  `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_category` (`category_id`),
  KEY `idx_post_author` (`author_id`),
  KEY `idx_post_create_time` (`create_time`),
  KEY `idx_post_status_deleted` (`status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子表';


-- =========================
-- 2. 帖子分类表 post_category
-- =========================
CREATE TABLE IF NOT EXISTS `post_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
  `sort` INT DEFAULT 0 COMMENT '排序（越小越靠前）',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子分类表';


-- =========================
-- 3. 帖子点赞表 post_like
-- =========================
CREATE TABLE IF NOT EXISTS `post_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  KEY `idx_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞记录表';


-- =========================
-- 4. 帖子收藏表 post_favorite
-- =========================
CREATE TABLE IF NOT EXISTS `post_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_fav_user` (`post_id`, `user_id`),
  KEY `idx_fav_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏记录表';


-- =========================
-- 5. 帖子评论表 post_comment（支持二级评论）
-- =========================
CREATE TABLE IF NOT EXISTS `post_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `root_id` BIGINT DEFAULT NULL COMMENT '根评论ID（一级评论为自身ID，二级评论指向所属一级评论ID）',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID（回复哪个评论）',
  `from_user_id` BIGINT NOT NULL COMMENT '评论用户ID',
  `to_user_id` BIGINT DEFAULT NULL COMMENT '被回复用户ID',
  `content` VARCHAR(1000) NOT NULL COMMENT '评论内容',
  `like_count` INT DEFAULT 0 COMMENT '点赞数量',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-已删除/屏蔽',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_comment_post` (`post_id`),
  KEY `idx_comment_root` (`root_id`),
  KEY `idx_comment_parent` (`parent_id`),
  KEY `idx_comment_from_user` (`from_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子评论表';



