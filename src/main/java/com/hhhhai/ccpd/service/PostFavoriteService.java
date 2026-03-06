package com.hhhhai.ccpd.service;

import com.hhhhai.ccpd.vo.forum.FavoriteToggleVO;
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

  /**
   * 收藏/取消收藏 Toggle：已收藏则取消，未收藏则收藏。返回当前状态。
   */
  FavoriteToggleVO toggleFavorite(Long postId);
}







