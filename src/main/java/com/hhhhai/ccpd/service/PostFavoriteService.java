package com.hhhhai.ccpd.service;

import org.springframework.stereotype.Service;

/**
 * 帖子收藏业务接口
 */
@Service
public interface PostFavoriteService {

  /**
   * 收藏帖子（如果已收藏则忽略）
   */
  void favorite(Long postId);

  /**
   * 取消收藏（如果未收藏则忽略）
   */
  void unfavorite(Long postId);
}



