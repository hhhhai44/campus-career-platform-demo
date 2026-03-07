package com.hhhhai.ccpd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import com.hhhhai.ccpd.common.enums.ContentStatusEnum;
import com.hhhhai.ccpd.common.enums.ErrorCode;
import com.hhhhai.ccpd.dto.resource.ResourceCommentCreateDTO;
import com.hhhhai.ccpd.entity.resource.ResourceCommentEntity;
import com.hhhhai.ccpd.entity.resource.ResourceEntity;
import com.hhhhai.ccpd.entity.user.UserEntity;
import com.hhhhai.ccpd.exception.BusinessException;
import com.hhhhai.ccpd.mapper.ResourceCommentMapper;
import com.hhhhai.ccpd.mapper.ResourceMapper;
import com.hhhhai.ccpd.mapper.UserMapper;
import com.hhhhai.ccpd.service.ResourceCommentService;
import com.hhhhai.ccpd.vo.resource.ResourceCommentVO;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 资源评论业务实现
 */
@Service
public class ResourceCommentServiceImpl implements ResourceCommentService {

  @Resource
  private ResourceCommentMapper resourceCommentMapper;

  @Resource
  private ResourceMapper resourceMapper;

  @Resource
  private UserMapper userMapper;

  @Override
  @Transactional
  public Long createComment(ResourceCommentCreateDTO dto) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }

    ResourceEntity resource = resourceMapper.selectById(dto.getResourceId());
    if (resource == null) {
      throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
    }

    ResourceCommentEntity entity = new ResourceCommentEntity();
    entity.setResourceId(dto.getResourceId());
    entity.setContent(dto.getContent());
    entity.setFromUserId(user.getUserId());
    entity.setToUserId(dto.getToUserId());
    entity.setParentId(dto.getParentId());
    entity.setLikeCount(0);
    entity.setStatus(ContentStatusEnum.NORMAL);
    if (dto.getRootId() != null) {
      entity.setRootId(dto.getRootId());
    }

    resourceCommentMapper.insert(entity);

    if (entity.getRootId() == null) {
      entity.setRootId(entity.getId());
      resourceCommentMapper.updateById(entity);
    }

    return entity.getId();
  }

  @Override
  public Page<ResourceCommentVO> pageComments(Long resourceId, Long page, Long size) {
    Page<ResourceCommentEntity> pageParam = new Page<>(page, size);

    LambdaQueryWrapper<ResourceCommentEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ResourceCommentEntity::getResourceId, resourceId)
        .eq(ResourceCommentEntity::getStatus, ContentStatusEnum.NORMAL)
        .isNull(ResourceCommentEntity::getParentId)
        .orderByDesc(ResourceCommentEntity::getCreateTime);

    Page<ResourceCommentEntity> entityPage = resourceCommentMapper.selectPage(pageParam, wrapper);
    List<ResourceCommentEntity> roots = entityPage.getRecords();
    if (roots.isEmpty()) {
      return new Page<>(page, size);
    }

    List<Long> rootIds = roots.stream().map(ResourceCommentEntity::getId).collect(Collectors.toList());
    LambdaQueryWrapper<ResourceCommentEntity> childWrapper = new LambdaQueryWrapper<>();
    childWrapper.eq(ResourceCommentEntity::getResourceId, resourceId)
        .in(ResourceCommentEntity::getRootId, rootIds)
        .isNotNull(ResourceCommentEntity::getParentId)
        .eq(ResourceCommentEntity::getStatus, ContentStatusEnum.NORMAL)
        .orderByAsc(ResourceCommentEntity::getCreateTime);
    List<ResourceCommentEntity> children = resourceCommentMapper.selectList(childWrapper);

    Set<Long> userIds = roots.stream().map(ResourceCommentEntity::getFromUserId).collect(Collectors.toSet());
    userIds.addAll(roots.stream()
        .map(ResourceCommentEntity::getToUserId)
        .filter(id -> id != null)
        .collect(Collectors.toSet()));
    userIds.addAll(children.stream().map(ResourceCommentEntity::getFromUserId).collect(Collectors.toSet()));
    userIds.addAll(children.stream()
        .map(ResourceCommentEntity::getToUserId)
        .filter(id -> id != null)
        .collect(Collectors.toSet()));

    final Map<Long, String> userNameMap;
    if (!userIds.isEmpty()) {
      List<UserEntity> users = userMapper.selectBatchIds(new ArrayList<>(userIds));
      userNameMap = users.stream().collect(Collectors.toMap(
          UserEntity::getId,
          u -> u.getRealName() != null && !u.getRealName().trim().isEmpty()
              ? u.getRealName() : u.getUsername()));
    } else {
      userNameMap = new HashMap<>();
    }

    Map<Long, List<ResourceCommentVO>> childrenMap = new HashMap<>();
    for (ResourceCommentEntity child : children) {
      ResourceCommentVO vo = new ResourceCommentVO();
      BeanUtils.copyProperties(child, vo);
      vo.setFromUserName(userNameMap.getOrDefault(child.getFromUserId(), "未知用户"));
      if (child.getToUserId() != null) {
        vo.setToUserName(userNameMap.getOrDefault(child.getToUserId(), "未知用户"));
      }
      childrenMap.computeIfAbsent(child.getRootId(), k -> new ArrayList<>()).add(vo);
    }

    List<ResourceCommentVO> rootVos = roots.stream().map(root -> {
      ResourceCommentVO vo = new ResourceCommentVO();
      BeanUtils.copyProperties(root, vo);
      vo.setFromUserName(userNameMap.getOrDefault(root.getFromUserId(), "未知用户"));
      if (root.getToUserId() != null) {
        vo.setToUserName(userNameMap.getOrDefault(root.getToUserId(), "未知用户"));
      }
      List<ResourceCommentVO> childList = childrenMap.get(root.getId());
      vo.setChildren(childList == null ? new ArrayList<>() : childList);
      return vo;
    }).collect(Collectors.toList());

    Page<ResourceCommentVO> result = new Page<>(page, size, entityPage.getTotal());
    result.setRecords(rootVos);
    return result;
  }

  @Override
  @Transactional
  public void deleteComment(Long commentId) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }

    ResourceCommentEntity comment = resourceCommentMapper.selectById(commentId);
    if (comment == null) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }
    if (!comment.getFromUserId().equals(user.getUserId())) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }

    comment.setStatus(ContentStatusEnum.DISABLED);
    resourceCommentMapper.updateById(comment);
  }
}

