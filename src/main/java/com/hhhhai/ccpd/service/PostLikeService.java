package com.hhhhai.ccpd.service;

import com.hhhhai.ccpd.vo.forum.LikeToggleVO;
import org.springframework.stereotype.Service;

/**
 * 帖子点赞业务接口
 */
@Service
public interface PostLikeService {

  /**
   * 点赞（如果已点赞则忽略）
   */
  void like(Long postId);

  /**
   * 取消点赞（如果未点赞则忽略）
   */
  void unlike(Long postId);

  /**
   * 点赞/取消点赞 Toggle：已赞则取消，未赞则点赞。返回当前状态与最新点赞数。
   */
  LikeToggleVO toggleLike(Long postId);
}







