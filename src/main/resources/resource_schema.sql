-- 资源库模块建表脚本
-- 执行前请根据需要调整库名、字符集等配置

-- =========================
-- 1. 资源表 resource
-- =========================
CREATE TABLE IF NOT EXISTS `resource` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(255) NOT NULL COMMENT '资源标题',
  `description` VARCHAR(1000) DEFAULT NULL COMMENT '资源描述',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `uploader_id` BIGINT NOT NULL COMMENT '上传用户ID',
  `file_url` VARCHAR(512) NOT NULL COMMENT '文件访问URL或存储路径',
  `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签，逗号分隔',
  `score_avg` DECIMAL(3,2) DEFAULT 0 COMMENT '平均评分',
  `score_count` INT DEFAULT 0 COMMENT '评分人数',
  `like_count` INT DEFAULT 0 COMMENT '点赞数量（预留）',
  `favorite_count` INT DEFAULT 0 COMMENT '收藏数量（预留）',
  `download_count` INT DEFAULT 0 COMMENT '下载次数',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-下架',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_resource_category` (`category_id`),
  KEY `idx_resource_title` (`title`),
  KEY `idx_resource_status_deleted` (`status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源表';


-- =========================
-- 2. 资源分类表 resource_category
-- =========================
CREATE TABLE IF NOT EXISTS `resource_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(64) NOT NULL COMMENT '分类名称，如：考研/保研/就业/考公',
  `code` VARCHAR(32) NOT NULL COMMENT '分类编码',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
  `sort` INT DEFAULT 0 COMMENT '排序（越小越靠前）',
  `status` INT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_category_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源分类表';


-- =========================
-- 3. 资源评分表 resource_rating
-- =========================
CREATE TABLE IF NOT EXISTS `resource_rating` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `resource_id` BIGINT NOT NULL COMMENT '资源ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `score` TINYINT NOT NULL COMMENT '评分分值，1-5',
  `comment` VARCHAR(1000) DEFAULT NULL COMMENT '评分评语',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_user` (`resource_id`, `user_id`),
  KEY `idx_rating_resource` (`resource_id`),
  KEY `idx_rating_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源评分表';


-- =========================
-- 4. 资源点赞表（预留） resource_like
-- =========================
CREATE TABLE IF NOT EXISTS `resource_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `resource_id` BIGINT NOT NULL COMMENT '资源ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_like_user` (`resource_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源点赞记录表';


-- =========================
-- 5. 资源收藏表（预留） resource_favorite
-- =========================
CREATE TABLE IF NOT EXISTS `resource_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `resource_id` BIGINT NOT NULL COMMENT '资源ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_fav_user` (`resource_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源收藏记录表';







