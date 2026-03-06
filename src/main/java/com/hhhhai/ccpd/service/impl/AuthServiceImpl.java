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

  public LoginVO login(LoginDTO request) {
    String username = request.getUsername();

    log.debug("登录请求: {}", request);
    // 2. 查用户
    UserEntity user = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery()
        .eq(UserEntity::getUsername, username));

    if(user == null){
      throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
    log.debug("用户信息: {}", user);

    // 4. 校验密码
    log.debug("校验密码");
    if (!passwordEncoder.matches(user.getPassword(), request.getPassword())) {
      throw new BusinessException(ErrorCode.PASSWORD_ERROR);
    }

    // 5. 生成 token
    log.debug("生成 token");
    String token = tokenService.generateToken(user);

    // 6. 返回用例结果
    return new LoginVO(token, 3600L);
  }

  @Override
  public void register(RegisterDTO request) {
    // 1. 校验密码一致性
    if (!request.getPassword().equals(request.getConfirmPassword())) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }

    // 2. 校验用户名是否已存在
    UserEntity exists = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery()
        .eq(UserEntity::getUsername, request.getUsername()));
    if (exists != null) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }

    // 3. 构建用户实体
    UserEntity user = new UserEntity();
    user.setUsername(request.getUsername());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRealName(request.getRealName());
    user.setStudentNo(request.getStudentNo());
    user.setEmail(request.getEmail());
    user.setPhone(request.getPhone());
    // 默认角色：学生
    user.setRole(UserRoleEnum.STUDENT);
    // 默认状态：正常
    user.setStatus(UserStatusEnum.ENABLED);
    user.setLoginFailCount(0);

    // 4. 插入数据库
    int insert = userMapper.insert(user);
    if (insert <= 0) {
      throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }
  }

}
