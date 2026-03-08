package com.hhhhai.ccpd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhhhai.ccpd.entity.forum.PostCommentLikeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子评论点赞 Mapper
 */
@Mapper
public interface PostCommentLikeMapper extends BaseMapper<PostCommentLikeEntity> {
}

