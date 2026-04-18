package com.hhhhai.ccpd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import com.hhhhai.ccpd.common.enums.ErrorCode;
import com.hhhhai.ccpd.common.enums.UserRoleEnum;
import com.hhhhai.ccpd.common.enums.UserStatusEnum;
import com.hhhhai.ccpd.entity.user.UserEntity;
import com.hhhhai.ccpd.exception.BusinessException;
import com.hhhhai.ccpd.mapper.UserMapper;
import com.hhhhai.ccpd.service.UserAdminService;
import com.hhhhai.ccpd.vo.user.UserAdminDetailVO;
import com.hhhhai.ccpd.vo.user.UserAdminListVO;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserAdminServiceImpl implements UserAdminService {

  @Resource
  private UserMapper userMapper;

  @Override
  public Page<UserAdminListVO> pageUsers(Long page, Long size, String keyword, Integer role, Integer status) {
    ensureAdmin();
    Page<UserEntity> pageParam = new Page<>(page, size);
    LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(keyword)) {
      wrapper.like(UserEntity::getUsername, keyword);
    }
    if (role != null) {
      UserRoleEnum roleEnum = UserRoleEnum.fromCode(role);
      if (roleEnum != null) {
        wrapper.eq(UserEntity::getRole, roleEnum);
      }
    }
    if (status != null) {
      UserStatusEnum statusEnum = UserStatusEnum.fromCode(status);
      if (statusEnum != null) {
        wrapper.eq(UserEntity::getStatus, statusEnum);
      }
    }
    wrapper.orderByDesc(UserEntity::getCreateTime);

    Page<UserEntity> entityPage = userMapper.selectPage(pageParam, wrapper);
    List<UserAdminListVO> voList = entityPage.getRecords().stream().map(this::toListVO)
        .collect(Collectors.toList());
    Page<UserAdminListVO> result = new Page<>(page, size, entityPage.getTotal());
    result.setRecords(voList);
    return result;
  }

  @Override
  public UserAdminDetailVO getUserDetail(Long userId) {
    ensureAdmin();
    UserEntity entity = userMapper.selectById(userId);
    if (entity == null) {
      throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
    UserAdminDetailVO vo = new UserAdminDetailVO();
    BeanUtils.copyProperties(entity, vo);
    vo.setRole(entity.getRole() == null ? null : entity.getRole().getCode());
    vo.setRoleDesc(entity.getRole() == null ? null : entity.getRole().getDescription());
    vo.setStatus(entity.getStatus() == null ? null : entity.getStatus().getCode());
    vo.setStatusDesc(entity.getStatus() == null ? null : entity.getStatus().getDescription());
    return vo;
  }

  @Override
  public void updateUserStatus(Long userId, Integer status) {
    ensureAdmin();
    UserEntity entity = userMapper.selectById(userId);
    if (entity == null) {
      throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
    UserStatusEnum target = UserStatusEnum.fromCode(status);
    if (target == null) {
      throw new BusinessException(ErrorCode.USER_STATUS_INVALID);
    }
    entity.setStatus(target);
    userMapper.updateById(entity);
  }

  private UserAdminListVO toListVO(UserEntity entity) {
    UserAdminListVO vo = new UserAdminListVO();
    BeanUtils.copyProperties(entity, vo);
    vo.setRole(entity.getRole() == null ? null : entity.getRole().getCode());
    vo.setRoleDesc(entity.getRole() == null ? null : entity.getRole().getDescription());
    vo.setStatus(entity.getStatus() == null ? null : entity.getStatus().getCode());
    vo.setStatusDesc(entity.getStatus() == null ? null : entity.getStatus().getDescription());
    return vo;
  }

  private void ensureAdmin() {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }
    String role = user.getRole();
    boolean isAdmin = UserRoleEnum.ADMIN.getDescription().equals(role)
        || String.valueOf(UserRoleEnum.ADMIN.getCode()).equals(role);
    if (!isAdmin) {
      throw new BusinessException(ErrorCode.ADMIN_FORBIDDEN);
    }
  }
}


