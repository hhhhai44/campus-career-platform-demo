package com.hhhhai.ccpd.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.result.Result;
import com.hhhhai.ccpd.dto.resource.ResourceCommentCreateDTO;
import com.hhhhai.ccpd.service.ResourceCommentService;
import com.hhhhai.ccpd.vo.resource.ResourceCommentVO;
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
 * 资源评论相关接口
 */
@RestController
@RequestMapping("/resource/comment")
@AllArgsConstructor
public class ResourceCommentController {

  @Resource
  private ResourceCommentService resourceCommentService;

  @PostMapping
  public Result<Long> create(@Valid @RequestBody ResourceCommentCreateDTO dto) {
    Long id = resourceCommentService.createComment(dto);
    return Result.success(id);
  }

  @GetMapping("/{resourceId}/page")
  public Result<Page<ResourceCommentVO>> page(
      @PathVariable("resourceId") Long resourceId,
      @RequestParam(defaultValue = "1") Long page,
      @RequestParam(defaultValue = "10") Long size) {
    Page<ResourceCommentVO> result = resourceCommentService.pageComments(resourceId, page, size);
    return Result.success(result);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable("id") Long id) {
    resourceCommentService.deleteComment(id);
    return Result.success();
  }
}

