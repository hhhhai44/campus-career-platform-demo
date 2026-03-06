package com.hhhhai.ccpd.service;

import com.hhhhai.ccpd.vo.CategoryListItemVO;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 帖子分类业务接口
 */
@Service
public interface PostCategoryService {

  /**
   * 查询所有启用分类（按 sort 升序），优先 Redis 缓存
   */
  List<CategoryListItemVO> listEnabled();
}







