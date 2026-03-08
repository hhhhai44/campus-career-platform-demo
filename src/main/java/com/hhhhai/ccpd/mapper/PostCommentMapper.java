package com.hhhhai.ccpd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhhhai.ccpd.entity.forum.PostCommentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 帖子评论 Mapper
 */
@Mapper
public interface PostCommentMapper extends BaseMapper<PostCommentEntity> {

  @Update("UPDATE post_comment SET like_count = IFNULL(like_count, 0) + 1 WHERE id = #{commentId}")
  int incrementLikeCount(@Param("commentId") Long commentId);

  @Update("UPDATE post_comment SET like_count = GREATEST(IFNULL(like_count, 0) - 1, 0) WHERE id = #{commentId}")
  int decrementLikeCount(@Param("commentId") Long commentId);
}
