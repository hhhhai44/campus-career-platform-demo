package com.hhhhai.ccpd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import com.hhhhai.ccpd.common.enums.ContentStatusEnum;
import com.hhhhai.ccpd.common.enums.ErrorCode;
import com.hhhhai.ccpd.common.enums.LogicDeleteEnum;
import com.hhhhai.ccpd.common.enums.ResourceCategoryEnum;
import com.hhhhai.ccpd.common.enums.TimeRangeEnum;
import com.hhhhai.ccpd.dto.resource.ResourceUploadDTO;
import com.hhhhai.ccpd.entity.resource.ResourceCategoryEntity;
import com.hhhhai.ccpd.entity.resource.ResourceCommentEntity;
import com.hhhhai.ccpd.entity.resource.ResourceEntity;
import com.hhhhai.ccpd.entity.resource.ResourceFavoriteEntity;
import com.hhhhai.ccpd.entity.resource.ResourceLikeEntity;
import com.hhhhai.ccpd.entity.resource.ResourceRatingEntity;
import com.hhhhai.ccpd.entity.user.UserEntity;
import com.hhhhai.ccpd.exception.BusinessException;
import com.hhhhai.ccpd.mapper.ResourceCategoryMapper;
import com.hhhhai.ccpd.mapper.ResourceCommentMapper;
import com.hhhhai.ccpd.mapper.ResourceFavoriteMapper;
import com.hhhhai.ccpd.mapper.ResourceLikeMapper;
import com.hhhhai.ccpd.mapper.ResourceMapper;
import com.hhhhai.ccpd.mapper.ResourceRatingMapper;
import com.hhhhai.ccpd.mapper.UserMapper;
import com.hhhhai.ccpd.service.ResourceService;
import com.hhhhai.ccpd.vo.forum.FavoriteToggleVO;
import com.hhhhai.ccpd.vo.forum.LikeToggleVO;
import com.hhhhai.ccpd.vo.resource.ResourceDetailVO;
import com.hhhhai.ccpd.vo.resource.ResourceListItemVO;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

  @Resource
  private ResourceCommentMapper resourceCommentMapper;

  @Resource
  private ResourceLikeMapper resourceLikeMapper;

  @Resource
  private ResourceFavoriteMapper resourceFavoriteMapper;

  @Resource
  private ResourceRatingMapper resourceRatingMapper;

  @Resource
  private UserMapper userMapper;

  @Override
  public Long upload(ResourceUploadDTO dto) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }

    ResourceEntity entity = new ResourceEntity();
    entity.setTitle(dto.getTitle());
    entity.setDescription(dto.getDescription());
    entity.setContent(dto.getContent());
    entity.setCategoryId(dto.getCategoryId());
    entity.setUploaderId(user.getUserId());
    entity.setTags(dto.getTags());
    entity.setScoreAvg(BigDecimal.ZERO);
    entity.setScoreCount(0);
    entity.setLikeCount(0);
    entity.setFavoriteCount(0);
    entity.setStatus(ContentStatusEnum.NORMAL);
    entity.setDeleted(LogicDeleteEnum.NOT_DELETED);

    resourceMapper.insert(entity);
    return entity.getId();
  }

  @Override
  public Page<ResourceListItemVO> pageList(Long page, Long size, String keyword, Long categoryId,
      String timeRange) {
    Page<ResourceEntity> pageParam = new Page<>(page, size);
    LocalDateTime startTime = TimeRangeEnum.fromCode(timeRange).resolveStartTime();

    LambdaQueryWrapper<ResourceEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ResourceEntity::getStatus, ContentStatusEnum.NORMAL)
        .eq(ResourceEntity::getDeleted, LogicDeleteEnum.NOT_DELETED);
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
    if (startTime != null) {
      wrapper.ge(ResourceEntity::getCreateTime, startTime);
    }
    wrapper.orderByDesc(ResourceEntity::getCreateTime);

    Page<ResourceEntity> entityPage = resourceMapper.selectPage(pageParam, wrapper);
    List<ResourceEntity> records = entityPage.getRecords();
    if (records.isEmpty()) {
      return new Page<>(page, size);
    }

    Map<Long, Long> commentCountMap = loadResourceCommentCountMap(records);

    List<Long> categoryIds =
        records.stream().map(ResourceEntity::getCategoryId).distinct().collect(Collectors.toList());
    Map<Long, String> categoryNameMap =
        resourceCategoryMapper.selectBatchIds(categoryIds).stream()
            .collect(Collectors.toMap(ResourceCategoryEntity::getId, ResourceCategoryEntity::getName));

    List<Long> uploaderIds =
        records.stream().map(ResourceEntity::getUploaderId).distinct().collect(Collectors.toList());
    final Map<Long, String> uploaderNameMap;
    if (!uploaderIds.isEmpty()) {
      List<UserEntity> users = userMapper.selectBatchIds(uploaderIds);
      uploaderNameMap = users.stream().collect(Collectors.toMap(
          UserEntity::getId,
          u -> u.getRealName() != null && !u.getRealName().trim().isEmpty()
              ? u.getRealName() : u.getUsername()));
    } else {
      uploaderNameMap = new HashMap<>();
    }

    List<ResourceListItemVO> voList =
        records.stream()
            .map(
                entity -> {
                  ResourceListItemVO vo = new ResourceListItemVO();
                  BeanUtils.copyProperties(entity, vo);
                  String categoryName = categoryNameMap.get(entity.getCategoryId());
                  if (categoryName == null && entity.getCategoryId() != null) {
                    categoryName = ResourceCategoryEnum.getDescByCode(entity.getCategoryId().intValue());
                  }
                  vo.setCategoryName(categoryName);
                  vo.setUploaderName(uploaderNameMap.getOrDefault(entity.getUploaderId(), "未知用户"));
                  vo.setContentPreview(buildContentPreview(entity.getContent(), entity.getDescription()));
                  vo.setCommentCount(commentCountMap.getOrDefault(entity.getId(), 0L).intValue());
                  return vo;
                })
            .collect(Collectors.toList());

    Page<ResourceListItemVO> result = new Page<>(page, size, entityPage.getTotal());
    result.setRecords(voList);
    return result;
  }

  @Override
  public Page<ResourceListItemVO> pageMyResources(Long page, Long size) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }

    Page<ResourceEntity> pageParam = new Page<>(page, size);
    LambdaQueryWrapper<ResourceEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ResourceEntity::getUploaderId, user.getUserId())
        .eq(ResourceEntity::getDeleted, LogicDeleteEnum.NOT_DELETED)
        .orderByDesc(ResourceEntity::getCreateTime);

    Page<ResourceEntity> entityPage = resourceMapper.selectPage(pageParam, wrapper);
    List<ResourceEntity> records = entityPage.getRecords();
    if (records == null || records.isEmpty()) {
      return new Page<>(page, size, entityPage.getTotal());
    }

    Map<Long, Long> commentCountMap = loadResourceCommentCountMap(records);

    List<Long> categoryIds =
        records.stream().map(ResourceEntity::getCategoryId).distinct().collect(Collectors.toList());
    Map<Long, String> categoryNameMap =
        resourceCategoryMapper.selectBatchIds(categoryIds).stream()
            .collect(Collectors.toMap(ResourceCategoryEntity::getId, ResourceCategoryEntity::getName));

    List<ResourceListItemVO> voList =
        records.stream()
            .map(entity -> {
              ResourceListItemVO vo = new ResourceListItemVO();
              BeanUtils.copyProperties(entity, vo);
              String categoryName = categoryNameMap.get(entity.getCategoryId());
              if (categoryName == null && entity.getCategoryId() != null) {
                categoryName = ResourceCategoryEnum.getDescByCode(entity.getCategoryId().intValue());
              }
              vo.setCategoryName(categoryName);
              vo.setUploaderName(StringUtils.hasText(user.getUsername()) ? user.getUsername() : "我");
              vo.setContentPreview(buildContentPreview(entity.getContent(), entity.getDescription()));
              vo.setCommentCount(commentCountMap.getOrDefault(entity.getId(), 0L).intValue());
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
    if (entity == null || entity.getDeleted() == LogicDeleteEnum.DELETED) {
      return null;
    }

    ResourceDetailVO vo = new ResourceDetailVO();
    BeanUtils.copyProperties(entity, vo);

    ResourceCategoryEntity category = resourceCategoryMapper.selectById(entity.getCategoryId());
    if (category != null) {
      vo.setCategoryName(category.getName());
    } else if (entity.getCategoryId() != null) {
      vo.setCategoryName(ResourceCategoryEnum.getDescByCode(entity.getCategoryId().intValue()));
    }

    if (entity.getUploaderId() != null) {
      UserEntity uploader = userMapper.selectById(entity.getUploaderId());
      if (uploader != null) {
        vo.setUploaderName(uploader.getRealName() != null && !uploader.getRealName().trim().isEmpty()
            ? uploader.getRealName() : uploader.getUsername());
      } else {
        vo.setUploaderName("未知用户");
      }
    }

    vo.setContent(entity.getContent());

    vo.setLiked(false);
    vo.setFavorited(false);
    vo.setOwner(false);
    vo.setHasRated(false);
    UserContext user = UserContextHolder.getUser();
    if (user != null && user.getUserId() != null) {
      Long currentUserId = user.getUserId();
      vo.setOwner(entity.getUploaderId() != null && entity.getUploaderId().equals(currentUserId));

      LambdaQueryWrapper<ResourceLikeEntity> likeWrapper = new LambdaQueryWrapper<>();
      likeWrapper.eq(ResourceLikeEntity::getResourceId, resourceId)
          .eq(ResourceLikeEntity::getUserId, currentUserId);
      vo.setLiked(resourceLikeMapper.selectCount(likeWrapper) > 0);

      LambdaQueryWrapper<ResourceFavoriteEntity> favoriteWrapper = new LambdaQueryWrapper<>();
      favoriteWrapper.eq(ResourceFavoriteEntity::getResourceId, resourceId)
          .eq(ResourceFavoriteEntity::getUserId, currentUserId);
      vo.setFavorited(resourceFavoriteMapper.selectCount(favoriteWrapper) > 0);

      LambdaQueryWrapper<ResourceRatingEntity> ratingWrapper = new LambdaQueryWrapper<>();
      ratingWrapper.eq(ResourceRatingEntity::getResourceId, resourceId)
          .eq(ResourceRatingEntity::getUserId, currentUserId);
      vo.setHasRated(resourceRatingMapper.selectCount(ratingWrapper) > 0);
    }

    return vo;
  }

  @Override
  @Transactional
  public LikeToggleVO toggleLike(Long resourceId) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }

    ResourceEntity resource = resourceMapper.selectById(resourceId);
    if (resource == null || resource.getDeleted() == LogicDeleteEnum.DELETED) {
      throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
    }

    LambdaQueryWrapper<ResourceLikeEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ResourceLikeEntity::getResourceId, resourceId)
        .eq(ResourceLikeEntity::getUserId, user.getUserId());
    ResourceLikeEntity exist = resourceLikeMapper.selectOne(wrapper);

    boolean liked;
    if (exist != null) {
      resourceLikeMapper.deleteById(exist.getId());
      resourceMapper.decrementLikeCount(resourceId);
      liked = false;
    } else {
      ResourceLikeEntity entity = new ResourceLikeEntity();
      entity.setResourceId(resourceId);
      entity.setUserId(user.getUserId());
      resourceLikeMapper.insert(entity);
      resourceMapper.incrementLikeCount(resourceId);
      liked = true;
    }

    ResourceEntity latest = resourceMapper.selectById(resourceId);
    long count = latest == null || latest.getLikeCount() == null ? 0L : latest.getLikeCount();
    return new LikeToggleVO(liked, count);
  }

  @Override
  @Transactional
  public FavoriteToggleVO toggleFavorite(Long resourceId) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }

    ResourceEntity resource = resourceMapper.selectById(resourceId);
    if (resource == null || resource.getDeleted() == LogicDeleteEnum.DELETED) {
      throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
    }

    LambdaQueryWrapper<ResourceFavoriteEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ResourceFavoriteEntity::getResourceId, resourceId)
        .eq(ResourceFavoriteEntity::getUserId, user.getUserId());
    ResourceFavoriteEntity exist = resourceFavoriteMapper.selectOne(wrapper);

    boolean favorited;
    if (exist != null) {
      resourceFavoriteMapper.deleteById(exist.getId());
      resourceMapper.decrementFavoriteCount(resourceId);
      favorited = false;
    } else {
      ResourceFavoriteEntity entity = new ResourceFavoriteEntity();
      entity.setResourceId(resourceId);
      entity.setUserId(user.getUserId());
      resourceFavoriteMapper.insert(entity);
      resourceMapper.incrementFavoriteCount(resourceId);
      favorited = true;
    }
    return new FavoriteToggleVO(favorited);
  }

  private Map<Long, Long> loadResourceCommentCountMap(List<ResourceEntity> resources) {
    List<Long> resourceIds = resources.stream().map(ResourceEntity::getId).collect(Collectors.toList());
    if (resourceIds.isEmpty()) {
      return new HashMap<>();
    }
    return resourceCommentMapper.selectList(
            new LambdaQueryWrapper<ResourceCommentEntity>()
                .in(ResourceCommentEntity::getResourceId, resourceIds)
                .eq(ResourceCommentEntity::getStatus, ContentStatusEnum.NORMAL))
        .stream()
        .collect(Collectors.groupingBy(ResourceCommentEntity::getResourceId, Collectors.counting()));
  }

  private String buildContentPreview(String content, String description) {
    String source = StringUtils.hasText(content) ? content : description;
    if (!StringUtils.hasText(source)) {
      return null;
    }
    String normalized = source.replaceAll("\\s+", " ").trim();
    return normalized.length() <= 120 ? normalized : normalized.substring(0, 120) + "...";
  }
}
