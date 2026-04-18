package com.hhhhai.ccpd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import com.hhhhai.ccpd.common.enums.ContentStatusEnum;
import com.hhhhai.ccpd.common.enums.ErrorCode;
import com.hhhhai.ccpd.common.enums.LogicDeleteEnum;
import com.hhhhai.ccpd.common.enums.ReportBizTypeEnum;
import com.hhhhai.ccpd.common.enums.ReportStatusEnum;
import com.hhhhai.ccpd.common.enums.UserRoleEnum;
import com.hhhhai.ccpd.dto.report.ReportCreateDTO;
import com.hhhhai.ccpd.dto.report.ReportHandleDTO;
import com.hhhhai.ccpd.entity.forum.PostCommentEntity;
import com.hhhhai.ccpd.entity.forum.PostEntity;
import com.hhhhai.ccpd.entity.report.ReportEntity;
import com.hhhhai.ccpd.entity.resource.ResourceCommentEntity;
import com.hhhhai.ccpd.entity.resource.ResourceEntity;
import com.hhhhai.ccpd.entity.user.UserEntity;
import com.hhhhai.ccpd.exception.BusinessException;
import com.hhhhai.ccpd.mapper.PostCommentMapper;
import com.hhhhai.ccpd.mapper.PostMapper;
import com.hhhhai.ccpd.mapper.ReportMapper;
import com.hhhhai.ccpd.mapper.ResourceCommentMapper;
import com.hhhhai.ccpd.mapper.ResourceMapper;
import com.hhhhai.ccpd.mapper.UserMapper;
import com.hhhhai.ccpd.service.ReportService;
import com.hhhhai.ccpd.vo.report.ReportDetailVO;
import com.hhhhai.ccpd.vo.report.ReportListVO;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ReportServiceImpl implements ReportService {

  @Resource
  private ReportMapper reportMapper;

  @Resource
  private PostMapper postMapper;

  @Resource
  private ResourceMapper resourceMapper;

  @Resource
  private PostCommentMapper postCommentMapper;

  @Resource
  private ResourceCommentMapper resourceCommentMapper;

  @Resource
  private UserMapper userMapper;

  @Override
  public Long createReport(ReportCreateDTO dto) {
    UserContext user = requireLogin();
    ReportBizTypeEnum bizType = ReportBizTypeEnum.fromCode(dto.getBizType());
    if (bizType == null || dto.getBizId() == null || !StringUtils.hasText(dto.getReason())) {
      throw new BusinessException(ErrorCode.REPORT_INVALID);
    }

    TargetInfo targetInfo = loadTargetInfo(bizType, dto.getBizId());
    if (targetInfo == null || targetInfo.ownerId == null) {
      throw new BusinessException(ErrorCode.REPORT_INVALID);
    }

    if (Objects.equals(targetInfo.ownerId, user.getUserId())) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }

    ReportEntity entity = new ReportEntity();
    entity.setBizType(bizType.getCode());
    entity.setBizId(dto.getBizId());
    entity.setBizTitle(targetInfo.title);
    entity.setBizSnippet(targetInfo.snippet);
    entity.setBizOwnerId(targetInfo.ownerId);
    entity.setReporterId(user.getUserId());
    entity.setReason(dto.getReason().trim());
    entity.setDetail(StringUtils.hasText(dto.getDetail()) ? dto.getDetail().trim() : null);
    entity.setStatus(ReportStatusEnum.PENDING.getCode());
    reportMapper.insert(entity);
    return entity.getId();
  }

  @Override
  public Page<ReportListVO> pageAdminReports(Long page, Long size, String bizType, Integer status,
      String keyword) {
    ensureAdmin();
    Page<ReportEntity> pageParam = new Page<>(page, size);
    LambdaQueryWrapper<ReportEntity> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(bizType)) {
      wrapper.eq(ReportEntity::getBizType, bizType.trim());
    }
    if (status != null) {
      wrapper.eq(ReportEntity::getStatus, status);
    }
    if (StringUtils.hasText(keyword)) {
      wrapper.and(w -> w.like(ReportEntity::getReason, keyword)
          .or().like(ReportEntity::getBizTitle, keyword)
          .or().like(ReportEntity::getDetail, keyword));
    }
    wrapper.orderByDesc(ReportEntity::getCreateTime);

    Page<ReportEntity> entityPage = reportMapper.selectPage(pageParam, wrapper);
    List<ReportEntity> records = entityPage.getRecords();
    if ((entityPage.getTotal() <= 0) && records != null && !records.isEmpty()) {
      Long count = reportMapper.selectCount(wrapper);
      entityPage.setTotal(count == null ? 0 : count);
    }
    if (records == null || records.isEmpty()) {
      return new Page<>(page, size, entityPage.getTotal());
    }

    Map<Long, String> userNameMap = buildUserNameMap(records);
    List<ReportListVO> voList = records.stream()
        .map(item -> toListVO(item, userNameMap))
        .collect(Collectors.toList());

    Page<ReportListVO> result = new Page<>(page, size, entityPage.getTotal());
    result.setRecords(voList);
    return result;
  }

  @Override
  public ReportDetailVO getAdminReportDetail(Long reportId) {
    ensureAdmin();
    ReportEntity entity = reportMapper.selectById(reportId);
    if (entity == null) {
      throw new BusinessException(ErrorCode.REPORT_NOT_FOUND);
    }
    Map<Long, String> names = loadNames(entity.getReporterId(), entity.getBizOwnerId(), entity.getHandlerId());
    return toDetailVO(entity, names);
  }

  @Override
  public void handleReport(Long reportId, ReportHandleDTO dto) {
    ensureAdmin();
    ReportEntity entity = reportMapper.selectById(reportId);
    if (entity == null) {
      throw new BusinessException(ErrorCode.REPORT_NOT_FOUND);
    }
    if (!Objects.equals(entity.getStatus(), ReportStatusEnum.PENDING.getCode())) {
      throw new BusinessException(ErrorCode.REPORT_HANDLED);
    }

    ReportStatusEnum target = ReportStatusEnum.fromCode(dto.getStatus());
    if (target == null || target == ReportStatusEnum.PENDING) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }

    UserContext user = requireLogin();
    entity.setStatus(target.getCode());
    entity.setHandlerId(user.getUserId());
    entity.setHandleRemark(StringUtils.hasText(dto.getHandleRemark()) ? dto.getHandleRemark().trim() : null);
    reportMapper.updateById(entity);
  }

  private TargetInfo loadTargetInfo(ReportBizTypeEnum bizType, Long bizId) {
    if (bizType == ReportBizTypeEnum.POST) {
      PostEntity post = postMapper.selectByIdIncludingDeleted(bizId);
      if (post == null || post.getDeleted() == LogicDeleteEnum.DELETED) {
        return null;
      }
      return new TargetInfo(post.getAuthorId(), safeText(post.getTitle()), clip(post.getContent()));
    }

    if (bizType == ReportBizTypeEnum.RESOURCE) {
      ResourceEntity resource = resourceMapper.selectById(bizId);
      if (resource == null || resource.getDeleted() == LogicDeleteEnum.DELETED) {
        return null;
      }
      return new TargetInfo(resource.getUploaderId(), safeText(resource.getTitle()), clip(resource.getDescription()));
    }

    if (bizType == ReportBizTypeEnum.POST_COMMENT) {
      PostCommentEntity comment = postCommentMapper.selectById(bizId);
      if (comment == null || comment.getStatus() != ContentStatusEnum.NORMAL) {
        return null;
      }
      return new TargetInfo(comment.getFromUserId(), "帖子评论#" + comment.getId(), clip(comment.getContent()));
    }

    if (bizType == ReportBizTypeEnum.RESOURCE_COMMENT) {
      ResourceCommentEntity comment = resourceCommentMapper.selectById(bizId);
      if (comment == null || comment.getStatus() != ContentStatusEnum.NORMAL) {
        return null;
      }
      return new TargetInfo(comment.getFromUserId(), "资源评论#" + comment.getId(), clip(comment.getContent()));
    }

    return null;
  }

  private String clip(String text) {
    if (!StringUtils.hasText(text)) {
      return "";
    }
    String normalized = text.trim();
    if (normalized.length() <= 120) {
      return normalized;
    }
    return normalized.substring(0, 120) + "...";
  }

  private String safeText(String text) {
    return StringUtils.hasText(text) ? text.trim() : "";
  }

  private ReportListVO toListVO(ReportEntity entity, Map<Long, String> userNameMap) {
    ReportListVO vo = new ReportListVO();
    vo.setId(entity.getId());
    vo.setBizType(entity.getBizType());
    vo.setBizTypeDesc(ReportBizTypeEnum.getDescByCode(entity.getBizType()));
    vo.setBizTitle(entity.getBizTitle());
    vo.setBizOwnerName(userNameMap.getOrDefault(entity.getBizOwnerId(), "未知用户"));
    vo.setReporterName(userNameMap.getOrDefault(entity.getReporterId(), "未知用户"));
    vo.setReason(entity.getReason());
    vo.setStatus(entity.getStatus());
    vo.setStatusDesc(ReportStatusEnum.getDescByCode(entity.getStatus()));
    vo.setCreateTime(entity.getCreateTime());
    return vo;
  }

  private ReportDetailVO toDetailVO(ReportEntity entity, Map<Long, String> userNameMap) {
    ReportDetailVO vo = new ReportDetailVO();
    vo.setId(entity.getId());
    vo.setBizType(entity.getBizType());
    vo.setBizTypeDesc(ReportBizTypeEnum.getDescByCode(entity.getBizType()));
    vo.setBizTitle(entity.getBizTitle());
    vo.setBizOwnerName(userNameMap.getOrDefault(entity.getBizOwnerId(), "未知用户"));
    vo.setReporterName(userNameMap.getOrDefault(entity.getReporterId(), "未知用户"));
    vo.setReason(entity.getReason());
    vo.setStatus(entity.getStatus());
    vo.setStatusDesc(ReportStatusEnum.getDescByCode(entity.getStatus()));
    vo.setCreateTime(entity.getCreateTime());
    vo.setBizSnippet(entity.getBizSnippet());
    vo.setDetail(entity.getDetail());
    vo.setHandleRemark(entity.getHandleRemark());
    return vo;
  }

  private Map<Long, String> buildUserNameMap(List<ReportEntity> records) {
    List<Long> userIds = records.stream()
        .flatMap(item -> java.util.stream.Stream.of(item.getReporterId(), item.getBizOwnerId(), item.getHandlerId()))
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.toList());
    if (userIds.isEmpty()) {
      return new HashMap<>();
    }
    return userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(
        UserEntity::getId,
        this::displayName,
        (a, b) -> a));
  }

  private Map<Long, String> loadNames(Long... ids) {
    List<Long> userIds = java.util.Arrays.stream(ids)
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.toList());
    if (userIds.isEmpty()) {
      return new HashMap<>();
    }
    return userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(
        UserEntity::getId,
        this::displayName,
        (a, b) -> a));
  }

  private String displayName(UserEntity user) {
    if (StringUtils.hasText(user.getRealName())) {
      return user.getRealName().trim();
    }
    return user.getUsername();
  }

  private UserContext requireLogin() {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }
    return user;
  }

  private void ensureAdmin() {
    UserContext user = requireLogin();
    if (!isAdminRole(user.getRole())) {
      throw new BusinessException(ErrorCode.ADMIN_FORBIDDEN);
    }
  }

  private boolean isAdminRole(String role) {
    return UserRoleEnum.ADMIN.getDescription().equals(role)
        || String.valueOf(UserRoleEnum.ADMIN.getCode()).equals(role);
  }

  private static class TargetInfo {

    private final Long ownerId;
    private final String title;
    private final String snippet;

    private TargetInfo(Long ownerId, String title, String snippet) {
      this.ownerId = ownerId;
      this.title = title;
      this.snippet = snippet;
    }
  }
}


