package com.hhhhai.ccpd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import com.hhhhai.ccpd.dto.resource.ResourceRateDTO;
import com.hhhhai.ccpd.entity.resource.ResourceEntity;
import com.hhhhai.ccpd.entity.resource.ResourceRatingEntity;
import com.hhhhai.ccpd.mapper.ResourceMapper;
import com.hhhhai.ccpd.mapper.ResourceRatingMapper;
import com.hhhhai.ccpd.service.ResourceRatingService;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

/**
 * 资源评分业务实现
 */
@Service
public class ResourceRatingServiceImpl implements ResourceRatingService {

  @Resource
  private ResourceRatingMapper resourceRatingMapper;

  @Resource
  private ResourceMapper resourceMapper;

  @Override
  public void rate(ResourceRateDTO dto) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new RuntimeException("未登录，无法评分");
    }
    Long userId = user.getUserId();

    Long resourceId = dto.getResourceId();

    // 查询是否已有评分记录，有则更新，没有则插入
    LambdaQueryWrapper<ResourceRatingEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ResourceRatingEntity::getResourceId, resourceId)
        .eq(ResourceRatingEntity::getUserId, userId);
    ResourceRatingEntity exist = resourceRatingMapper.selectOne(wrapper);
    if (exist == null) {
      ResourceRatingEntity rating = new ResourceRatingEntity();
      rating.setResourceId(resourceId);
      rating.setUserId(userId);
      rating.setScore(dto.getScore());
      rating.setComment(dto.getComment());
      resourceRatingMapper.insert(rating);
    } else {
      exist.setScore(dto.getScore());
      exist.setComment(dto.getComment());
      resourceRatingMapper.updateById(exist);
    }

    // 重新统计该资源的平均分和评分人数，并更新到 resource 表
    // 为简单起见，这里直接从数据库聚合计算
    Integer totalScore =
        resourceRatingMapper.selectList(
                new LambdaQueryWrapper<ResourceRatingEntity>()
                    .eq(ResourceRatingEntity::getResourceId, resourceId))
            .stream()
            .map(ResourceRatingEntity::getScore)
            .reduce(0, Integer::sum);
    long count =
        resourceRatingMapper.selectCount(
            new LambdaQueryWrapper<ResourceRatingEntity>()
                .eq(ResourceRatingEntity::getResourceId, resourceId));

    if (count > 0) {
      BigDecimal avg =
          BigDecimal.valueOf(totalScore)
              .divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
      ResourceEntity resource = resourceMapper.selectById(resourceId);
      if (resource != null) {
        resource.setScoreAvg(avg);
        resource.setScoreCount((int) count);
        resourceMapper.updateById(resource);
      }
    }
  }
}


