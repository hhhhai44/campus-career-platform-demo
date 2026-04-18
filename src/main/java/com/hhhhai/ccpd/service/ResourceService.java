package com.hhhhai.ccpd.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.dto.resource.ResourceUploadDTO;
import com.hhhhai.ccpd.vo.forum.FavoriteToggleVO;
import com.hhhhai.ccpd.vo.forum.LikeToggleVO;
import com.hhhhai.ccpd.vo.resource.ResourceDetailVO;
import com.hhhhai.ccpd.vo.resource.ResourceListItemVO;
import org.springframework.stereotype.Service;

/**
 * 资源业务接口
 */
@Service
public interface ResourceService {

  /**
   * 上传资源
   *
   * @param dto 上传参数
   * @return 资源ID
   */
  Long upload(ResourceUploadDTO dto);

  /**
   * 资源分页列表 + 搜索
   *
   * @param page 页码
   * @param size 每页大小
   * @param keyword 标题或描述关键字（可选）
   * @param categoryId 分类ID（可选）
   * @param timeRange 时间范围（可选）
   * @return 分页结果
   */
  Page<ResourceListItemVO> pageList(Long page, Long size, String keyword, Long categoryId,
      String timeRange);

  /**
   * 当前用户上传的资源分页
   */
  Page<ResourceListItemVO> pageMyResources(Long page, Long size);

  /**
   * 资源详情
   */
  ResourceDetailVO getDetail(Long resourceId);

  /**
   * 点赞/取消点赞
   */
  LikeToggleVO toggleLike(Long resourceId);

  /**
   * 收藏/取消收藏
   */
  FavoriteToggleVO toggleFavorite(Long resourceId);
}
