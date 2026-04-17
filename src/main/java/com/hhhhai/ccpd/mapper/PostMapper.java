package com.hhhhai.ccpd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.entity.forum.PostEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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

  @Select(
      "<script>"
          + "SELECT * FROM post WHERE id = #{postId} LIMIT 1"
          + "</script>")
  PostEntity selectByIdIncludingDeleted(@Param("postId") Long postId);

  @Update("UPDATE post SET deleted = 0 WHERE id = #{postId} AND deleted = 1")
  int restoreById(@Param("postId") Long postId);

  @Select(
      "<script>"
          + "SELECT * FROM post WHERE 1=1 "
          + "<if test='keyword != null and keyword != \"\"'> AND title LIKE CONCAT('%', #{keyword}, '%')</if>"
          + "<if test='categoryId != null'> AND category_id = #{categoryId}</if>"
          + "<if test='status != null'> AND status = #{status}</if>"
          + "<if test='deleted != null'> AND deleted = #{deleted}</if>"
          + "ORDER BY is_top DESC, create_time DESC"
          + "</script>")
  List<PostEntity> selectAdminPage(
      Page<PostEntity> page,
      @Param("keyword") String keyword,
      @Param("categoryId") Long categoryId,
      @Param("status") Integer status,
      @Param("deleted") Integer deleted);

  @Select(
      "<script>"
          + "SELECT COUNT(1) FROM post WHERE 1=1 "
          + "<if test='keyword != null and keyword != \"\"'> AND title LIKE CONCAT('%', #{keyword}, '%')</if>"
          + "<if test='categoryId != null'> AND category_id = #{categoryId}</if>"
          + "<if test='status != null'> AND status = #{status}</if>"
          + "<if test='deleted != null'> AND deleted = #{deleted}</if>"
          + "</script>")
  Long countAdminPage(
      @Param("keyword") String keyword,
      @Param("categoryId") Long categoryId,
      @Param("status") Integer status,
      @Param("deleted") Integer deleted);
}

