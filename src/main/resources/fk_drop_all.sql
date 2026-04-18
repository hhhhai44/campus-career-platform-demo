-- Drop all foreign keys created by fk_add_all.sql.
-- Run this to return to logical-FK-only mode.

ALTER TABLE `report`
  DROP FOREIGN KEY `fk_report_handler_user`,
  DROP FOREIGN KEY `fk_report_reporter_user`,
  DROP FOREIGN KEY `fk_report_owner_user`;

ALTER TABLE `notification`
  DROP FOREIGN KEY `fk_notification_from_user`,
  DROP FOREIGN KEY `fk_notification_user`;

ALTER TABLE `private_message_session_setting`
  DROP FOREIGN KEY `fk_pm_setting_peer_user`,
  DROP FOREIGN KEY `fk_pm_setting_user`;

ALTER TABLE `private_message`
  DROP FOREIGN KEY `fk_private_message_to_user`,
  DROP FOREIGN KEY `fk_private_message_from_user`;

ALTER TABLE `resource_comment_like`
  DROP FOREIGN KEY `fk_resource_comment_like_user`,
  DROP FOREIGN KEY `fk_resource_comment_like_comment`;

ALTER TABLE `resource_favorite`
  DROP FOREIGN KEY `fk_resource_favorite_user`,
  DROP FOREIGN KEY `fk_resource_favorite_resource`;

ALTER TABLE `resource_like`
  DROP FOREIGN KEY `fk_resource_like_user`,
  DROP FOREIGN KEY `fk_resource_like_resource`;

ALTER TABLE `resource_comment`
  DROP FOREIGN KEY `fk_resource_comment_to_user`,
  DROP FOREIGN KEY `fk_resource_comment_from_user`,
  DROP FOREIGN KEY `fk_resource_comment_parent`,
  DROP FOREIGN KEY `fk_resource_comment_root`,
  DROP FOREIGN KEY `fk_resource_comment_resource`;

ALTER TABLE `resource_rating`
  DROP FOREIGN KEY `fk_resource_rating_user`,
  DROP FOREIGN KEY `fk_resource_rating_resource`;

ALTER TABLE `resource`
  DROP FOREIGN KEY `fk_resource_uploader`,
  DROP FOREIGN KEY `fk_resource_category`;

ALTER TABLE `post_comment_like`
  DROP FOREIGN KEY `fk_post_comment_like_user`,
  DROP FOREIGN KEY `fk_post_comment_like_comment`;

ALTER TABLE `post_comment`
  DROP FOREIGN KEY `fk_post_comment_to_user`,
  DROP FOREIGN KEY `fk_post_comment_from_user`,
  DROP FOREIGN KEY `fk_post_comment_parent`,
  DROP FOREIGN KEY `fk_post_comment_root`,
  DROP FOREIGN KEY `fk_post_comment_post`;

ALTER TABLE `post_favorite`
  DROP FOREIGN KEY `fk_post_favorite_user`,
  DROP FOREIGN KEY `fk_post_favorite_post`;

ALTER TABLE `post_like`
  DROP FOREIGN KEY `fk_post_like_user`,
  DROP FOREIGN KEY `fk_post_like_post`;

ALTER TABLE `post`
  DROP FOREIGN KEY `fk_post_author`,
  DROP FOREIGN KEY `fk_post_category`;

