package com.hhhhai.ccpd.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.result.Result;
import com.hhhhai.ccpd.dto.forum.PostCreateDTO;
import com.hhhhai.ccpd.service.PostService;
import com.hhhhai.ccpd.vo.forum.PostDetailVO;
import com.hhhhai.ccpd.vo.forum.PostListItemVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 论坛帖子相关接口
 */
@RestController
@RequestMapping("/forum/post")
@AllArgsConstructor
public class PostController {

  @Resource
  private PostService postService;

  /**
   * 发布帖子
   */
  @PostMapping
  public Result<Long> create(@Valid @RequestBody PostCreateDTO dto) {
    Long postId = postService.createPost(dto);
    return Result.success(postId);
  }

  /**
   * 帖子分页列表
   */
  @GetMapping("/page")
  public Result<Page<PostListItemVO>> page(
      @RequestParam(defaultValue = "1") Long page,
      @RequestParam(defaultValue = "10") Long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long categoryId) {
    Page<PostListItemVO> result =
        postService.pagePostList(page, size, keyword, categoryId);
    return Result.success(result);
  }

  /**
   * 帖子详情
   */
  @GetMapping("/{id}")
  public Result<PostDetailVO> detail(@PathVariable("id") Long id) {
    PostDetailVO vo = postService.getPostDetail(id);
    return Result.success(vo);
  }

  /**
   * 删除帖子
   */
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable("id") Long id) {
    postService.deletePost(id);
    return Result.success();
  }

  /**
   * 查询当前用户发布的帖子列表
   */
  @GetMapping("/my")
  public Result<Page<PostListItemVO>> myPosts(
      @RequestParam(defaultValue = "1") Long page,
      @RequestParam(defaultValue = "10") Long size) {
    Page<PostListItemVO> result = postService.pageMyPosts(page, size);
    return Result.success(result);
  }
}







