-- 分类表字段（code/status）已并入 forum_schema.sql 与 resource_schema.sql 的 CREATE TABLE
-- 该脚本仅用于兼容旧库数据修复与初始化

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

