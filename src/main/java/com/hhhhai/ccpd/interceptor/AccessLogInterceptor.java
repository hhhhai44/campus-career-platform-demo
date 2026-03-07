package com.hhhhai.ccpd.interceptor;

import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 统一接口访问日志拦截器
 */
@Slf4j
@Component
public class AccessLogInterceptor implements HandlerInterceptor {

  private static final String ATTR_START_AT = "accessLog.startAt";
  private static final String ATTR_TRACE_ID = "accessLog.traceId";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    long startAt = System.currentTimeMillis();
    request.setAttribute(ATTR_START_AT, startAt);

    String traceId = UUID.randomUUID().toString().replace("-", "");
    request.setAttribute(ATTR_TRACE_ID, traceId);
    response.setHeader("X-Trace-Id", traceId);
    MDC.put("traceId", traceId);

    UserContext user = UserContextHolder.getUser();
    log.info(
        "API_REQUEST traceId={} method={} uri={} query={} ip={} userId={} username={} ua={}",
        traceId,
        request.getMethod(),
        request.getRequestURI(),
        safe(request.getQueryString()),
        resolveClientIp(request),
        user == null ? "-" : user.getUserId(),
        user == null ? "anonymous" : safe(user.getUsername()),
        abbreviate(request.getHeader("User-Agent"), 120));
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) throws Exception {
    Long startAt = (Long) request.getAttribute(ATTR_START_AT);
    long durationMs = startAt == null ? -1L : System.currentTimeMillis() - startAt;

    String traceId = (String) request.getAttribute(ATTR_TRACE_ID);
    UserContext user = UserContextHolder.getUser();

    if (ex == null) {
      log.info(
          "API_RESPONSE traceId={} method={} uri={} status={} durationMs={} userId={} username={}",
          safe(traceId),
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          durationMs,
          user == null ? "-" : user.getUserId(),
          user == null ? "anonymous" : safe(user.getUsername()));
    } else {
      log.error(
          "API_RESPONSE traceId={} method={} uri={} status={} durationMs={} userId={} username={} exType={} exMsg={}",
          safe(traceId),
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          durationMs,
          user == null ? "-" : user.getUserId(),
          user == null ? "anonymous" : safe(user.getUsername()),
          ex.getClass().getSimpleName(),
          safe(ex.getMessage()));
    }

    MDC.remove("traceId");
  }

  private String resolveClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isBlank()) {
      int comma = xForwardedFor.indexOf(',');
      return comma > 0 ? xForwardedFor.substring(0, comma).trim() : xForwardedFor.trim();
    }
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isBlank()) {
      return xRealIp.trim();
    }
    return safe(request.getRemoteAddr());
  }

  private String abbreviate(String value, int maxLen) {
    if (value == null) {
      return "-";
    }
    if (value.length() <= maxLen) {
      return value;
    }
    return value.substring(0, maxLen) + "...";
  }

  private String safe(String value) {
    return (value == null || value.isBlank()) ? "-" : value;
  }
}

