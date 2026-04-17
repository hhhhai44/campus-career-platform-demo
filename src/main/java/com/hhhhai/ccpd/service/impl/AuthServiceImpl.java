package com.hhhhai.ccpd.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import com.hhhhai.ccpd.common.enums.ErrorCode;
import com.hhhhai.ccpd.common.enums.UserRoleEnum;
import com.hhhhai.ccpd.common.enums.UserStatusEnum;
import com.hhhhai.ccpd.dto.LoginDTO;
import com.hhhhai.ccpd.dto.RegisterDTO;
import com.hhhhai.ccpd.entity.user.UserEntity;
import com.hhhhai.ccpd.exception.BusinessException;
import com.hhhhai.ccpd.mapper.UserMapper;
import com.hhhhai.ccpd.security.token.TokenService;
import com.hhhhai.ccpd.service.AuthService;
import com.hhhhai.ccpd.common.utils.PasswordEncoder;
import com.hhhhai.ccpd.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

  @Resource
  private TokenService tokenService;

  @Resource
  private UserMapper userMapper;

  @Resource
  private PasswordEncoder passwordEncoder;

  @Override
  public LoginVO login(LoginDTO request) {
    String username = request.getUsername() == null ? "" : request.getUsername().trim();
    String password = request.getPassword();
    if (username.isEmpty() || password == null || password.isEmpty()) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }

    log.debug("登录请求参数: {}", request);

    UserEntity user = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery()
        .eq(UserEntity::getUsername, username));
    if (user == null) {
      throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }

    if (user.getStatus() != null && !user.getStatus().canLogin()) {
      throw new BusinessException(ErrorCode.USER_BANNED);
    }

    if (!Boolean.TRUE.equals(passwordEncoder.matches(user.getPassword(), password))) {
      throw new BusinessException(ErrorCode.PASSWORD_ERROR);
    }

    user.setLastLoginTime(java.time.LocalDateTime.now());
    userMapper.updateById(user);

    log.debug("开始生成令牌");
    String token = tokenService.generateToken(user);

    return new LoginVO(token, 3600L);
  }

  @Override
  public void register(RegisterDTO request) {
    if (request == null || request.getUsername() == null || request.getPassword() == null
        || request.getConfirmPassword() == null) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }

    String username = request.getUsername().trim();
    if (username.isEmpty()) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }

    if (!request.getPassword().equals(request.getConfirmPassword())) {
      throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCH);
    }

    UserEntity exists = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery()
        .eq(UserEntity::getUsername, username));
    if (exists != null) {
      throw new BusinessException(ErrorCode.USER_EXIST);
    }

    UserEntity user = new UserEntity();
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRealName(request.getRealName());
    user.setStudentNo(request.getStudentNo());
    user.setEmail(request.getEmail());
    user.setPhone(request.getPhone());
    user.setRole(UserRoleEnum.STUDENT);
    user.setStatus(UserStatusEnum.ENABLED);
    user.setLoginFailCount(0);

    int insert = userMapper.insert(user);
    if (insert <= 0) {
      throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }
  }

}
