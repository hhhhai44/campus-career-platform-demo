package com.hhhhai.ccpd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import com.hhhhai.ccpd.dto.resource.ResourceUploadDTO;
import com.hhhhai.ccpd.entity.resource.ResourceCategoryEntity;
import com.hhhhai.ccpd.entity.resource.ResourceEntity;
import com.hhhhai.ccpd.mapper.ResourceCategoryMapper;
import com.hhhhai.ccpd.mapper.ResourceMapper;
import com.hhhhai.ccpd.service.ResourceService;
import com.hhhhai.ccpd.vo.resource.ResourceDetailVO;
import com.hhhhai.ccpd.vo.resource.ResourceListItemVO;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 资源业务实现
 */
@Service
public class ResourceServiceImpl implements ResourceService {

  @Resource
  private ResourceMapper resourceMapper;

  @Resource
  private ResourceCategoryMapper resourceCategoryMapper;

  @Override
  public Long upload(ResourceUploadDTO dto) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new RuntimeException("未登录，无法上传资源");
    }

    ResourceEntity entity = new ResourceEntity();
    entity.setTitle(dto.getTitle());
    entity.setDescription(dto.getDescription());
    entity.setCategoryId(dto.getCategoryId());
    entity.setUploaderId(user.getUserId());
    entity.setFileUrl(dto.getFileUrl());
    entity.setTags(dto.getTags());
    entity.setScoreAvg(BigDecimal.ZERO);
    entity.setScoreCount(0);
    entity.setLikeCount(0);
    entity.setFavoriteCount(0);
    entity.setDownloadCount(0);
    entity.setStatus(1);
    entity.setDeleted(0);

    resourceMapper.insert(entity);
    return entity.getId();
  }

  @Override
  public Page<ResourceListItemVO> pageList(Long page, Long size, String keyword, Long categoryId) {
    Page<ResourceEntity> pageParam = new Page<>(page, size);

    LambdaQueryWrapper<ResourceEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ResourceEntity::getStatus, 1)
        .eq(ResourceEntity::getDeleted, 0);
    if (StringUtils.hasText(keyword)) {
      wrapper.and(
          w ->
              w.like(ResourceEntity::getTitle, keyword)
                  .or()
                  .like(ResourceEntity::getDescription, keyword));
    }
    if (categoryId != null) {
      wrapper.eq(ResourceEntity::getCategoryId, categoryId);
    }
    wrapper.orderByDesc(ResourceEntity::getCreateTime);

    Page<ResourceEntity> entityPage = resourceMapper.selectPage(pageParam, wrapper);
    List<ResourceEntity> records = entityPage.getRecords();
    if (records.isEmpty()) {
      return new Page<>(page, size);
    }

    List<Long> categoryIds =
        records.stream().map(ResourceEntity::getCategoryId).distinct().collect(Collectors.toList());
    Map<Long, String> categoryNameMap =
        resourceCategoryMapper.selectBatchIds(categoryIds).stream()
            .collect(Collectors.toMap(ResourceCategoryEntity::getId, ResourceCategoryEntity::getName));

    List<ResourceListItemVO> voList =
        records.stream()
            .map(
                entity -> {
                  ResourceListItemVO vo = new ResourceListItemVO();
                  BeanUtils.copyProperties(entity, vo);
                  vo.setCategoryName(categoryNameMap.get(entity.getCategoryId()));
                  vo.setUploaderName(null);
                  return vo;
                })
            .collect(Collectors.toList());

    Page<ResourceListItemVO> result = new Page<>(page, size, entityPage.getTotal());
    result.setRecords(voList);
    return result;
  }

  @Override
  public ResourceDetailVO getDetail(Long resourceId) {
    ResourceEntity entity = resourceMapper.selectById(resourceId);
    if (entity == null || entity.getDeleted() != null && entity.getDeleted() == 1) {
      return null;
    }

    ResourceDetailVO vo = new ResourceDetailVO();
    BeanUtils.copyProperties(entity, vo);

    ResourceCategoryEntity category = resourceCategoryMapper.selectById(entity.getCategoryId());
    if (category != null) {
      vo.setCategoryName(category.getName());
    }
    vo.setUploaderName(null);
    return vo;
  }
}



