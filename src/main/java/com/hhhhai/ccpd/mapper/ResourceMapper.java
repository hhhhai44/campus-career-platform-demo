package com.hhhhai.ccpd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhhhai.ccpd.entity.resource.ResourceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 资源 Mapper
 */
@Mapper
public interface ResourceMapper extends BaseMapper<ResourceEntity> {

  @Update("UPDATE resource SET like_count = IFNULL(like_count, 0) + 1 WHERE id = #{resourceId}")
  int incrementLikeCount(@Param("resourceId") Long resourceId);

  @Update("UPDATE resource SET like_count = GREATEST(IFNULL(like_count, 0) - 1, 0) WHERE id = #{resourceId}")
  int decrementLikeCount(@Param("resourceId") Long resourceId);

  @Update("UPDATE resource SET favorite_count = IFNULL(favorite_count, 0) + 1 WHERE id = #{resourceId}")
  int incrementFavoriteCount(@Param("resourceId") Long resourceId);

  @Update("UPDATE resource SET favorite_count = GREATEST(IFNULL(favorite_count, 0) - 1, 0) WHERE id = #{resourceId}")
  int decrementFavoriteCount(@Param("resourceId") Long resourceId);

  @Update("UPDATE resource SET download_count = IFNULL(download_count, 0) + 1 WHERE id = #{resourceId}")
  int incrementDownloadCount(@Param("resourceId") Long resourceId);
}
