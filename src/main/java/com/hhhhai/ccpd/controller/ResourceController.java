package com.hhhhai.ccpd.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.result.Result;
import com.hhhhai.ccpd.dto.resource.ResourceUploadDTO;
import com.hhhhai.ccpd.service.ResourceService;
import com.hhhhai.ccpd.vo.forum.FavoriteToggleVO;
import com.hhhhai.ccpd.vo.forum.LikeToggleVO;
import com.hhhhai.ccpd.vo.resource.ResourceDetailVO;
import com.hhhhai.ccpd.vo.resource.ResourceListItemVO;
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
 * 资源库模块接口
 */
@RestController
@RequestMapping("/resource")
@AllArgsConstructor
public class ResourceController {

  @Resource
  private ResourceService resourceService;

  /**
   * 资源上传
   */
  @PostMapping
  public Result<Long> upload(@Valid @RequestBody ResourceUploadDTO dto) {
    Long id = resourceService.upload(dto);
    return Result.success(id);
  }

  /**
   * 资源分页列表 + 搜索
   */
  @GetMapping("/page")
  public Result<Page<ResourceListItemVO>> page(
      @RequestParam(defaultValue = "1") Long page,
      @RequestParam(defaultValue = "10") Long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long categoryId) {
    Page<ResourceListItemVO> result =
        resourceService.pageList(page, size, keyword, categoryId);
    return Result.success(result);
  }

  /**
   * 资源详情
   */
  @GetMapping("/{id}")
  public Result<ResourceDetailVO> detail(@PathVariable("id") Long id) {
    ResourceDetailVO vo = resourceService.getDetail(id);
    return Result.success(vo);
  }

  /**
   * 资源点赞/取消点赞 Toggle
   */
  @PostMapping("/{id}/like/toggle")
  public Result<LikeToggleVO> likeToggle(@PathVariable("id") Long id) {
    LikeToggleVO vo = resourceService.toggleLike(id);
    return Result.success(vo);
  }

  /**
   * 资源收藏/取消收藏 Toggle
   */
  @PostMapping("/{id}/favorite/toggle")
  public Result<FavoriteToggleVO> favoriteToggle(@PathVariable("id") Long id) {
    FavoriteToggleVO vo = resourceService.toggleFavorite(id);
    return Result.success(vo);
  }

  /**
   * 获取下载地址并累加下载次数
   */
  @PostMapping("/{id}/download")
  public Result<String> download(@PathVariable("id") Long id) {
    String url = resourceService.getDownloadUrlAndIncreaseCount(id);
    return Result.success(url);
  }
}
