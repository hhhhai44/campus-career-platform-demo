package com.hhhhai.ccpd.service.impl;

import static com.hhhhai.ccpd.common.constant.RedisConstants.CATEGORY_LIST_TTL_MINUTES;
import static com.hhhhai.ccpd.common.constant.RedisConstants.FORUM_CATEGORY_LIST_KEY;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhhhai.ccpd.common.enums.CategoryStatusEnum;
import com.hhhhai.ccpd.common.enums.LogicDeleteEnum;
import com.hhhhai.ccpd.entity.forum.PostCategoryEntity;
import com.hhhhai.ccpd.mapper.PostCategoryMapper;
import com.hhhhai.ccpd.service.PostCategoryService;
import com.hhhhai.ccpd.vo.CategoryListItemVO;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 帖子分类业务实现：只返回启用分类，带 Redis 缓存
 */
@Service
public class PostCategoryServiceImpl implements PostCategoryService {

  @Resource
  private PostCategoryMapper postCategoryMapper;

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Resource
  private ObjectMapper objectMapper;

  @Override
  public List<CategoryListItemVO> listEnabled() {
    String key = FORUM_CATEGORY_LIST_KEY;
    String json = stringRedisTemplate.opsForValue().get(key);
    if (StringUtils.hasText(json)) {
      try {
        return objectMapper.readValue(json, new TypeReference<List<CategoryListItemVO>>() {});
      } catch (Exception ignored) {
      }
    }

    LambdaQueryWrapper<PostCategoryEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostCategoryEntity::getStatus, CategoryStatusEnum.ENABLED)
        .eq(PostCategoryEntity::getDeleted, LogicDeleteEnum.NOT_DELETED)
        .orderByAsc(PostCategoryEntity::getSort);
    List<PostCategoryEntity> list = postCategoryMapper.selectList(wrapper);
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







