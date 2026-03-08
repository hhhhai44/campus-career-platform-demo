package com.hhhhai.ccpd.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.dto.forum.PostCommentCreateDTO;
import com.hhhhai.ccpd.vo.forum.LikeToggleVO;
import com.hhhhai.ccpd.vo.forum.PostCommentVO;
import org.springframework.stereotype.Service;

/**
 * 帖子评论业务接口
 */
@Service
public interface PostCommentService {

  /**
   * 发表评论 / 回复
   */
  Long createComment(PostCommentCreateDTO dto);

  /**
   * 分页查询某个帖子的评论（包含二级评论）
   */
  Page<PostCommentVO> pageComments(Long postId, Long page, Long size);

  /**
   * 删除评论
   */
  void deleteComment(Long commentId);

  /**
   * 评论点赞/取消点赞
   */
  LikeToggleVO toggleLike(Long commentId);
}
