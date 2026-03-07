package com.hhhhai.ccpd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhhhai.ccpd.entity.forum.PostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 帖子 Mapper
 */
@Mapper
public interface PostMapper extends BaseMapper<PostEntity> {

  @Update("UPDATE post SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{postId}")
  int incrementViewCount(@Param("postId") Long postId);

  @Update("UPDATE post SET comment_count = #{commentCount} WHERE id = #{postId}")
  int updateCommentCount(@Param("postId") Long postId, @Param("commentCount") Integer commentCount);
}
