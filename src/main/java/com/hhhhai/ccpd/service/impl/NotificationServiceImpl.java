package com.hhhhai.ccpd.service.impl;

import static com.hhhhai.ccpd.common.constant.RedisConstants.USER_NOTIFICATION_UNREAD_COUNT_KEY;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import com.hhhhai.ccpd.common.enums.NotificationBizTypeEnum;
import com.hhhhai.ccpd.common.enums.NotificationTypeEnum;
import com.hhhhai.ccpd.common.enums.ReadStatusEnum;
import com.hhhhai.ccpd.entity.forum.PostCommentEntity;
import com.hhhhai.ccpd.entity.forum.PostEntity;
import com.hhhhai.ccpd.entity.notification.NotificationEntity;
import com.hhhhai.ccpd.mapper.NotificationMapper;
import com.hhhhai.ccpd.mapper.PostCommentMapper;
import com.hhhhai.ccpd.mapper.PostMapper;
import com.hhhhai.ccpd.service.NotificationService;
import com.hhhhai.ccpd.vo.notification.NotificationVO;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 消息通知业务实现
 */
@Service
public class NotificationServiceImpl implements NotificationService {

  @Resource
  private NotificationMapper notificationMapper;

  @Resource
  private PostMapper postMapper;

  @Resource
  private PostCommentMapper postCommentMapper;

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Override
  public void createPostCommentNotification(PostCommentEntity comment) {
    if (comment == null || comment.getPostId() == null || comment.getFromUserId() == null) {
      return;
    }

    Long receiverId = resolveCommentReceiver(comment);
    if (receiverId == null || Objects.equals(receiverId, comment.getFromUserId())) {
      // 无有效接收人或自己给自己评论则不创建通知
      return;
    }

    NotificationEntity entity = new NotificationEntity();
    entity.setUserId(receiverId);
    entity.setFromUserId(comment.getFromUserId());
    entity.setType(NotificationTypeEnum.COMMENT);
    entity.setBizType(NotificationBizTypeEnum.POST);
    entity.setBizId(comment.getPostId());
    entity.setTitle("你有一条新的评论");
    entity.setContent(buildCommentContent(comment.getContent()));
    entity.setIsRead(ReadStatusEnum.UNREAD);

    notificationMapper.insert(entity);

    increaseUnreadCount(receiverId);
  }

  @Override
  public void createPostLikeNotification(Long postId) {
    if (postId == null) {
      return;
    }
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      return;
    }

    PostEntity post = postMapper.selectById(postId);
    if (post == null || post.getAuthorId() == null) {
      return;
    }

    Long receiverId = post.getAuthorId();
    Long fromUserId = user.getUserId();
    if (Objects.equals(receiverId, fromUserId)) {
      // 自己给自己点赞不产生通知
      return;
    }

    NotificationEntity entity = new NotificationEntity();
    entity.setUserId(receiverId);
    entity.setFromUserId(fromUserId);
    entity.setType(NotificationTypeEnum.LIKE);
    entity.setBizType(NotificationBizTypeEnum.POST);
    entity.setBizId(postId);
    entity.setTitle("你的帖子收到一个点赞");
    entity.setContent(buildPostTitleContent(post.getTitle()));
    entity.setIsRead(ReadStatusEnum.UNREAD);

    notificationMapper.insert(entity);

    increaseUnreadCount(receiverId);
  }

  @Override
  public void createPostFavoriteNotification(Long postId) {
    if (postId == null) {
      return;
    }
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      return;
    }

    PostEntity post = postMapper.selectById(postId);
    if (post == null || post.getAuthorId() == null) {
      return;
    }

    Long receiverId = post.getAuthorId();
    Long fromUserId = user.getUserId();
    if (Objects.equals(receiverId, fromUserId)) {
      // 自己收藏自己的帖子不产生通知
      return;
    }

    NotificationEntity entity = new NotificationEntity();
    entity.setUserId(receiverId);
    entity.setFromUserId(fromUserId);
    entity.setType(NotificationTypeEnum.FAVORITE);
    entity.setBizType(NotificationBizTypeEnum.POST);
    entity.setBizId(postId);
    entity.setTitle("你的帖子被用户收藏");
    entity.setContent(buildPostTitleContent(post.getTitle()));
    entity.setIsRead(ReadStatusEnum.UNREAD);

    notificationMapper.insert(entity);

    increaseUnreadCount(receiverId);
  }

  @Override
  public Page<NotificationVO> pageUserNotifications(Long page, Long size, Integer type,
      Integer isRead) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new RuntimeException("未登录，无法查询通知");
    }

    Page<NotificationEntity> pageParam = new Page<>(page, size);

    LambdaQueryWrapper<NotificationEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(NotificationEntity::getUserId, user.getUserId());
    if (type != null) {
      wrapper.eq(NotificationEntity::getType, NotificationTypeEnum.fromCode(type));
    }
    if (isRead != null) {
      wrapper.eq(NotificationEntity::getIsRead, ReadStatusEnum.fromCode(isRead));
    }
    wrapper.orderByDesc(NotificationEntity::getCreateTime);

    Page<NotificationEntity> entityPage = notificationMapper.selectPage(pageParam, wrapper);
    List<NotificationEntity> records = entityPage.getRecords();
    if (records.isEmpty()) {
      return new Page<>(page, size);
    }

    List<NotificationVO> voList =
        records.stream()
            .map(
                entity -> {
                  NotificationVO vo = new NotificationVO();
                  vo.setId(entity.getId());
                  vo.setFromUserId(entity.getFromUserId());
                  vo.setBizId(entity.getBizId());
                  vo.setTitle(entity.getTitle());
                  vo.setContent(entity.getContent());
                  vo.setCreateTime(entity.getCreateTime());
                  vo.setFromUserName(null);
                  if (entity.getType() != null) {
                    vo.setType(entity.getType().getCode());
                    String typeDesc = entity.getType().getDescription();
                    vo.setTypeDesc(typeDesc);
                    vo.setTypeName(typeDesc);
                  }
                  if (entity.getBizType() != null) {
                    vo.setBizType(entity.getBizType().getCode());
                    String bizTypeDesc = entity.getBizType().getDescription();
                    vo.setBizTypeDesc(bizTypeDesc);
                    vo.setBizTypeName(bizTypeDesc);
                  }
                  if (entity.getIsRead() != null) {
                    vo.setIsRead(entity.getIsRead().getCode());
                    String isReadDesc = entity.getIsRead().getDescription();
                    vo.setIsReadDesc(isReadDesc);
                    vo.setIsReadName(isReadDesc);
                  }
                  return vo;
                })
            .collect(Collectors.toList());

    Page<NotificationVO> result = new Page<>(page, size, entityPage.getTotal());
    result.setRecords(voList);
    return result;
  }

  @Override
  public void markRead(Long notificationId) {
    if (notificationId == null) {
      return;
    }
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new RuntimeException("未登录，无法操作通知");
    }

    NotificationEntity entity = notificationMapper.selectById(notificationId);
    if (entity == null || !Objects.equals(entity.getUserId(), user.getUserId())) {
      // 非当前用户的通知直接忽略
      return;
    }
    if (entity.getIsRead() == ReadStatusEnum.READ) {
      return;
    }

    entity.setIsRead(ReadStatusEnum.READ);
    notificationMapper.updateById(entity);

    decreaseUnreadCount(user.getUserId());
  }

  @Override
  public void markAllRead() {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new RuntimeException("未登录，无法操作通知");
    }
    Long userId = user.getUserId();

    NotificationEntity update = new NotificationEntity();
    update.setIsRead(ReadStatusEnum.READ);
    LambdaUpdateWrapper<NotificationEntity> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(NotificationEntity::getUserId, userId)
        .eq(NotificationEntity::getIsRead, ReadStatusEnum.UNREAD);
    notificationMapper.update(update, wrapper);

    // 清空未读数缓存，下一次查询时会重新统计
    String key = USER_NOTIFICATION_UNREAD_COUNT_KEY + userId;
    stringRedisTemplate.delete(key);
  }

  @Override
  public Long getUnreadCount() {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new RuntimeException("未登录，无法查询通知");
    }
    Long userId = user.getUserId();

    String key = USER_NOTIFICATION_UNREAD_COUNT_KEY + userId;
    String val = stringRedisTemplate.opsForValue().get(key);
    if (StringUtils.hasText(val)) {
      try {
        return Long.parseLong(val);
      } catch (NumberFormatException ignored) {
      }
    }

    // 回源数据库统计
    Long count =
        notificationMapper.selectCount(
            new LambdaQueryWrapper<NotificationEntity>()
                .eq(NotificationEntity::getUserId, userId)
                .eq(NotificationEntity::getIsRead, ReadStatusEnum.UNREAD));
    if (count == null) {
      count = 0L;
    }
    stringRedisTemplate.opsForValue().set(key, String.valueOf(count));
    return count;
  }

  /**
   * 解析评论的接收人 ID： 对帖子评论 -> 帖子作者； 对评论回复 -> 被回复用户或父评论作者。
   */
  private Long resolveCommentReceiver(PostCommentEntity comment) {
    if (comment.getParentId() == null) {
      PostEntity post = postMapper.selectById(comment.getPostId());
      return post == null ? null : post.getAuthorId();
    }

    if (comment.getToUserId() != null) {
      return comment.getToUserId();
    }

    PostCommentEntity parent = postCommentMapper.selectById(comment.getParentId());
    if (parent != null && parent.getFromUserId() != null) {
      return parent.getFromUserId();
    }

    PostEntity post = postMapper.selectById(comment.getPostId());
    return post == null ? null : post.getAuthorId();
  }

  /**
   * 评论内容摘要，做简单截断避免过长
   */
  private String buildCommentContent(String content) {
    if (!StringUtils.hasText(content)) {
      return null;
    }
    if (content.length() <= 100) {
      return content;
    }
    return content.substring(0, 100) + "...";
  }

  /**
   * 帖子标题摘要
   */
  private String buildPostTitleContent(String title) {
    if (!StringUtils.hasText(title)) {
      return null;
    }
    if (title.length() <= 50) {
      return "帖子《" + title + "》";
    }
    return "帖子《" + title.substring(0, 50) + "...》";
  }

  /**
   * 未读数自增
   */
  private void increaseUnreadCount(Long userId) {
    if (userId == null) {
      return;
    }
    String key = USER_NOTIFICATION_UNREAD_COUNT_KEY + userId;
    stringRedisTemplate.opsForValue().increment(key);
  }

  /**
   * 未读数自减，最小为 0
   */
  private void decreaseUnreadCount(Long userId) {
    if (userId == null) {
      return;
    }
    String key = USER_NOTIFICATION_UNREAD_COUNT_KEY + userId;
    Long val = stringRedisTemplate.opsForValue().decrement(key);
    if (val != null && val < 0) {
      stringRedisTemplate.opsForValue().set(key, "0");
    }
  }
}

