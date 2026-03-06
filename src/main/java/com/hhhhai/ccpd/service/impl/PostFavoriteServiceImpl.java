package com.hhhhai.ccpd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import com.hhhhai.ccpd.entity.forum.PostFavoriteEntity;
import com.hhhhai.ccpd.mapper.PostFavoriteMapper;
import com.hhhhai.ccpd.service.NotificationService;
import com.hhhhai.ccpd.service.PostFavoriteService;
import com.hhhhai.ccpd.vo.forum.FavoriteToggleVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 帖子收藏业务实现
 */
@Service
public class PostFavoriteServiceImpl implements PostFavoriteService {

  @Resource
  private PostFavoriteMapper postFavoriteMapper;

  @Resource
  private NotificationService notificationService;

  @Override
  public void favorite(Long postId) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new RuntimeException("未登录，无法收藏");
    }
    Long userId = user.getUserId();

    LambdaQueryWrapper<PostFavoriteEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostFavoriteEntity::getPostId, postId)
        .eq(PostFavoriteEntity::getUserId, userId);
    PostFavoriteEntity exist = postFavoriteMapper.selectOne(wrapper);
    if (exist != null) {
      return;
    }

    PostFavoriteEntity favorite = new PostFavoriteEntity();
    favorite.setPostId(postId);
    favorite.setUserId(userId);
    postFavoriteMapper.insert(favorite);

    // 发送帖子收藏通知
    notificationService.createPostFavoriteNotification(postId);
  }

  @Override
  public void unfavorite(Long postId) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new RuntimeException("未登录，无法取消收藏");
    }
    Long userId = user.getUserId();

    LambdaQueryWrapper<PostFavoriteEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostFavoriteEntity::getPostId, postId)
        .eq(PostFavoriteEntity::getUserId, userId);
    PostFavoriteEntity exist = postFavoriteMapper.selectOne(wrapper);
    if (exist == null) {
      return;
    }
    postFavoriteMapper.deleteById(exist.getId());
  }

  @Override
  public FavoriteToggleVO toggleFavorite(Long postId) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new RuntimeException("未登录，无法收藏");
    }
    Long userId = user.getUserId();
    LambdaQueryWrapper<PostFavoriteEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostFavoriteEntity::getPostId, postId)
        .eq(PostFavoriteEntity::getUserId, userId);
    PostFavoriteEntity exist = postFavoriteMapper.selectOne(wrapper);
    boolean favorited;
    if (exist != null) {
      postFavoriteMapper.deleteById(exist.getId());
      favorited = false;
    } else {
      PostFavoriteEntity favorite = new PostFavoriteEntity();
      favorite.setPostId(postId);
      favorite.setUserId(userId);
      postFavoriteMapper.insert(favorite);
      notificationService.createPostFavoriteNotification(postId);
      favorited = true;
    }
    return new FavoriteToggleVO(favorited);
  }
}







