package com.hhhhai.ccpd.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import com.hhhhai.ccpd.common.enums.ErrorCode;
import com.hhhhai.ccpd.dto.LoginDTO;
import com.hhhhai.ccpd.entity.user.UserEntity;
import com.hhhhai.ccpd.exception.BusinessException;
import com.hhhhai.ccpd.mapper.UserMapper;
import com.hhhhai.ccpd.security.token.TokenService;
import com.hhhhai.ccpd.service.AuthService;
import com.hhhhai.ccpd.common.utils.PasswordEncoder;
import com.hhhhai.ccpd.vo.LoginVO;
import org.springframework.stereotype.Service;

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

    // 2. 查用户
    UserEntity user = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery()
        .eq(UserEntity::getUsername, username));

    if(user == null){
      throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }

    // 3. 领域规则校验
    // user.assertCanLogin();

    // 4. 校验密码
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BusinessException(ErrorCode.PASSWORD_ERROR);
    }

    // 5. 生成 token
    String token = tokenService.generateToken(user);

    // 6. 返回用例结果
    return new LoginVO(token, 3600L);
  }

}
