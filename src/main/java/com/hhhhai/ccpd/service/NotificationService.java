package com.hhhhai.ccpd.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.entity.forum.PostCommentEntity;
import com.hhhhai.ccpd.vo.notification.NotificationVO;
import org.springframework.stereotype.Service;

/**
 * 消息通知业务接口
 */
@Service
public interface NotificationService {

  /**
   * 创建帖子评论通知（包括对帖子本身的评论和对评论的回复）
   *
   * @param comment 新增的评论实体
   */
  void createPostCommentNotification(PostCommentEntity comment);

  /**
   * 创建帖子点赞通知
   *
   * @param postId 被点赞的帖子ID
   */
  void createPostLikeNotification(Long postId);

  /**
   * 创建帖子收藏通知
   *
   * @param postId 被收藏的帖子ID
   */
  void createPostFavoriteNotification(Long postId);

  /**
   * 分页查询当前登录用户的通知列表
   *
   * @param page 页码
   * @param size 每页大小
   * @param type 通知类型（可空）
   * @param isRead 是否已读（可空）
   * @return 分页结果
   */
  Page<NotificationVO> pageUserNotifications(Long page, Long size, Integer type, Integer isRead);

  /**
   * 将指定通知标记为已读
   *
   * @param notificationId 通知ID
   */
  void markRead(Long notificationId);

  /**
   * 将当前用户的所有通知标记为已读
   */
  void markAllRead();

  /**
   * 获取当前用户未读通知数量（使用 Redis 缓存）
   *
   * @return 未读数量
   */
  Long getUnreadCount();
}

