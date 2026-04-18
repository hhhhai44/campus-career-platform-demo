package com.hhhhai.ccpd.service.impl;

import static com.hhhhai.ccpd.common.constant.RedisConstants.POST_LIKE_KEY;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import com.hhhhai.ccpd.common.enums.ContentStatusEnum;
import com.hhhhai.ccpd.common.enums.ErrorCode;
import com.hhhhai.ccpd.common.enums.LogicDeleteEnum;
import com.hhhhai.ccpd.common.enums.PostCategoryEnum;
import com.hhhhai.ccpd.common.enums.TimeRangeEnum;
import com.hhhhai.ccpd.common.enums.UserRoleEnum;
import com.hhhhai.ccpd.dto.forum.PostCreateDTO;
import com.hhhhai.ccpd.entity.forum.PostCategoryEntity;
import com.hhhhai.ccpd.entity.forum.PostEntity;
import com.hhhhai.ccpd.entity.forum.PostFavoriteEntity;
import com.hhhhai.ccpd.entity.forum.PostLikeEntity;
import com.hhhhai.ccpd.entity.user.UserEntity;
import com.hhhhai.ccpd.exception.BusinessException;
import com.hhhhai.ccpd.mapper.PostCategoryMapper;
import com.hhhhai.ccpd.mapper.PostFavoriteMapper;
import com.hhhhai.ccpd.mapper.PostLikeMapper;
import com.hhhhai.ccpd.mapper.PostMapper;
import com.hhhhai.ccpd.mapper.UserMapper;
import com.hhhhai.ccpd.service.PostService;
import com.hhhhai.ccpd.vo.forum.PostDetailVO;
import com.hhhhai.ccpd.vo.forum.PostListItemVO;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 帖子业务实现
 */
@Service
public class PostServiceImpl implements PostService {

  @Resource
  private PostMapper postMapper;

  @Resource
  private PostCategoryMapper postCategoryMapper;

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Resource
  private PostFavoriteMapper postFavoriteMapper;

  @Resource
  private PostLikeMapper postLikeMapper;

  @Resource
  private UserMapper userMapper;

  @Override
  public Long createPost(PostCreateDTO dto) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new RuntimeException("未登录，无法发帖");
    }

    PostEntity entity = new PostEntity();
    entity.setTitle(dto.getTitle());
    entity.setContent(dto.getContent());
    entity.setCategoryId(dto.getCategoryId());
    entity.setAuthorId(user.getUserId());
    entity.setLikeCount(0);
    entity.setViewCount(0);
    entity.setCommentCount(0);
    entity.setIsTop(0);
    entity.setStatus(ContentStatusEnum.NORMAL);

    postMapper.insert(entity);
    return entity.getId();
  }

  @Override
  public Page<PostListItemVO> pagePostList(Long page, Long size, String keyword, Long categoryId,
      String timeRange) {
    return queryPostPage(page, size, keyword, categoryId, timeRange, ContentStatusEnum.NORMAL,
        LogicDeleteEnum.NOT_DELETED, null, false);
  }

  @Override
  public Page<PostListItemVO> pageMyPosts(Long page, Long size) {
    UserContext user = requireLogin();
    return queryPostPage(page, size, null, null, null, null, LogicDeleteEnum.NOT_DELETED,
        user.getUserId(), false);
  }

  @Override
  public Page<PostListItemVO> pageAdminPosts(Long page, Long size, String keyword, Long categoryId,
      Integer status, Integer deleted) {
    Integer statusParam = deleted != null && deleted == 1 ? null : status;
    long safePage = page == null || page < 1 ? 1 : page;
    long safeSize = size == null || size < 1 ? 10 : Math.min(size, 100);
    long offset = (safePage - 1) * safeSize;

    Page<PostEntity> entityPage = new Page<>(safePage, safeSize);
    Long total = postMapper.countAdminPage(keyword, categoryId, statusParam, deleted);
    entityPage.setTotal(total == null ? 0 : total);

    List<PostEntity> records = total == null || total <= 0
        ? Collections.emptyList()
        : postMapper.selectAdminPage(keyword, categoryId, statusParam, deleted, offset, safeSize);
    entityPage.setRecords(records);
    return convertToPostListPage(entityPage, safePage, safeSize, true);
  }

  @Override
  public PostDetailVO getPostDetail(Long postId) {
    return buildPostDetail(postId, false, true);
  }

  @Override
  public PostDetailVO getAdminPostDetail(Long postId) {
    ensureAdmin();
    return buildPostDetail(postId, true, false);
  }

  private PostDetailVO buildPostDetail(Long postId, boolean allowHidden, boolean increaseViewCount) {
    PostEntity entity = getExistingPost(postId, allowHidden);
    if (!allowHidden
        && (entity.getDeleted() == LogicDeleteEnum.DELETED || entity.getStatus() == ContentStatusEnum.DISABLED)) {
      return null;
    }

    if (increaseViewCount) {
      // 累计浏览量（简单处理：数据库字段自增）
      entity.setViewCount(entity.getViewCount() == null ? 1 : entity.getViewCount() + 1);
      postMapper.updateById(entity);
    }

    PostDetailVO vo = new PostDetailVO();
    BeanUtils.copyProperties(entity, vo);

    // 分类名称：优先 DB，缺失时用枚举兜底
    PostCategoryEntity category = postCategoryMapper.selectById(entity.getCategoryId());
    if (category != null) {
      vo.setCategoryName(category.getName());
    } else if (entity.getCategoryId() != null) {
      vo.setCategoryName(PostCategoryEnum.getDescByCode(entity.getCategoryId().intValue()));
    }

    // 点赞数从Redis获取
    vo.setLikeCount(getPostLikeCount(entity.getId(), entity.getLikeCount()));

    // 默认 false，避免序列化缺失时前端首屏状态错误
    vo.setLiked(false);
    vo.setFavorited(false);
    if (allowHidden) {
      vo.setStatus(entity.getStatus() == null ? null : entity.getStatus().getCode());
      vo.setDeleted(entity.getDeleted() == null ? null : entity.getDeleted().getCode());
    }
    vo.setAuthorName(loadAuthorName(entity.getAuthorId()));

    UserContext user = UserContextHolder.getUser();
    if (user != null && user.getUserId() != null) {
      Long userId = user.getUserId();
      vo.setCurrentUserId(userId);

      LambdaQueryWrapper<PostLikeEntity> likeWrapper = new LambdaQueryWrapper<>();
      likeWrapper.eq(PostLikeEntity::getPostId, postId).eq(PostLikeEntity::getUserId, userId);
      vo.setLiked(postLikeMapper.selectCount(likeWrapper) > 0);

      LambdaQueryWrapper<PostFavoriteEntity> favoriteWrapper = new LambdaQueryWrapper<>();
      favoriteWrapper.eq(PostFavoriteEntity::getPostId, postId)
          .eq(PostFavoriteEntity::getUserId, userId);
      vo.setFavorited(postFavoriteMapper.selectCount(favoriteWrapper) > 0);
    }

    return vo;
  }

  @Override
  public void deletePost(Long postId) {
    softDeletePost(postId);
  }

  @Override
  public void reviewPost(Long postId, Integer status) {
    ensureAdmin();
    PostEntity entity = getExistingPost(postId, true);
    ContentStatusEnum statusEnum = ContentStatusEnum.fromCode(status);
    if (statusEnum == null) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }
    entity.setStatus(statusEnum);
    postMapper.updateById(entity);
  }

  @Override
  public void softDeletePost(Long postId) {
    PostEntity entity = getExistingPost(postId, false);
    ensureOwnerOrAdmin(entity);
    postMapper.deleteById(postId);
  }

  @Override
  public void restorePost(Long postId) {
    ensureAdmin();
    int updated = postMapper.restoreById(postId);
    if (updated <= 0) {
      throw new BusinessException(ErrorCode.POST_NOT_FOUND);
    }
  }

  private Page<PostListItemVO> queryPostPage(Long page, Long size, String keyword, Long categoryId,
      String timeRange,
      ContentStatusEnum status, LogicDeleteEnum deleted, Long authorId,
      boolean includeModerationFields) {
    Page<PostEntity> pageParam = new Page<>(page, size);
    LocalDateTime startTime = TimeRangeEnum.fromCode(timeRange).resolveStartTime();

    LambdaQueryWrapper<PostEntity> wrapper = new LambdaQueryWrapper<>();
    if (status != null) {
      wrapper.eq(PostEntity::getStatus, status);
    }
    if (deleted != null) {
      wrapper.eq(PostEntity::getDeleted, deleted);
    }
    if (StringUtils.hasText(keyword)) {
      wrapper.like(PostEntity::getTitle, keyword);
    }
    if (categoryId != null) {
      wrapper.eq(PostEntity::getCategoryId, categoryId);
    }
    if (authorId != null) {
      wrapper.eq(PostEntity::getAuthorId, authorId);
    }
    if (startTime != null) {
      wrapper.ge(PostEntity::getCreateTime, startTime);
    }
    wrapper.orderByDesc(PostEntity::getIsTop)
        .orderByDesc(PostEntity::getCreateTime);

    Page<PostEntity> entityPage = postMapper.selectPage(pageParam, wrapper);
    return convertToPostListPage(entityPage, page, size, includeModerationFields);
  }

  private Page<PostListItemVO> convertToPostListPage(Page<PostEntity> entityPage, Long page, Long size,
      boolean includeModerationFields) {
    List<PostEntity> records = entityPage.getRecords();
    if (records.isEmpty()) {
      Page<PostListItemVO> empty = new Page<>(page, size, entityPage.getTotal());
      empty.setRecords(Collections.emptyList());
      return empty;
    }

    Map<Long, String> authorNameMap = loadAuthorNameMap(records);
    Map<Long, String> categoryNameMap = loadCategoryNameMap(records);

    List<PostListItemVO> voList = records.stream().map(entity -> {
      PostListItemVO vo = new PostListItemVO();
      BeanUtils.copyProperties(entity, vo);
      String categoryName = categoryNameMap.get(entity.getCategoryId());
      if (categoryName == null && entity.getCategoryId() != null) {
        categoryName = PostCategoryEnum.getDescByCode(entity.getCategoryId().intValue());
      }
      vo.setCategoryName(categoryName);
      vo.setAuthorName(authorNameMap.getOrDefault(entity.getAuthorId(), "用户" + entity.getAuthorId()));
      vo.setSummary(buildSummary(entity.getContent(), 96));
      vo.setFavoriteCount(getPostFavoriteCount(entity.getId()));
      vo.setLikeCount(getPostLikeCount(entity.getId(), entity.getLikeCount()));
      if (includeModerationFields) {
        vo.setStatus(entity.getStatus() == null ? null : entity.getStatus().getCode());
        vo.setDeleted(entity.getDeleted() == null ? null : entity.getDeleted().getCode());
      }
      return vo;
    }).collect(Collectors.toList());

    Page<PostListItemVO> result = new Page<>(page, size, entityPage.getTotal());
    result.setRecords(voList);
    return result;
  }

  private PostEntity getExistingPost(Long postId, boolean includeDeleted) {
    if (postId == null) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }
    PostEntity entity = includeDeleted ? postMapper.selectByIdIncludingDeleted(postId) : postMapper.selectById(postId);
    if (entity == null) {
      throw new BusinessException(ErrorCode.POST_NOT_FOUND);
    }
    return entity;
  }

  private UserContext requireLogin() {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }
    return user;
  }

  private void ensureAdmin() {
    UserContext user = requireLogin();
    if (user.getRole() == null) {
      throw new BusinessException(ErrorCode.ADMIN_FORBIDDEN);
    }
    boolean admin = UserRoleEnum.ADMIN.getDescription().equals(user.getRole())
        || String.valueOf(UserRoleEnum.ADMIN.getCode()).equals(user.getRole());
    if (!admin) {
      throw new BusinessException(ErrorCode.ADMIN_FORBIDDEN);
    }
  }

  private void ensureOwnerOrAdmin(PostEntity entity) {
    UserContext user = requireLogin();
    boolean owner = entity.getAuthorId() != null && entity.getAuthorId().equals(user.getUserId());
    boolean admin = user.getRole() != null && (UserRoleEnum.ADMIN.getDescription().equals(user.getRole())
        || String.valueOf(UserRoleEnum.ADMIN.getCode()).equals(user.getRole()));
    if (!owner && !admin) {
      throw new BusinessException(ErrorCode.POST_FORBIDDEN);
    }
  }

  private Map<Long, String> loadAuthorNameMap(List<PostEntity> records) {
    List<Long> authorIds = records.stream().map(PostEntity::getAuthorId).distinct().collect(Collectors.toList());
    if (authorIds.isEmpty()) {
      return Collections.emptyMap();
    }
    return userMapper.selectBatchIds(authorIds).stream().collect(Collectors.toMap(
        UserEntity::getId,
        item -> item.getUsername() == null ? "用户" + item.getId() : item.getUsername(),
        (left, right) -> left
    ));
  }

  private String loadAuthorName(Long authorId) {
    if (authorId == null) {
      return null;
    }
    UserEntity author = userMapper.selectById(authorId);
    if (author == null || !StringUtils.hasText(author.getUsername())) {
      return "用户" + authorId;
    }
    return author.getUsername();
  }

  private Map<Long, String> loadCategoryNameMap(List<PostEntity> records) {
    List<Long> categoryIds = records.stream().map(PostEntity::getCategoryId).distinct().collect(Collectors.toList());
    if (categoryIds.isEmpty()) {
      return Collections.emptyMap();
    }
    return postCategoryMapper.selectBatchIds(categoryIds).stream().collect(Collectors.toMap(
        PostCategoryEntity::getId,
        PostCategoryEntity::getName,
        (left, right) -> left
    ));
  }

  /**
   * 获取帖子点赞数量，优先从Redis Set读取（存储点赞用户ID），Redis无值则回退到数据库冗余字段并写入Redis
   */
  private Integer getPostLikeCount(Long postId, Integer fallback) {
    String key = POST_LIKE_KEY + postId;
    Long size = stringRedisTemplate.opsForSet().size(key);
    if (size != null && size > 0) {
      return size.intValue();
    }
    int count = fallback == null ? 0 : fallback;
    // 初始化一个空的Set时不需要写入成员，仅在有实际点赞行为时由点赞逻辑写入
    return count;
  }

  private Integer getPostFavoriteCount(Long postId) {
    if (postId == null) {
      return 0;
    }
    LambdaQueryWrapper<PostFavoriteEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostFavoriteEntity::getPostId, postId);
    Long count = postFavoriteMapper.selectCount(wrapper);
    return count == null ? 0 : count.intValue();
  }

  private String buildSummary(String content, int maxLen) {
    if (!StringUtils.hasText(content)) {
      return null;
    }
    String normalized = content.replaceAll("\\s+", " ").trim();
    if (normalized.length() <= maxLen) {
      return normalized;
    }
    return normalized.substring(0, maxLen) + "...";
  }
}
