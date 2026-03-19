-- 分类数据迁移脚本

UPDATE `post_category` SET `status` = 1 WHERE `status` IS NULL;
UPDATE `resource_category` SET `status` = 1 WHERE `status` IS NULL;

INSERT INTO `post_category` (`name`, `code`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT '考研', 'KAOYAN', 1, 1, 0, NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `post_category`)
UNION ALL
SELECT '保研', 'BAOYAN', 2, 1, 0, NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `post_category`)
UNION ALL
SELECT '就业', 'JOB', 3, 1, 0, NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `post_category`)
UNION ALL
SELECT '考公', 'GONGWUYUAN', 4, 1, 0, NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `post_category`);

INSERT INTO `resource_category` (`name`, `code`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT '考研', 'KAOYAN', 1, 1, 0, NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `resource_category`)
UNION ALL
SELECT '保研', 'BAOYAN', 2, 1, 0, NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `resource_category`)
UNION ALL
SELECT '就业', 'JOB', 3, 1, 0, NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `resource_category`)
UNION ALL
SELECT '考公', 'GONGWUYUAN', 4, 1, 0, NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `resource_category`);
