package com.hhhhai.ccpd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import com.hhhhai.ccpd.common.enums.ContentStatusEnum;
import com.hhhhai.ccpd.common.enums.ErrorCode;
import com.hhhhai.ccpd.dto.forum.PostCommentCreateDTO;
import com.hhhhai.ccpd.entity.forum.PostCommentEntity;
import com.hhhhai.ccpd.entity.forum.PostCommentLikeEntity;
import com.hhhhai.ccpd.entity.user.UserEntity;
import com.hhhhai.ccpd.exception.BusinessException;
import com.hhhhai.ccpd.mapper.PostCommentLikeMapper;
import com.hhhhai.ccpd.mapper.PostCommentMapper;
import com.hhhhai.ccpd.mapper.PostMapper;
import com.hhhhai.ccpd.mapper.UserMapper;
import com.hhhhai.ccpd.service.NotificationService;
import com.hhhhai.ccpd.service.PostCommentService;
import com.hhhhai.ccpd.vo.forum.LikeToggleVO;
import com.hhhhai.ccpd.vo.forum.PostCommentVO;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 帖子评论业务实现
 */
@Service
public class PostCommentServiceImpl implements PostCommentService {

  @Resource
  private PostCommentMapper postCommentMapper;

  @Resource
  private PostCommentLikeMapper postCommentLikeMapper;

  @Resource
  private NotificationService notificationService;

  @Resource
  private PostMapper postMapper;

  @Resource
  private UserMapper userMapper;

  @Override
  @Transactional
  public Long createComment(PostCommentCreateDTO dto) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }

    PostCommentEntity entity = new PostCommentEntity();
    entity.setPostId(dto.getPostId());
    entity.setContent(dto.getContent());
    entity.setFromUserId(user.getUserId());
    entity.setToUserId(dto.getToUserId());
    entity.setParentId(dto.getParentId());
    entity.setLikeCount(0);
    entity.setStatus(ContentStatusEnum.NORMAL);

    // 处理 rootId：一级评论 rootId = null，插入后更新为自身ID；二级评论 rootId 使用传入值或父评论的 rootId
    if (dto.getRootId() != null) {
      entity.setRootId(dto.getRootId());
    }

    postCommentMapper.insert(entity);

    // 发送评论通知（帖子评论 / 评论回复）
    notificationService.createPostCommentNotification(entity);

    if (entity.getRootId() == null) {
      entity.setRootId(entity.getId());
      postCommentMapper.updateById(entity);
    }

    // 更新帖子的评论数
    updatePostCommentCount(dto.getPostId());

    return entity.getId();
  }

  @Override
  public Page<PostCommentVO> pageComments(Long postId, Long page, Long size) {
    Page<PostCommentEntity> pageParam = new Page<>(page, size);

    LambdaQueryWrapper<PostCommentEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostCommentEntity::getPostId, postId)
        .eq(PostCommentEntity::getStatus, ContentStatusEnum.NORMAL)
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
        .eq(PostCommentEntity::getStatus, ContentStatusEnum.NORMAL)
        .orderByAsc(PostCommentEntity::getCreateTime);
    List<PostCommentEntity> children = postCommentMapper.selectList(childWrapper);

    // 收集所有需要查询的用户ID
    Set<Long> userIds = roots.stream()
        .map(PostCommentEntity::getFromUserId)
        .collect(Collectors.toSet());
    userIds.addAll(roots.stream()
        .map(PostCommentEntity::getToUserId)
        .filter(id -> id != null)
        .collect(Collectors.toSet()));
    userIds.addAll(children.stream()
        .map(PostCommentEntity::getFromUserId)
        .collect(Collectors.toSet()));
    userIds.addAll(children.stream()
        .map(PostCommentEntity::getToUserId)
        .filter(id -> id != null)
        .collect(Collectors.toSet()));

    // 批量查询用户信息
    final Map<Long, String> userNameMap;
    if (!userIds.isEmpty()) {
      List<UserEntity> users = userMapper.selectBatchIds(new ArrayList<>(userIds));
      userNameMap = users.stream()
          .collect(Collectors.toMap(UserEntity::getId, 
              u -> u.getRealName() != null && !u.getRealName().trim().isEmpty() 
                  ? u.getRealName() : u.getUsername()));
    } else {
      userNameMap = new HashMap<>();
    }

    final Set<Long> likedCommentIds = loadLikedCommentIds(roots, children);

    Map<Long, List<PostCommentVO>> childrenMap = new HashMap<>();
    for (PostCommentEntity child : children) {
      PostCommentVO vo = new PostCommentVO();
      BeanUtils.copyProperties(child, vo);
      vo.setFromUserName(userNameMap.getOrDefault(child.getFromUserId(), "未知用户"));
      vo.setLiked(likedCommentIds.contains(child.getId()));
      if (child.getToUserId() != null) {
        vo.setToUserName(userNameMap.getOrDefault(child.getToUserId(), "未知用户"));
      }
      childrenMap.computeIfAbsent(child.getRootId(), k -> new ArrayList<>()).add(vo);
    }

    List<PostCommentVO> rootVos =
        roots.stream()
            .map(root -> {
              PostCommentVO vo = new PostCommentVO();
              BeanUtils.copyProperties(root, vo);
              vo.setFromUserName(userNameMap.getOrDefault(root.getFromUserId(), "未知用户"));
              vo.setLiked(likedCommentIds.contains(root.getId()));
              if (root.getToUserId() != null) {
                vo.setToUserName(userNameMap.getOrDefault(root.getToUserId(), "未知用户"));
              }
              List<PostCommentVO> childList = childrenMap.get(root.getId());
              vo.setChildren(childList == null ? new ArrayList<>() : childList);
              return vo;
            })
            .collect(Collectors.toList());

    Page<PostCommentVO> result = new Page<>(page, size, entityPage.getTotal());
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

    PostCommentEntity comment = postCommentMapper.selectById(commentId);
    if (comment == null) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }

    // 只能删除自己的评论
    if (!comment.getFromUserId().equals(user.getUserId())) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }

    // 逻辑删除评论（使用DISABLED状态表示已删除）
    comment.setStatus(ContentStatusEnum.DISABLED);
    postCommentMapper.updateById(comment);

    // 更新帖子的评论数
    updatePostCommentCount(comment.getPostId());
  }

  @Override
  @Transactional
  public LikeToggleVO toggleLike(Long commentId) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }

    PostCommentEntity comment = postCommentMapper.selectById(commentId);
    if (comment == null || comment.getStatus() != ContentStatusEnum.NORMAL) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }

    LambdaQueryWrapper<PostCommentLikeEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostCommentLikeEntity::getCommentId, commentId)
        .eq(PostCommentLikeEntity::getUserId, user.getUserId());
    PostCommentLikeEntity exist = postCommentLikeMapper.selectOne(wrapper);

    boolean liked;
    if (exist != null) {
      postCommentLikeMapper.deleteById(exist.getId());
      postCommentMapper.decrementLikeCount(commentId);
      liked = false;
    } else {
      PostCommentLikeEntity entity = new PostCommentLikeEntity();
      entity.setCommentId(commentId);
      entity.setUserId(user.getUserId());
      postCommentLikeMapper.insert(entity);
      postCommentMapper.incrementLikeCount(commentId);
      liked = true;
    }

    PostCommentEntity latest = postCommentMapper.selectById(commentId);
    long count = latest == null || latest.getLikeCount() == null ? 0L : latest.getLikeCount();
    return new LikeToggleVO(liked, count);
  }

  /**
   * 更新帖子的评论数
   */
  private void updatePostCommentCount(Long postId) {
    if (postId == null) {
      return;
    }
    LambdaQueryWrapper<PostCommentEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostCommentEntity::getPostId, postId)
        .eq(PostCommentEntity::getStatus, ContentStatusEnum.NORMAL);
    Long count = postCommentMapper.selectCount(wrapper);
    postMapper.updateCommentCount(postId, count == null ? 0 : count.intValue());
  }

  private Set<Long> loadLikedCommentIds(List<PostCommentEntity> roots,
      List<PostCommentEntity> children) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      return new HashSet<>();
    }
    List<Long> allCommentIds = new ArrayList<>();
    allCommentIds.addAll(roots.stream().map(PostCommentEntity::getId).collect(Collectors.toList()));
    allCommentIds.addAll(children.stream().map(PostCommentEntity::getId).collect(Collectors.toList()));
    if (allCommentIds.isEmpty()) {
      return new HashSet<>();
    }
    return postCommentLikeMapper.selectList(
        new LambdaQueryWrapper<PostCommentLikeEntity>()
            .eq(PostCommentLikeEntity::getUserId, user.getUserId())
            .in(PostCommentLikeEntity::getCommentId, allCommentIds))
        .stream()
        .map(PostCommentLikeEntity::getCommentId)
        .collect(Collectors.toSet());
  }
}
