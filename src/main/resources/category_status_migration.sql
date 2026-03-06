-- 分类表增加 status/code（若字段已存在请注释掉对应 ALTER 再执行）
-- 帖子分类表：增加 code、status
ALTER TABLE `post_category`
  ADD COLUMN `code` VARCHAR(32) DEFAULT NULL COMMENT '分类编码' AFTER `name`,
  ADD COLUMN `status` INT DEFAULT 1 COMMENT '状态：0-禁用 1-启用' AFTER `sort`;

-- 资源分类表：增加 status
ALTER TABLE `resource_category`
  ADD COLUMN `status` INT DEFAULT 1 COMMENT '状态：0-禁用 1-启用' AFTER `sort`;

-- 为已有 post_category 行设置 status=1（若未设置）
UPDATE `post_category` SET `status` = 1 WHERE `status` IS NULL;

-- 为已有 resource_category 行设置 status=1（若未设置）
UPDATE `resource_category` SET `status` = 1 WHERE `status` IS NULL;

-- 若帖子分类表为空，可插入初始数据：
INSERT INTO `post_category` (`name`, `code`, `sort`, `status`, `deleted`, `create_time`, `update_time`) VALUES
('考研', 'KAOYAN', 1, 1, 0, NOW(), NOW()),
('保研', 'BAOYAN', 2, 1, 0, NOW(), NOW()),
('就业', 'JOB', 3, 1, 0, NOW(), NOW()),
('考公', 'GONGWUYUAN', 4, 1, 0, NOW(), NOW());

-- 若资源分类表为空，可插入初始数据：
INSERT INTO `resource_category` (`name`, `code`, `sort`, `status`, `deleted`, `create_time`, `update_time`) VALUES
('考研', 'KAOYAN', 1, 1, 0, NOW(), NOW()),
('保研', 'BAOYAN', 2, 1, 0, NOW(), NOW()),
('就业', 'JOB', 3, 1, 0, NOW(), NOW()),
('考公', 'GONGWUYUAN', 4, 1, 0, NOW(), NOW());
