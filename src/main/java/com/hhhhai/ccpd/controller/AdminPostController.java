package com.hhhhai.ccpd.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.result.Result;
import com.hhhhai.ccpd.dto.forum.PostModerationDTO;
import com.hhhhai.ccpd.service.PostService;
import com.hhhhai.ccpd.vo.forum.PostDetailVO;
import com.hhhhai.ccpd.vo.forum.PostListItemVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员帖子审核接口
 */
@RestController
@RequestMapping("/admin/forum/post")
@AllArgsConstructor
public class AdminPostController {

  @Resource
  private PostService postService;

  @GetMapping("/page")
  public Result<Page<PostListItemVO>> page(
      @RequestParam(defaultValue = "1") Long page,
      @RequestParam(defaultValue = "10") Long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String deleted) {
    Integer statusValue = parseNullableInt(status);
    Integer deletedValue = parseNullableInt(deleted);
    return Result.success(postService.pageAdminPosts(page, size, keyword, categoryId, statusValue, deletedValue));
  }

  @GetMapping("/{id}")
  public Result<PostDetailVO> detail(@PathVariable("id") Long id) {
    return Result.success(postService.getAdminPostDetail(id));
  }

  private Integer parseNullableInt(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return Integer.valueOf(raw);
    } catch (NumberFormatException ignore) {
      return null;
    }
  }

  @PutMapping("/{id}/status")
  public Result<Void> review(@PathVariable("id") Long id, @Valid @RequestBody PostModerationDTO dto) {
    postService.reviewPost(id, dto.getStatus());
    return Result.success();
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable("id") Long id) {
    postService.softDeletePost(id);
    return Result.success();
  }

  @PutMapping("/{id}/restore")
  public Result<Void> restore(@PathVariable("id") Long id) {
    postService.restorePost(id);
    return Result.success();
  }
}




