package org.hhhhai.campuscareerplatformdemo.security.token;


import org.hhhhai.campuscareerplatformdemo.common.context.UserContext;
import org.hhhhai.campuscareerplatformdemo.entity.user.User;

public interface TokenService {

  String generateToken(User user);

  UserContext parseAndValidate(String token);
}
