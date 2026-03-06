package com.hhhhai.ccpd.service.impl;

import static com.hhhhai.ccpd.common.constant.RedisConstants.CATEGORY_LIST_TTL_MINUTES;
import static com.hhhhai.ccpd.common.constant.RedisConstants.RESOURCE_CATEGORY_LIST_KEY;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhhhai.ccpd.common.enums.CategoryStatusEnum;
import com.hhhhai.ccpd.common.enums.LogicDeleteEnum;
import com.hhhhai.ccpd.entity.resource.ResourceCategoryEntity;
import com.hhhhai.ccpd.mapper.ResourceCategoryMapper;
import com.hhhhai.ccpd.service.ResourceCategoryService;
import com.hhhhai.ccpd.vo.CategoryListItemVO;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 资源分类业务实现：只返回启用分类，带 Redis 缓存
 */
@Service
public class ResourceCategoryServiceImpl implements ResourceCategoryService {

  @Resource
  private ResourceCategoryMapper resourceCategoryMapper;

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Resource
  private ObjectMapper objectMapper;

  @Override
  public List<CategoryListItemVO> listEnabled() {
    String key = RESOURCE_CATEGORY_LIST_KEY;
    String json = stringRedisTemplate.opsForValue().get(key);
    if (StringUtils.hasText(json)) {
      try {
        return objectMapper.readValue(json, new TypeReference<List<CategoryListItemVO>>() {});
      } catch (Exception ignored) {
      }
    }

    LambdaQueryWrapper<ResourceCategoryEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ResourceCategoryEntity::getStatus, CategoryStatusEnum.ENABLED)
        .eq(ResourceCategoryEntity::getDeleted, LogicDeleteEnum.NOT_DELETED)
        .orderByAsc(ResourceCategoryEntity::getSort);
    List<ResourceCategoryEntity> list = resourceCategoryMapper.selectList(wrapper);
    List<CategoryListItemVO> voList = list.stream()
        .map(e -> new CategoryListItemVO(e.getId(), e.getName()))
        .collect(Collectors.toList());

    try {
      stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(voList), CATEGORY_LIST_TTL_MINUTES, TimeUnit.MINUTES);
    } catch (Exception ignored) {
    }
    return voList;
  }
}
