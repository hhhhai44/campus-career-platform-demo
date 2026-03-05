package com.hhhhai.ccpd.controller;

import com.hhhhai.ccpd.common.result.Result;
import com.hhhhai.ccpd.service.PostFavoriteService;
import com.hhhhai.ccpd.service.PostLikeService;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子点赞 / 收藏等互动接口
 */
@RestController
@RequestMapping("/forum/post")
@AllArgsConstructor
public class PostInteractionController {

  @Resource
  private PostLikeService postLikeService;

  @Resource
  private PostFavoriteService postFavoriteService;

  /**
   * 点赞帖子
   */
  @PostMapping("/{postId}/like")
  public Result<Void> like(@PathVariable("postId") Long postId) {
    postLikeService.like(postId);
    return Result.success();
  }

  /**
   * 取消点赞
   */
  @DeleteMapping("/{postId}/like")
  public Result<Void> unlike(@PathVariable("postId") Long postId) {
    postLikeService.unlike(postId);
    return Result.success();
  }

  /**
   * 收藏帖子
   */
  @PostMapping("/{postId}/favorite")
  public Result<Void> favorite(@PathVariable("postId") Long postId) {
    postFavoriteService.favorite(postId);
    return Result.success();
  }

  /**
   * 取消收藏
   */
  @DeleteMapping("/{postId}/favorite")
  public Result<Void> unfavorite(@PathVariable("postId") Long postId) {
    postFavoriteService.unfavorite(postId);
    return Result.success();
  }
}



