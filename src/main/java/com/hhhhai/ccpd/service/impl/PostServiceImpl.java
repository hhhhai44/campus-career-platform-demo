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
import com.hhhhai.ccpd.exception.BusinessException;
import com.hhhhai.ccpd.dto.forum.PostCreateDTO;
import com.hhhhai.ccpd.entity.forum.PostCategoryEntity;
import com.hhhhai.ccpd.entity.forum.PostCommentEntity;
import com.hhhhai.ccpd.entity.forum.PostEntity;
import com.hhhhai.ccpd.entity.forum.PostFavoriteEntity;
import com.hhhhai.ccpd.entity.forum.PostLikeEntity;
import com.hhhhai.ccpd.mapper.PostCategoryMapper;
import com.hhhhai.ccpd.mapper.PostMapper;
import com.hhhhai.ccpd.mapper.PostCommentMapper;
import com.hhhhai.ccpd.mapper.PostFavoriteMapper;
import com.hhhhai.ccpd.mapper.PostLikeMapper;
import com.hhhhai.ccpd.mapper.UserMapper;
import com.hhhhai.ccpd.entity.user.UserEntity;
import com.hhhhai.ccpd.service.PostService;
import com.hhhhai.ccpd.vo.forum.PostDetailVO;
import com.hhhhai.ccpd.vo.forum.PostListItemVO;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

  @Resource
  private PostCommentMapper postCommentMapper;

  @Override
  public Long createPost(PostCreateDTO dto) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
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
  public Page<PostListItemVO> pagePostList(Long page, Long size, String keyword, Long categoryId) {
    Page<PostEntity> pageParam = new Page<>(page, size);

    LambdaQueryWrapper<PostEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostEntity::getStatus, ContentStatusEnum.NORMAL);
    wrapper.eq(PostEntity::getDeleted, LogicDeleteEnum.NOT_DELETED);
    if (StringUtils.hasText(keyword)) {
      wrapper.like(PostEntity::getTitle, keyword);
    }
    if (categoryId != null) {
      wrapper.eq(PostEntity::getCategoryId, categoryId);
    }
    wrapper.orderByDesc(PostEntity::getIsTop)
        .orderByDesc(PostEntity::getCreateTime);

    Page<PostEntity> entityPage = postMapper.selectPage(pageParam, wrapper);

    List<PostEntity> records = entityPage.getRecords();
    if (records.isEmpty()) {
      return new Page<>(page, size);
    }

    Map<Long, Long> commentCountMap = loadPostCommentCountMap(records);

    // 批量查询分类名称
    List<Long> categoryIds =
        records.stream().map(PostEntity::getCategoryId).distinct().collect(Collectors.toList());
    Map<Long, String> categoryNameMap =
        postCategoryMapper.selectBatchIds(categoryIds).stream()
            .collect(Collectors.toMap(PostCategoryEntity::getId, PostCategoryEntity::getName));

    // 批量查询作者信息
    List<Long> authorIds = records.stream()
        .map(PostEntity::getAuthorId)
        .distinct()
        .collect(Collectors.toList());
    final Map<Long, String> authorNameMap;
    if (!authorIds.isEmpty()) {
      List<UserEntity> authors = userMapper.selectBatchIds(authorIds);
      authorNameMap = authors.stream()
          .collect(Collectors.toMap(UserEntity::getId,
              u -> u.getRealName() != null && !u.getRealName().trim().isEmpty()
                  ? u.getRealName() : u.getUsername()));
    } else {
      authorNameMap = new HashMap<>();
    }

    // 构建VO并填充点赞数量（从Redis获取）
    List<PostListItemVO> voList =
        records.stream()
            .map(
                entity -> {
                  PostListItemVO vo = new PostListItemVO();
                  BeanUtils.copyProperties(entity, vo);
                  String categoryName = categoryNameMap.get(entity.getCategoryId());
                  if (categoryName == null && entity.getCategoryId() != null) {
                    categoryName = PostCategoryEnum.getDescByCode(entity.getCategoryId().intValue());
                  }
                  vo.setCategoryName(categoryName);
                  vo.setSummary(buildSummary(entity.getContent(), 96));
                  vo.setFavoriteCount(getPostFavoriteCount(entity.getId()));
                  vo.setAuthorName(authorNameMap.getOrDefault(entity.getAuthorId(), "未知用户"));
                  vo.setLikeCount(getPostLikeCount(entity.getId(), entity.getLikeCount()));
                  vo.setCommentCount(commentCountMap.getOrDefault(entity.getId(), 0L).intValue());
                  return vo;
                })
            .collect(Collectors.toList());

    Page<PostListItemVO> result = new Page<>(page, size, entityPage.getTotal());
    result.setRecords(voList);
    return result;
  }

  @Override
  public PostDetailVO getPostDetail(Long postId) {
    PostEntity entity = postMapper.selectById(postId);
    if (entity == null || entity.getDeleted() == LogicDeleteEnum.DELETED) {
      return null;
    }

    // 仅自增浏览量，避免 updateById 覆盖其他并发更新字段（如 comment_count）
    postMapper.incrementViewCount(postId);
    entity = postMapper.selectById(postId);

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

    UserContext user = UserContextHolder.getUser();
    if (user != null && user.getUserId() != null) {
      Long userId = user.getUserId();

      LambdaQueryWrapper<PostLikeEntity> likeWrapper = new LambdaQueryWrapper<>();
      likeWrapper.eq(PostLikeEntity::getPostId, postId).eq(PostLikeEntity::getUserId, userId);
      vo.setLiked(postLikeMapper.selectCount(likeWrapper) > 0);

      LambdaQueryWrapper<PostFavoriteEntity> favoriteWrapper = new LambdaQueryWrapper<>();
      favoriteWrapper.eq(PostFavoriteEntity::getPostId, postId)
          .eq(PostFavoriteEntity::getUserId, userId);
      vo.setFavorited(postFavoriteMapper.selectCount(favoriteWrapper) > 0);
    }

    // 查询作者信息
    if (entity.getAuthorId() != null) {
      UserEntity author = userMapper.selectById(entity.getAuthorId());
      if (author != null) {
        vo.setAuthorName(author.getRealName() != null && !author.getRealName().trim().isEmpty()
            ? author.getRealName() : author.getUsername());
      } else {
        vo.setAuthorName("未知用户");
      }
    } else {
      vo.setAuthorName("未知用户");
    }

    // 设置当前登录用户ID（复用上面已获取的user变量）
    if (user != null && user.getUserId() != null) {
      vo.setCurrentUserId(user.getUserId());
    }

    return vo;
  }

  @Override
  @Transactional
  public void deletePost(Long postId) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }

    PostEntity post = postMapper.selectById(postId);
    if (post == null || post.getDeleted() == LogicDeleteEnum.DELETED) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }

    // 只能删除自己的帖子
    if (!post.getAuthorId().equals(user.getUserId())) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }

    // 逻辑删除帖子
    post.setDeleted(LogicDeleteEnum.DELETED);
    postMapper.updateById(post);
  }

  @Override
  public Page<PostListItemVO> pageMyPosts(Long page, Long size) {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }

    Page<PostEntity> pageParam = new Page<>(page, size);

    LambdaQueryWrapper<PostEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostEntity::getAuthorId, user.getUserId())
        .eq(PostEntity::getDeleted, LogicDeleteEnum.NOT_DELETED)
        .orderByDesc(PostEntity::getCreateTime);

    Page<PostEntity> entityPage = postMapper.selectPage(pageParam, wrapper);

    List<PostEntity> records = entityPage.getRecords();
    if (records.isEmpty()) {
      return new Page<>(page, size);
    }

    Map<Long, Long> commentCountMap = loadPostCommentCountMap(records);

    // 批量查询分类名称
    List<Long> categoryIds =
        records.stream().map(PostEntity::getCategoryId).distinct().collect(Collectors.toList());
    Map<Long, String> categoryNameMap =
        postCategoryMapper.selectBatchIds(categoryIds).stream()
            .collect(Collectors.toMap(PostCategoryEntity::getId, PostCategoryEntity::getName));

    // 构建VO
    List<PostListItemVO> voList =
        records.stream()
            .map(
                entity -> {
                  PostListItemVO vo = new PostListItemVO();
                  BeanUtils.copyProperties(entity, vo);
                  String categoryName = categoryNameMap.get(entity.getCategoryId());
                  if (categoryName == null && entity.getCategoryId() != null) {
                    categoryName = PostCategoryEnum.getDescByCode(entity.getCategoryId().intValue());
                  }
                  vo.setCategoryName(categoryName);
                  vo.setSummary(buildSummary(entity.getContent(), 96));
                  vo.setFavoriteCount(getPostFavoriteCount(entity.getId()));
                  vo.setAuthorName(user.getUsername());
                  vo.setLikeCount(getPostLikeCount(entity.getId(), entity.getLikeCount()));
                  vo.setCommentCount(commentCountMap.getOrDefault(entity.getId(), 0L).intValue());
                  return vo;
                })
            .collect(Collectors.toList());

    Page<PostListItemVO> result = new Page<>(page, size, entityPage.getTotal());
    result.setRecords(voList);
    return result;
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

  private Map<Long, Long> loadPostCommentCountMap(List<PostEntity> posts) {
    List<Long> postIds = posts.stream().map(PostEntity::getId).collect(Collectors.toList());
    if (postIds.isEmpty()) {
      return new HashMap<>();
    }
    return postCommentMapper.selectList(
            new LambdaQueryWrapper<PostCommentEntity>()
                .in(PostCommentEntity::getPostId, postIds)
                .eq(PostCommentEntity::getStatus, ContentStatusEnum.NORMAL))
        .stream()
        .collect(Collectors.groupingBy(PostCommentEntity::getPostId, Collectors.counting()));
  }
}
