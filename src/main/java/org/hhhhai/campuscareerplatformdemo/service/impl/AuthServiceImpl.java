package org.hhhhai.campuscareerplatformdemo.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import java.util.Optional;
import org.hhhhai.campuscareerplatformdemo.common.enums.ErrorCode;
import org.hhhhai.campuscareerplatformdemo.dto.LoginDTO;
import org.hhhhai.campuscareerplatformdemo.entity.user.UserEntity;
import org.hhhhai.campuscareerplatformdemo.exception.BusinessException;
import org.hhhhai.campuscareerplatformdemo.mapper.UserMapper;
import org.hhhhai.campuscareerplatformdemo.security.token.TokenService;
import org.hhhhai.campuscareerplatformdemo.service.AuthService;
import org.hhhhai.campuscareerplatformdemo.common.utils.PasswordEncoder;
import org.hhhhai.campuscareerplatformdemo.vo.LoginVO;
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
