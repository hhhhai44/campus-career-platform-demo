package com.hhhhai.ccpd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhhhai.ccpd.entity.forum.PostCategoryEntity;
import com.hhhhai.ccpd.mapper.PostCategoryMapper;
import com.hhhhai.ccpd.service.PostCategoryService;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 帖子分类业务实现
 */
@Service
public class PostCategoryServiceImpl implements PostCategoryService {

  @Resource
  private PostCategoryMapper postCategoryMapper;

  @Override
  public List<PostCategoryEntity> listAll() {
    LambdaQueryWrapper<PostCategoryEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PostCategoryEntity::getDeleted, 0)
        .orderByAsc(PostCategoryEntity::getSort);
    return postCategoryMapper.selectList(wrapper);
  }
}



