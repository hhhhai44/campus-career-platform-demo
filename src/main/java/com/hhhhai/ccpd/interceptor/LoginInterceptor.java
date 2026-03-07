package com.hhhhai.ccpd.interceptor;

import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    UserContext user = UserContextHolder.getUser();
    if (user == null) {
      response.setStatus(401);
      log.warn("AUTH_DENY method={} uri={} ip={} reason=unauthorized", request.getMethod(),
          request.getRequestURI(), request.getRemoteAddr());
      return false;
    }

    log.debug("AUTH_PASS method={} uri={} userId={} username={}", request.getMethod(),
        request.getRequestURI(), user.getUserId(), user.getUsername());
    return true;
  }
}
