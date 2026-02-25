package com.hhhhai.ccpd.security.token;


import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.entity.user.UserEntity;

public interface TokenService {

  String generateToken(UserEntity user);

  UserContext parseAndValidate(String token);
}
