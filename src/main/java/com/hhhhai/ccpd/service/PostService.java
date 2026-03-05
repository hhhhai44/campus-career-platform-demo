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
}



