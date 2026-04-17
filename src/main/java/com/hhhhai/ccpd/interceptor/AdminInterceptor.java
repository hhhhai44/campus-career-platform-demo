package com.hhhhai.ccpd.interceptor;

import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import com.hhhhai.ccpd.common.enums.ErrorCode;
import com.hhhhai.ccpd.common.enums.UserRoleEnum;
import com.hhhhai.ccpd.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员权限拦截器
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }

    String role = user.getRole();
    boolean isAdmin = role != null
        && (UserRoleEnum.ADMIN.getDescription().equals(role)
        || String.valueOf(UserRoleEnum.ADMIN.getCode()).equals(role));
    if (!isAdmin) {
      throw new BusinessException(ErrorCode.ADMIN_FORBIDDEN);
    }

    return true;
  }
}


