package com.hhhhai.ccpd.service;

import com.hhhhai.ccpd.dto.resource.ResourceRateDTO;
import org.springframework.stereotype.Service;

/**
 * 资源评分业务接口
 */
@Service
public interface ResourceRatingService {

  /**
   * 对资源进行评分（如果用户已经评分则覆盖）
   */
  void rate(ResourceRateDTO dto);
}



