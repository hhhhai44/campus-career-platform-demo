package org.hhhhai.campuscareerplatformdemo.entity.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hhhhai.campuscareerplatformdemo.common.enums.ErrorCode;
import org.hhhhai.campuscareerplatformdemo.entity.user.enums.UserRole;
import org.hhhhai.campuscareerplatformdemo.entity.user.enums.UserStatus;
import org.hhhhai.campuscareerplatformdemo.exception.BusinessException;
import org.hhhhai.campuscareerplatformdemo.utils.PasswordEncoder;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

  private Long id;
  private String username;
  private String password;
  private UserRole role;
  private UserStatus status;
  private Long schoolId;

  public void checkCanLogin() {

    // 1.基础状态校验
    if (!status.canLogin()) {
      throw new BusinessException(ErrorCode.USER_BANNED);
    }

  }

  public boolean passwordMatches(String raw, PasswordEncoder encoder) {
    return encoder.matches(raw, this.password);
  }

}
