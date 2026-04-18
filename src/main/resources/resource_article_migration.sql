-- 资源模块文章化迁移脚本
-- 适用于已有 resource 表从“文件下载”模式迁移到“文章内容”模式

-- 1) 新增正文列（若不存在）
ALTER TABLE `resource`
  ADD COLUMN IF NOT EXISTS `content` LONGTEXT NULL COMMENT '文章正文' AFTER `uploader_id`;

-- 2) 为历史数据兜底填充正文：保留原描述并附加原链接信息
UPDATE `resource`
SET `content` = CONCAT(
    COALESCE(NULLIF(`description`, ''), '历史资源迁移记录'),
    '\n\n[历史下载链接] ',
    COALESCE(`file_url`, '')
)
WHERE (`content` IS NULL OR `content` = '');

-- 3) 正文列改为非空
ALTER TABLE `resource`
  MODIFY COLUMN `content` LONGTEXT NOT NULL COMMENT '文章正文';

-- 4) 清理下载模式字段（若存在）
ALTER TABLE `resource`
  DROP COLUMN IF EXISTS `file_url`,
  DROP COLUMN IF EXISTS `download_count`;

