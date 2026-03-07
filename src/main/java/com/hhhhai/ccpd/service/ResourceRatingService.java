package com.hhhhai.ccpd.service;

import com.hhhhai.ccpd.dto.resource.ResourceRateDTO;
import org.springframework.stereotype.Service;

/**
 * 资源评分业务接口
 */
@Service
public interface ResourceRatingService {

  /**
   * 对资源进行评分（每个用户只能评分一次，且不能给自己的资源评分）
   */
  void rate(ResourceRateDTO dto);
}





