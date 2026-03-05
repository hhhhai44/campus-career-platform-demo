package com.hhhhai.ccpd.controller;

import com.hhhhai.ccpd.common.result.Result;
import com.hhhhai.ccpd.entity.forum.PostCategoryEntity;
import com.hhhhai.ccpd.service.PostCategoryService;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子分类相关接口
 */
@RestController
@RequestMapping("/forum/category")
@AllArgsConstructor
public class PostCategoryController {

  @Resource
  private PostCategoryService postCategoryService;

  /**
   * 查询所有帖子分类
   */
  @GetMapping("/list")
  public Result<List<PostCategoryEntity>> list() {
    List<PostCategoryEntity> list = postCategoryService.listAll();
    return Result.success(list);
  }
}



