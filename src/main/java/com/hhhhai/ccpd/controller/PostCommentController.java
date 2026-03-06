package com.hhhhai.ccpd.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.result.Result;
import com.hhhhai.ccpd.dto.forum.PostCommentCreateDTO;
import com.hhhhai.ccpd.service.PostCommentService;
import com.hhhhai.ccpd.vo.forum.PostCommentVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子评论相关接口
 */
@RestController
@RequestMapping("/forum/post/comment")
@AllArgsConstructor
public class PostCommentController {

  @Resource
  private PostCommentService postCommentService;

  /**
   * 新增评论 / 回复
   */
  @PostMapping
  public Result<Long> create(@Valid @RequestBody PostCommentCreateDTO dto) {
    Long id = postCommentService.createComment(dto);
    return Result.success(id);
  }

  /**
   * 某个帖子的评论列表（分页，一级评论 + 二级评论）
   */
  @GetMapping("/{postId}/page")
  public Result<Page<PostCommentVO>> page(
      @PathVariable("postId") Long postId,
      @RequestParam(defaultValue = "1") Long page,
      @RequestParam(defaultValue = "10") Long size) {
    Page<PostCommentVO> result = postCommentService.pageComments(postId, page, size);
    return Result.success(result);
  }
}







