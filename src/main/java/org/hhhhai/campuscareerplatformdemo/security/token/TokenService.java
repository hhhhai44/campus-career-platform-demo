package org.hhhhai.campuscareerplatformdemo.security.token;


import org.hhhhai.campuscareerplatformdemo.common.context.UserContext;
import org.hhhhai.campuscareerplatformdemo.entity.user.UserEntity;

public interface TokenService {

  String generateToken(UserEntity user);

  UserContext parseAndValidate(String token);
}
