package com.hhhhai.ccpd.service.impl;

import static com.hhhhai.ccpd.common.constant.RedisConstants.POST_LIKE_KEY;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import com.hhhhai.ccpd.common.enums.ContentStatusEnum;
import com.hhhhai.ccpd.common.enums.LogicDeleteEnum;
import com.hhhhai.ccpd.common.enums.PostCategoryEnum;
import com.hhhhai.ccpd.dto.forum.PostCreateDTO;
import com.hhhhai.ccpd.entity.forum.PostCategoryEntity;
import com.hhhhai.ccpd.entity.forum.PostEntity;
import com.hhhhai.ccpd.entity.forum.PostFavoriteEntity;
import com.hhhhai.ccpd.entity.forum.PostLikeEntity;
import com.hhhhai.ccpd.mapper.PostCategoryMapper;
import com.hhhhai.ccpd.mapper.PostMapper;
import com.hhhhai.ccpd.mapper.PostFavoriteMapper;
import com.hhhhai.ccpd.mapper.PostLikeMapper;
import com.hhhhai.ccpd.service.PostService;
import com.hhhhai.ccpd.vo.forum.PostDetailVO;
import com.hhhhai.ccpd.vo.forum.PostListItemVO;
import jakarta.annotation.Resource;
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

    // 批量查询分类名称
    List<Long> categoryIds =
        records.stream().map(PostEntity::getCategoryId).distinct().collect(Collectors.toList());
    Map<Long, String> categoryNameMap =
        postCategoryMapper.selectBatchIds(categoryIds).stream()
            .collect(Collectors.toMap(PostCategoryEntity::getId, PostCategoryEntity::getName));

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
                  // TODO: 这里可以补充作者昵称（需要用户资料模块）
                  vo.setAuthorName(null);
                  vo.setLikeCount(getPostLikeCount(entity.getId(), entity.getLikeCount()));
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

    // 累计浏览量（简单处理：数据库字段自增）
    entity.setViewCount(entity.getViewCount() == null ? 1 : entity.getViewCount() + 1);
    postMapper.updateById(entity);

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

    // TODO: 这里可以补充作者昵称（需要用户资料模块）
    vo.setAuthorName(null);

    return vo;
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
