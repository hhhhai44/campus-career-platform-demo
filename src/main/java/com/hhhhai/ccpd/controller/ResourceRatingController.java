package com.hhhhai.ccpd.controller;

import com.hhhhai.ccpd.common.result.Result;
import com.hhhhai.ccpd.dto.resource.ResourceRateDTO;
import com.hhhhai.ccpd.service.ResourceRatingService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 资源评分相关接口
 */
@RestController
@RequestMapping("/resource/rating")
@AllArgsConstructor
public class ResourceRatingController {

  @Resource
  private ResourceRatingService resourceRatingService;

  /**
   * 对资源进行评分/更新评分
   */
  @PostMapping
  public Result<Void> rate(@Valid @RequestBody ResourceRateDTO dto) {
    resourceRatingService.rate(dto);
    return Result.success();
  }
}







