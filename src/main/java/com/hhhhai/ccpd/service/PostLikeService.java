package com.hhhhai.ccpd.service;

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
}



