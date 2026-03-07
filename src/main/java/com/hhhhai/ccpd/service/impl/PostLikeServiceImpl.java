package com.hhhhai.ccpd.service.impl;

import static com.hhhhai.ccpd.common.constant.RedisConstants.POST_LIKE_KEY;
import static com.hhhhai.ccpd.common.constant.RedisConstants.USER_LIKE_POST_KEY;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import com.hhhhai.ccpd.common.enums.ErrorCode;
import com.hhhhai.ccpd.entity.forum.PostEntity;
import com.hhhhai.ccpd.entity.forum.PostLikeEntity;
import com.hhhhai.ccpd.exception.BusinessException;
import com.hhhhai.ccpd.mapper.PostLikeMapper;
import com.hhhhai.ccpd.mapper.PostMapper;
import com.hhhhai.ccpd.service.NotificationService;
import com.hhhhai.ccpd.service.PostLikeService;
import com.hhhhai.ccpd.vo.forum.LikeToggleVO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 帖子点赞业务实现
 */
@Service
public class PostLikeServiceImpl implements PostLikeService {

  @Resource
  private PostLikeMapper postLikeMapper;

  @Resource
  private PostMapper postMapper;

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Resource
  private NotificationService notificationService;

  @Override
  public void like(Long postId) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }
    Long userId = user.getUserId();

    // 防重复：先查是否已存在记录
    LambdaQueryWrapper<PostLikeEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostLikeEntity::getPostId, postId)
        .eq(PostLikeEntity::getUserId, userId);
    PostLikeEntity exist = postLikeMapper.selectOne(wrapper);
    if (exist != null) {
      return;
    }

    // 写入数据库
    PostLikeEntity like = new PostLikeEntity();
    like.setPostId(postId);
    like.setUserId(userId);
    postLikeMapper.insert(like);

    // 更新 Redis：使用 set 存储点赞用户，计数用 scard
    String postLikeKey = POST_LIKE_KEY + postId;
    String userLikeKey = USER_LIKE_POST_KEY + userId;
    stringRedisTemplate.opsForSet().add(postLikeKey, String.valueOf(userId));
    stringRedisTemplate.opsForSet().add(userLikeKey, String.valueOf(postId));

    // 简单地同步冗余字段（非强一致）
    PostEntity post = postMapper.selectById(postId);
    if (post != null) {
      int count = post.getLikeCount() == null ? 0 : post.getLikeCount();
      post.setLikeCount(count + 1);
      postMapper.updateById(post);
    }

    // 发送帖子点赞通知
    notificationService.createPostLikeNotification(postId);
  }

  @Override
  public void unlike(Long postId) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }
    Long userId = user.getUserId();

    LambdaQueryWrapper<PostLikeEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostLikeEntity::getPostId, postId)
        .eq(PostLikeEntity::getUserId, userId);
    PostLikeEntity exist = postLikeMapper.selectOne(wrapper);
    if (exist == null) {
      return;
    }

    postLikeMapper.deleteById(exist.getId());

    String postLikeKey = POST_LIKE_KEY + postId;
    String userLikeKey = USER_LIKE_POST_KEY + userId;
    stringRedisTemplate.opsForSet().remove(postLikeKey, String.valueOf(userId));
    stringRedisTemplate.opsForSet().remove(userLikeKey, String.valueOf(postId));

    PostEntity post = postMapper.selectById(postId);
    if (post != null && post.getLikeCount() != null && post.getLikeCount() > 0) {
      post.setLikeCount(post.getLikeCount() - 1);
      postMapper.updateById(post);
    }
  }

  @Override
  public LikeToggleVO toggleLike(Long postId) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }
    Long userId = user.getUserId();
    String postLikeKey = POST_LIKE_KEY + postId;
    Boolean isMember = stringRedisTemplate.opsForSet().isMember(postLikeKey, String.valueOf(userId));
    boolean liked;
    if (Boolean.TRUE.equals(isMember)) {
      unlike(postId);
      liked = false;
    } else {
      like(postId);
      liked = true;
    }
    Long count = stringRedisTemplate.opsForSet().size(postLikeKey);
    return new LikeToggleVO(liked, count != null ? count : 0L);
  }
}



