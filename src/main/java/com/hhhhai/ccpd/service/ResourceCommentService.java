package com.hhhhai.ccpd.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.dto.resource.ResourceCommentCreateDTO;
import com.hhhhai.ccpd.vo.resource.ResourceCommentVO;

/**
 * 资源评论业务接口
 */
public interface ResourceCommentService {

  Long createComment(ResourceCommentCreateDTO dto);

  Page<ResourceCommentVO> pageComments(Long resourceId, Long page, Long size);

  void deleteComment(Long commentId);
}

