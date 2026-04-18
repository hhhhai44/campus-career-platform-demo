-- Add physical foreign keys for all clear table dependencies.
-- NOTE: In production, keep using logical FKs in business logic; this script is for ER modeling/validation.
-- NOTE: Fields like notification.biz_id and report.biz_id are polymorphic and are intentionally NOT constrained.

ALTER TABLE `post`
  ADD CONSTRAINT `fk_post_category`
    FOREIGN KEY (`category_id`) REFERENCES `post_category` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_post_author`
    FOREIGN KEY (`author_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE `post_like`
  ADD CONSTRAINT `fk_post_like_post`
    FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_post_like_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE `post_favorite`
  ADD CONSTRAINT `fk_post_favorite_post`
    FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_post_favorite_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE `post_comment`
  ADD CONSTRAINT `fk_post_comment_post`
    FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_post_comment_root`
    FOREIGN KEY (`root_id`) REFERENCES `post_comment` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_post_comment_parent`
    FOREIGN KEY (`parent_id`) REFERENCES `post_comment` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_post_comment_from_user`
    FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_post_comment_to_user`
    FOREIGN KEY (`to_user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE `post_comment_like`
  ADD CONSTRAINT `fk_post_comment_like_comment`
    FOREIGN KEY (`comment_id`) REFERENCES `post_comment` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_post_comment_like_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE `resource`
  ADD CONSTRAINT `fk_resource_category`
    FOREIGN KEY (`category_id`) REFERENCES `resource_category` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_resource_uploader`
    FOREIGN KEY (`uploader_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE `resource_rating`
  ADD CONSTRAINT `fk_resource_rating_resource`
    FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_resource_rating_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE `resource_comment`
  ADD CONSTRAINT `fk_resource_comment_resource`
    FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_resource_comment_root`
    FOREIGN KEY (`root_id`) REFERENCES `resource_comment` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_resource_comment_parent`
    FOREIGN KEY (`parent_id`) REFERENCES `resource_comment` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_resource_comment_from_user`
    FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_resource_comment_to_user`
    FOREIGN KEY (`to_user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE `resource_like`
  ADD CONSTRAINT `fk_resource_like_resource`
    FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_resource_like_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE `resource_favorite`
  ADD CONSTRAINT `fk_resource_favorite_resource`
    FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_resource_favorite_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE `resource_comment_like`
  ADD CONSTRAINT `fk_resource_comment_like_comment`
    FOREIGN KEY (`comment_id`) REFERENCES `resource_comment` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_resource_comment_like_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE `private_message`
  ADD CONSTRAINT `fk_private_message_from_user`
    FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_private_message_to_user`
    FOREIGN KEY (`to_user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE `private_message_session_setting`
  ADD CONSTRAINT `fk_pm_setting_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_pm_setting_peer_user`
    FOREIGN KEY (`peer_user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE `notification`
  ADD CONSTRAINT `fk_notification_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_notification_from_user`
    FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE `report`
  ADD CONSTRAINT `fk_report_owner_user`
    FOREIGN KEY (`biz_owner_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_report_reporter_user`
    FOREIGN KEY (`reporter_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_report_handler_user`
    FOREIGN KEY (`handler_id`) REFERENCES `user` (`id`)
    ON UPDATE CASCADE ON DELETE RESTRICT;

