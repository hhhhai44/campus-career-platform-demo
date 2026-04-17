-- 举报模块数据库结构

CREATE TABLE IF NOT EXISTS `report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `biz_type` VARCHAR(32) NOT NULL COMMENT '举报对象类型：POST/RESOURCE/POST_COMMENT/RESOURCE_COMMENT',
  `biz_id` BIGINT NOT NULL COMMENT '举报对象ID',
  `biz_title` VARCHAR(255) DEFAULT NULL COMMENT '举报对象标题快照',
  `biz_snippet` VARCHAR(255) DEFAULT NULL COMMENT '举报内容摘要快照',
  `biz_owner_id` BIGINT NOT NULL COMMENT '被举报对象所属用户ID',
  `reporter_id` BIGINT NOT NULL COMMENT '举报人用户ID',
  `reason` VARCHAR(128) NOT NULL COMMENT '举报原因',
  `detail` VARCHAR(500) DEFAULT NULL COMMENT '举报补充说明',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待处理 1-已处理 2-已驳回',
  `handler_id` BIGINT DEFAULT NULL COMMENT '处理人用户ID',
  `handle_remark` VARCHAR(255) DEFAULT NULL COMMENT '处理备注',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_report_status_time` (`status`, `create_time`),
  KEY `idx_report_biz` (`biz_type`, `biz_id`),
  KEY `idx_report_reporter` (`reporter_id`),
  KEY `idx_report_owner` (`biz_owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容举报表';

