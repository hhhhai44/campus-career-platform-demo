package com.hhhhai.ccpd.service;

import com.hhhhai.ccpd.entity.forum.PostCategoryEntity;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 帖子分类业务接口
 */
@Service
public interface PostCategoryService {

  /**
   * 查询所有分类（按 sort 升序）
   */
  List<PostCategoryEntity> listAll();
}



