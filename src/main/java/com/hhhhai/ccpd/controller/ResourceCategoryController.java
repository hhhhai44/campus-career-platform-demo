package com.hhhhai.ccpd.controller;

import com.hhhhai.ccpd.common.result.Result;
import com.hhhhai.ccpd.service.ResourceCategoryService;
import com.hhhhai.ccpd.vo.CategoryListItemVO;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 资源分类相关接口（只返回启用分类，按 sort 排序，带 Redis 缓存）
 */
@RestController
@RequestMapping("/resource/category")
@AllArgsConstructor
public class ResourceCategoryController {

  @Resource
  private ResourceCategoryService resourceCategoryService;

  /**
   * 查询所有可用资源分类
   */
  @GetMapping("/list")
  public Result<List<CategoryListItemVO>> list() {
    List<CategoryListItemVO> list = resourceCategoryService.listEnabled();
    return Result.success(list);
  }
}
