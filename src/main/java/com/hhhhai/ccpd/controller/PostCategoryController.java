package com.hhhhai.ccpd.controller;

import com.hhhhai.ccpd.common.result.Result;
import com.hhhhai.ccpd.service.PostCategoryService;
import com.hhhhai.ccpd.vo.CategoryListItemVO;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子分类相关接口（只返回启用分类，按 sort 排序，带 Redis 缓存）
 */
@RestController
@RequestMapping("/forum/category")
@AllArgsConstructor
public class PostCategoryController {

  @Resource
  private PostCategoryService postCategoryService;

  /**
   * 查询所有可用帖子分类
   */
  @GetMapping("/list")
  public Result<List<CategoryListItemVO>> list() {
    List<CategoryListItemVO> list = postCategoryService.listEnabled();
    return Result.success(list);
  }
}







