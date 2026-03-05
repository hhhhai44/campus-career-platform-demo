package com.hhhhai.ccpd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import com.hhhhai.ccpd.dto.forum.PostCommentCreateDTO;
import com.hhhhai.ccpd.entity.forum.PostCommentEntity;
import com.hhhhai.ccpd.mapper.PostCommentMapper;
import com.hhhhai.ccpd.service.PostCommentService;
import com.hhhhai.ccpd.vo.forum.PostCommentVO;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 帖子评论业务实现
 */
@Service
public class PostCommentServiceImpl implements PostCommentService {

  @Resource
  private PostCommentMapper postCommentMapper;

  @Override
  public Long createComment(PostCommentCreateDTO dto) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new RuntimeException("未登录，无法评论");
    }

    PostCommentEntity entity = new PostCommentEntity();
    entity.setPostId(dto.getPostId());
    entity.setContent(dto.getContent());
    entity.setFromUserId(user.getUserId());
    entity.setToUserId(dto.getToUserId());
    entity.setParentId(dto.getParentId());
    entity.setLikeCount(0);
    entity.setStatus(1);

    // 处理 rootId：一级评论 rootId = null，插入后更新为自身ID；二级评论 rootId 使用传入值或父评论的 rootId
    if (dto.getRootId() != null) {
      entity.setRootId(dto.getRootId());
    }

    postCommentMapper.insert(entity);

    if (entity.getRootId() == null) {
      entity.setRootId(entity.getId());
      postCommentMapper.updateById(entity);
    }

    return entity.getId();
  }

  @Override
  public Page<PostCommentVO> pageComments(Long postId, Long page, Long size) {
    Page<PostCommentEntity> pageParam = new Page<>(page, size);

    LambdaQueryWrapper<PostCommentEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostCommentEntity::getPostId, postId)
        .eq(PostCommentEntity::getStatus, 1)
        .isNull(PostCommentEntity::getParentId)
        .orderByDesc(PostCommentEntity::getCreateTime);

    Page<PostCommentEntity> entityPage = postCommentMapper.selectPage(pageParam, wrapper);
    List<PostCommentEntity> roots = entityPage.getRecords();
    if (roots.isEmpty()) {
      return new Page<>(page, size);
    }

    // 查询这些根评论下的所有二级评论
    List<Long> rootIds = roots.stream().map(PostCommentEntity::getId).collect(Collectors.toList());
    LambdaQueryWrapper<PostCommentEntity> childWrapper = new LambdaQueryWrapper<>();
    childWrapper.eq(PostCommentEntity::getPostId, postId)
        .in(PostCommentEntity::getRootId, rootIds)
        .isNotNull(PostCommentEntity::getParentId)
        .eq(PostCommentEntity::getStatus, 1)
        .orderByAsc(PostCommentEntity::getCreateTime);
    List<PostCommentEntity> children = postCommentMapper.selectList(childWrapper);

    Map<Long, List<PostCommentVO>> childrenMap = new HashMap<>();
    for (PostCommentEntity child : children) {
      PostCommentVO vo = new PostCommentVO();
      BeanUtils.copyProperties(child, vo);
      vo.setFromUserName(null);
      vo.setToUserName(null);
      childrenMap.computeIfAbsent(
              child.getRootId(), k -> new ArrayList<>())
          .add(vo);
    }

    List<PostCommentVO> rootVos =
        roots.stream()
            .map(
                root -> {
                  PostCommentVO vo = new PostCommentVO();
                  BeanUtils.copyProperties(root, vo);
                  vo.setFromUserName(null);
                  vo.setToUserName(null);
                  List<PostCommentVO> childList = childrenMap.get(root.getId());
                  if (childList != null) {
                    vo.setChildren(childList);
                  } else {
                    vo.setChildren(new ArrayList<>());
                  }
                  return vo;
                })
            .collect(Collectors.toList());

    Page<PostCommentVO> result = new Page<>(page, size, entityPage.getTotal());
    result.setRecords(rootVos);
    return result;
  }
}



