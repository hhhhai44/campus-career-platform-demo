package com.hhhhai.ccpd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhhhai.ccpd.entity.resource.ResourceCommentLikeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资源评论点赞 Mapper
 */
@Mapper
public interface ResourceCommentLikeMapper extends BaseMapper<ResourceCommentLikeEntity> {
}

