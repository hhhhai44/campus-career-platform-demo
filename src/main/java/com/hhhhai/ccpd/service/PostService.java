package com.hhhhai.ccpd.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.dto.forum.PostCreateDTO;
import com.hhhhai.ccpd.vo.forum.PostDetailVO;
import com.hhhhai.ccpd.vo.forum.PostListItemVO;
import org.springframework.stereotype.Service;

/**
 * 帖子业务接口
 */
@Service
public interface PostService {

  /**
   * 发布帖子
   *
   * @param dto 发帖参数
   * @return 帖子ID
   */
  Long createPost(PostCreateDTO dto);

  /**
   * 分页查询帖子列表
   *
   * @param page 当前页码
   * @param size 每页大小
   * @param keyword 标题关键字（可选）
   * @param categoryId 分类ID（可选）
   * @return 分页结果
   */
  Page<PostListItemVO> pagePostList(Long page, Long size, String keyword, Long categoryId);

  /**
   * 帖子详情
   *
   * @param postId 帖子ID
   * @return 详情
   */
  PostDetailVO getPostDetail(Long postId);

  /**
   * 管理员查看帖子详情（可查看禁用/软删除帖子）
   *
   * @param postId 帖子ID
   * @return 详情
   */
  PostDetailVO getAdminPostDetail(Long postId);

  /**
   * 删除帖子
   *
   * @param postId 帖子ID
   */
  void deletePost(Long postId);

  /**
   * 查询当前用户发布的帖子列表
   *
   * @param page 当前页码
   * @param size 每页大小
   * @return 分页结果
   */
  Page<PostListItemVO> pageMyPosts(Long page, Long size);

  /**
   * 管理员分页查询帖子列表
   *
   * @param page 当前页码
   * @param size 每页大小
   * @param keyword 标题关键字
   * @param categoryId 分类ID
   * @param status 帖子状态
   * @param deleted 逻辑删除标记
   * @return 分页结果
   */
  Page<PostListItemVO> pageAdminPosts(Long page, Long size, String keyword, Long categoryId,
      Integer status, Integer deleted);

  /**
   * 管理员审核帖子状态
   */
  void reviewPost(Long postId, Integer status);

  /**
   * 管理员软删除帖子
   */
  void softDeletePost(Long postId);

  /**
   * 管理员恢复帖子
   */
  void restorePost(Long postId);
}







